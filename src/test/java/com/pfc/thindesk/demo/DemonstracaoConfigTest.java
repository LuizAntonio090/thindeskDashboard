package com.pfc.thindesk.demo;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.result.DeleteResult;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DemonstracaoConfigTest {
    @Test void cargaFicaDesativadaSemPerfilDemo() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.registerBean(MongoTemplate.class, () -> mock(MongoTemplate.class));
            context.register(DemonstracaoConfig.class);
            context.refresh();
            assertThat(context.getBeansOfType(DemonstracaoConfig.class)).isEmpty();
        }
    }

    @Test void dadosTemIdsEstaveisVinculosValidosEDistribuicaoParaVideo() {
        var dados = DemonstracaoConfig.dados(LocalDate.of(2026, 11, 14));
        var todosIds = new HashSet<Object>();
        dados.values().forEach(lista -> lista.forEach(d -> {
            assertThat(todosIds.add(d.get("_id"))).isTrue();
            assertThat(d.getString("demoLote")).isEqualTo(DemonstracaoConfig.MARCADOR);
        }));
        assertThat(todosIds).hasSize(97);
        var clientes = dados.get("clientes").stream().map(d -> d.getObjectId("_id").toHexString()).toList();
        var chamados = dados.get("chamados");
        assertThat(chamados).hasSize(72);
        assertThat(chamados.stream().filter(d -> d.getString("status").equals("Aberto"))).hasSize(18);
        assertThat(chamados.stream().filter(d -> d.getString("status").equals("Em andamento"))).hasSize(18);
        assertThat(chamados.stream().filter(d -> d.getString("status").equals("Concluído"))).hasSize(30);
        assertThat(chamados.stream().filter(d -> d.getString("status").equals("Cancelado"))).hasSize(6);
        chamados.forEach(d -> assertThat(clientes).contains(d.getString("clienteId")));
        assertThat(chamados.stream().filter(d -> d.get("dataAbertura") == null)).hasSize(3);
        assertThat(chamados.stream().filter(d -> !d.getString("consideracoesTecnicas").isEmpty())).hasSize(60);
        assertThat(DemonstracaoConfig.dados(LocalDate.of(2026, 11, 14))).isEqualTo(dados);
    }

    @Test void reexecucaoUsaSomenteSetOnInsertPreservandoDadosExistentes() {
        MongoTemplate mongo = mock(MongoTemplate.class);
        BulkOperations lote = mock(BulkOperations.class);
        BulkWriteResult resultado = mock(BulkWriteResult.class);
        when(mongo.bulkOps(eq(BulkOperations.BulkMode.UNORDERED), anyString())).thenReturn(lote);
        when(lote.execute()).thenReturn(resultado);
        when(resultado.getUpserts()).thenReturn(List.of());
        new DemonstracaoConfig(mongo, false).run();
        var updates = ArgumentCaptor.forClass(Update.class);
        verify(lote, times(97)).upsert(any(Query.class), updates.capture());
        updates.getAllValues().forEach(u -> {
            assertThat(u.getUpdateObject().keySet()).containsExactly("$setOnInsert");
            assertThat(u.getUpdateObject().get("$setOnInsert", Document.class).getString("demoLote"))
                    .isEqualTo(DemonstracaoConfig.MARCADOR);
        });
        verify(mongo, never()).remove(any(Query.class), anyString());
    }

    @Test void limpezaSelecionaSomenteLoteDemoENaoRecriaCarga() {
        MongoTemplate mongo = mock(MongoTemplate.class);
        when(mongo.remove(any(Query.class), anyString())).thenReturn(DeleteResult.acknowledged(0));
        new DemonstracaoConfig(mongo, true).run();
        var queries = ArgumentCaptor.forClass(Query.class);
        verify(mongo, times(4)).remove(queries.capture(), anyString());
        queries.getAllValues().forEach(q -> assertThat(q.getQueryObject())
                .isEqualTo(new Document("demoLote", DemonstracaoConfig.MARCADOR)));
        verify(mongo, never()).bulkOps(any(BulkOperations.BulkMode.class), anyString());
    }
}
