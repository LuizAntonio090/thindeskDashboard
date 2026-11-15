# Dados de demonstração para gravação

A carga é fictícia e fica desativada na execução normal do Thindesk. Ela é inserida no MongoDB configurado para o projeto apenas ao iniciar com o perfil `demo`.

## Abrir com a carga

Feche outra instância que esteja usando a porta 8081. Abra **Carregar demonstracao.cmd** na raiz, aguarde a mensagem `DEMO pronta` e acesse http://localhost:8081/login. Utilize a mesma conta de acesso do projeto.

Também é possível executar:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081 --spring.profiles.active=demo"
```

Depois de carregados, os registros continuam disponíveis ao iniciar por **Iniciar Thindesk.cmd**. Não é necessário manter o perfil demo ativo.

## Conteúdo

- 72 chamados: 18 abertos, 18 em andamento, 30 concluídos e 6 cancelados.
- Taxa de resolução esperada da carga isolada: 41,67%.
- 60 chamados com considerações técnicas e 3 sem data de abertura, para demonstrar o tratamento de dados incompletos.
- 8 clientes fictícios, 4 equipes técnicas, 5 tipos, 3 recursos e 5 horários de atendimento.
- Datas distribuídas pelos 160 dias anteriores à primeira carga, com agrupamento por dia ou mês.

A tabela exibe 20 itens por página e terá 4 páginas quando houver somente os dados da carga. Os totais do painel incluem também os registros reais que já existirem no banco.

Os IDs são estáveis. Ao executar novamente, a carga usa somente `$setOnInsert`: não duplica nem atualiza documentos existentes. As datas permanecem as da primeira inserção. Não há substituição ou limpeza das coleções. Cada registro possui `demoLote=thindesk-video-v1` e os chamados estão identificados com DEMO na descrição.

## Roteiro curto para o vídeo

1. Apresentar os cartões e a taxa de resolução no dashboard.
2. Descer até o gráfico de evolução e alternar o agrupamento entre Dia e Mês.
3. Mostrar a distribuição por situação e os tipos mais demandados.
4. Filtrar por status `Aberto` e depois por tipo `Rede`.
5. Usar a busca `impressora` e mostrar as considerações técnicas.
6. Limpar os filtros e avançar para a segunda página da tabela.
7. Apresentar os clientes e os cadastros de técnicos, tipos, recursos e horários.

## Remover depois da gravação

Feche a aplicação na porta 8081 e abra **Remover demonstracao.cmd**. Confirme quando o iniciador perguntar. A remoção seleciona apenas os documentos com `demoLote=thindesk-video-v1` em chamados, clientes, configurações e horários. Registros que já existiam sem essa marca não são removidos.

Alternativa:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081 --spring.profiles.active=demo --thindesk.demo.limpar=true"
```

O comando de limpeza inicia a aplicação sem recriar a carga. Para recriar, interrompa essa execução e abra **Carregar demonstracao.cmd** novamente. Qualquer alteração feita nos registros DEMO também será removida pela limpeza, pois eles continuam pertencendo à carga.

A carga é uma etapa posterior às evidências originais da documentação Word, que registraram a base vazia. O código de produção e os endpoints existentes permanecem com os mesmos contratos.