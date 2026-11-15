package com.pfc.thindesk.demo;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

/** Carga fictícia ativada exclusivamente pelo perfil demo, sem substituir registros existentes. */
@Component
@Profile("demo")
public class DemonstracaoConfig implements CommandLineRunner {
    static final String MARCADOR = "thindesk-video-v1";
    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    private static final Logger log = LoggerFactory.getLogger(DemonstracaoConfig.class);
    private final MongoTemplate mongo;
    private final boolean limpar;

    public DemonstracaoConfig(MongoTemplate mongo,
            @Value("${thindesk.demo.limpar:false}") boolean limpar) {
        this.mongo = mongo;
        this.limpar = limpar;
    }

    @Override
    public void run(String... args) {
        if (limpar) {
            for (String colecao : List.of("chamados", "clientes", "configuracoes", "horariosAtendimento")) {
                long removidos = mongo.remove(Query.query(Criteria.where("demoLote").is(MARCADOR)), colecao)
                        .getDeletedCount();
                log.info("DEMO: {} registros de demonstracao removidos de {}", removidos, colecao);
            }
            return;
        }
        dados(LocalDate.now(ZONA)).forEach((colecao, documentos) -> {
            BulkOperations lote = mongo.bulkOps(BulkOperations.BulkMode.UNORDERED, colecao);
            for (Document documento : documentos) {
                Update update = new Update();
                documento.forEach((campo, valor) -> {
                    if (!campo.equals("_id")) update.setOnInsert(campo, valor);
                });
                lote.upsert(Query.query(Criteria.where("_id").is(documento.get("_id"))), update);
            }
            var resultado = lote.execute();
            log.info("DEMO: {} inseridos em {}; registros existentes preservados", resultado.getUpserts().size(), colecao);
        });
        log.info("DEMO pronta: 72 chamados ficticios, 8 clientes, 12 cadastros de apoio e 5 horarios. Acesse /dashboard.");
    }

    static Map<String, List<Document>> dados(LocalDate referencia) {
        Map<String, List<Document>> dados = new LinkedHashMap<>();
        List<Document> clientes = new ArrayList<>();
        String[] setores = {"Administrativo", "Financeiro", "Biblioteca", "Laboratório", "Secretaria", "Comercial", "Recepção", "Tecnologia"};
        for (int i = 0; i < setores.length; i++) {
            clientes.add(base(100 + i).append("nome", "Unidade " + setores[i] + " (DEMO)")
                    .append("telefone", "(00) 00000-0000").append("setor", setores[i]));
        }
        dados.put("clientes", clientes);
        String[] tecnicos = {"Equipe Redes (DEMO)", "Equipe Sistemas (DEMO)", "Equipe Suporte (DEMO)", "Equipe Infra (DEMO)"};
        String[] tipos = {"Manutenção", "Rede", "Software", "Acesso", "Instalação"};
        List<Document> configuracoes = new ArrayList<>();
        int chave = 200;
        for (String tecnico : tecnicos) configuracoes.add(base(chave++).append("categoria", "tecnicos").append("nome", tecnico).append("descricao", "Equipe fictícia para gravação da apresentação."));
        for (String tipo : tipos) configuracoes.add(base(chave++).append("categoria", "tipo-chamado").append("nome", tipo).append("descricao", "Categoria utilizada nos chamados de demonstração."));
        for (String recurso : List.of("Computador (DEMO)", "Impressora (DEMO)", "Roteador (DEMO)")) configuracoes.add(base(chave++).append("categoria", "recursos").append("nome", recurso).append("descricao", "Recurso fictício de demonstração."));
        dados.put("configuracoes", configuracoes);
        List<Document> horarios = new ArrayList<>();
        String[] dias = {"Segunda-feira", "Terça-feira", "Quarta-feira", "Quinta-feira", "Sexta-feira"};
        for (int i = 0; i < dias.length; i++) horarios.add(base(300 + i).append("setor", "Suporte (DEMO)").append("diaSemana", dias[i]).append("horarioInicio", "08:00").append("horarioFim", "18:00"));
        dados.put("horariosAtendimento", horarios);
        List<Document> chamados = new ArrayList<>();
        String[] descricoes = {"Computador com lentidão", "Impressora sem conexão", "Atualização do sistema", "Redefinição de acesso", "Instalação de aplicativo", "Falha de conexão Wi-Fi", "Monitor sem imagem", "Configuração de e-mail", "Backup de arquivos", "Troca de teclado", "Ajuste de permissões", "Verificação do servidor"};
        String[] notas = {"Equipamento revisado e testado com o solicitante.", "Verificação de rede e cabeamento registrada.", "Atualização aplicada em ambiente de demonstração.", "Permissões conferidas; aguardando retorno do solicitante."};
        for (int i = 0; i < 72; i++) {
            int grupo = i % 12;
            String status = grupo < 3 ? "Aberto" : grupo < 6 ? "Em andamento" : grupo < 11 ? "Concluído" : "Cancelado";
            LocalDate abertura = referencia.minusDays((i * 7) % 160);
            Document chamado = base(1000 + i)
                    .append("descricao", descricoes[i % descricoes.length] + " - DEMO " + String.format("%02d", i + 1))
                    .append("status", status).append("tipo", tipos[i % tipos.length])
                    .append("tecnico", tecnicos[i % tecnicos.length]).append("usuario", "Solicitante DEMO " + String.format("%02d", i % 8 + 1))
                    .append("clienteId", id(100 + i % 8).toHexString())
                    .append("consideracoesTecnicas", i % 6 == 0 ? "" : notas[i % notas.length])
                    .append("dataAbertura", i < 3 ? null : Date.from(abertura.atTime(9 + i % 8, 15).atZone(ZONA).toInstant()))
                    .append("dataConclusao", status.equals("Concluído") ? Date.from(abertura.plusDays(Math.min(2, (int) java.time.temporal.ChronoUnit.DAYS.between(abertura, referencia))).atTime(18, 0).atZone(ZONA).toInstant()) : null);
            chamados.add(chamado);
        }
        dados.put("chamados", chamados);
        return dados;
    }

    private static Document base(int chave) {
        return new Document("_id", id(chave)).append("demoLote", MARCADOR);
    }

    private static ObjectId id(int chave) {
        return new ObjectId("7de00000" + String.format("%016x", chave));
    }
}
