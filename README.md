# Thindesk — Dashboard administrativo

Projeto acadêmico de central de atendimento com API integrada ao MongoDB e dashboard administrativo. Implementado em Java, Spring Boot 3.4.0, Spring Security, Spring Data MongoDB, Thymeleaf, Bootstrap e Chart.js.

## Entrega e Gitflow

Repositório: [LuizAntonio090/tindeskDashboard](https://github.com/LuizAntonio090/tindeskDashboard).

**A implementação da atividade está na branch `feature/api-dashboard`.**

| Branch | Finalidade |
| --- | --- |
| `main` | Projeto-base e histórico já existentes no repositório. |
| `develop` | Base de integração com configuração privada separada e arquivos compilados fora do versionamento. |
| `feature/api-dashboard` | API do dashboard integrada ao MongoDB, interface administrativa, cadastros de apoio, testes e documentação. |

Fluxo de revisão: `feature/api-dashboard` → `develop`. A implementação permanece na feature para avaliação; não foi realizado merge na `main`.

## Documentação da atividade

- [Documento em Word](docs/Thindesk_Documentacao_Tecnica.docx): requisitos funcionais e não funcionais, contratos dos endpoints, parâmetros, exemplos de requisição e resposta, erros, testes e evidências visuais.
- [Resultados dos testes](docs/evidencias/testes-execucao-atual.json).
- [Respostas HTTP capturadas](docs/evidencias/api-execucao-atual.json).
- [Dashboard em execução](docs/evidencias/thindesk-dashboard.png).
- [Região dos gráficos](docs/evidencias/thindesk-graficos.png).

## Executar o projeto

Requisitos: JDK 23 ou superior compatível, MongoDB 4.2+ e conexão de rede para dependências e bibliotecas carregadas por CDN. A validação utilizou JDK 27.

```powershell
git clone https://github.com/LuizAntonio090/tindeskDashboard.git
cd tindeskDashboard
git switch feature/api-dashboard
Copy-Item .thindesk-local.properties.example .thindesk-local.properties
```

Preencha a URI do banco somente em `.thindesk-local.properties`, ignorado pelo Git. O exemplo contém marcadores. Use o usuário e a senha do banco Atlas, com caracteres especiais codificados para URL, e autorize o IP no Atlas. A configuração privada não é distribuída no repositório.

Também é possível definir `MONGODB_URI`, ou `MONGODB_USERNAME` e `MONGODB_PASSWORD`, no ambiente. Se o arquivo privado definir `spring.data.mongodb.uri`, essa propriedade importada prevalece sobre a definição padrão.

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
```

No Windows, também é possível abrir **Iniciar Thindesk.cmd**. Acesse [http://localhost:8081/login](http://localhost:8081/login) e depois `/dashboard`. O iniciador usa a porta 8081; sem esse argumento, a aplicação utiliza 8080 ou a variável `SERVER_PORT`.

Conta demonstrativa da configuração padrão: `admin` / `thindesk`. Ela pode ser substituída pelas propriedades do Spring Security. Não se trata de uma credencial do MongoDB. Não utilizar a conta demonstrativa em produção.

Nenhuma carga é importada automaticamente. `chamados.json` é apenas uma carga de referência. Os testes HTTP documentados fizeram somente consultas de leitura e encontraram a base vazia. Os gráficos ficam sem séries nessa situação; não foram criados dados fictícios no banco compartilhado.

## API do dashboard

As seis rotas exigem autenticação de sessão ou HTTP Basic. O prefixo é `/api/dashboard`, com resposta JSON no formato `{"sucesso":true,"dados":...,"erro":null}`.

| Método | Endpoint | Finalidade |
| --- | --- | --- |
| GET | `/api/dashboard/indicadores` | Total, abertos, em andamento, resolvidos, outros, taxa de resolução, sem data e com considerações. |
| GET | `/api/dashboard/por-periodo` | Evolução por abertura; `agrupamento=dia` ou `mes`. |
| GET | `/api/dashboard/situacao` | Quantidade e percentual por status. |
| GET | `/api/dashboard/tipos` | Ranking de demanda por tipo. |
| GET | `/api/dashboard/chamados` | Listagem paginada com campos de atendimento e considerações. |
| GET | `/api/dashboard/chamados/{id}` | Detalhe de um chamado pelo ObjectId. |

Nas cinco consultas coletivas: `dataInicio` e `dataFim` em AAAA-MM-DD, `status`, `tipo`, `cliente` (comparado com `clienteId`) e `busca`. Os filtros são opcionais e combinados com AND. A busca textual é literal e ignora maiúsculas nos campos descrição, técnico, usuário e considerações. Textos têm limite de 200 caracteres.

Datas usam America/Sao_Paulo; início e fim são inclusivos. Listagem: `pagina=0` por padrão, até 1000000; `tamanho=20` por padrão, entre 1 e 100. Os registros são ordenados por data de abertura decrescente e ID no desempate.

```powershell
# A senha é solicitada pelo curl, sem ficar gravada no comando.
curl.exe -u admin "http://localhost:8081/api/dashboard/indicadores"
curl.exe -u admin "http://localhost:8081/api/dashboard/por-periodo?agrupamento=mes"
curl.exe -u admin "http://localhost:8081/api/dashboard/chamados?pagina=0&tamanho=20"
```

Resposta real observada na base vazia:

```json
{"sucesso":true,"dados":{"total":0,"abertos":0,"emAndamento":0,"resolvidos":0,"outros":0,"taxaResolucao":0.0,"semDataAbertura":0,"comConsideracoesTecnicas":0},"erro":null}
```

Erros do dashboard: `400` para parâmetros inválidos, `401` sem autenticação, `403` acesso negado, `404` ObjectId válido sem registro e `500` falha interna de consulta. Erros seguem `{"sucesso":false,"dados":null,"erro":{"status":400,"mensagem":"..."}}`. A documentação em Word apresenta os exemplos completos e os limites de cada rota.

## Integração e funcionalidades

Os indicadores e agrupamentos são calculados no MongoDB por `MongoTemplate`, com `$addFields`, `$match`, `$group`, `$facet`, `$sort`, `$skip` e `$limit`. Não se substitui o banco por dados fixos no frontend. A normalização preserva o legado `Tipo`, trata textos e datas ausentes e não inventa datas históricas.

O dashboard possui seis cartões, três gráficos, tabela de detalhamento, filtros combinados, paginação, atualização assíncrona, cancelamento de requisições anteriores e estados de carregamento, vazio e erro. A apresentação utiliza verde e marfim, menu superior e filtros à direita.

As APIs legadas de chamados e clientes mantêm seu formato direto, sem o envelope do dashboard. Há criação, listagem e atualização de chamados; criação e listagem de clientes. Técnicos, tipos e recursos possuem páginas de cadastro, edição e exclusão; horários permitem cadastro, listagem e exclusão. Alguns links legados de clientes e chamados não possuem handlers correspondentes; o documento delimita essas rotas, sem afirmar um CRUD de interface inexistente.

## Testes e evidências

```powershell
.\mvnw.cmd test
```

Execução em 14 de novembro de 2026: **44 testes, zero falhas, zero erros e zero ignorados**, com `BUILD SUCCESS`.

| Suíte | Casos |
| --- | ---: |
| `DashboardApiTest` | 24 |
| `DashboardViewTest` | 2 |
| `ConfiguracaoWebTest` | 18 |

JUnit 5, MockMvc e Mockito verificam contratos, autenticação, filtros, paginação, percentuais, estados vazios, erros e formulários. Os testes automatizados usam dependências de banco/serviço simuladas e não substituem testes de carga ou integração com uma base populada.

A conferência adicional na aplicação conectada ao MongoDB registrou HTTP 200 nas cinco consultas coletivas e HTTP 400, 401 e 404 nos cenários inválidos previstos. A falha 500 foi simulada nos testes, sem interromper o banco. Login e dashboard foram verificados no navegador. Nenhum registro foi criado ou alterado nessas consultas.

## Limites e configuração

Qualquer usuário autenticado pode acessar o dashboard; não há separação de perfil administrador. CSRF permanece desabilitado conforme a base. Bibliotecas visuais usam CDN. Não foram medidos SLA, carga simultânea ou latência máxima. A normalização anterior aos filtros pode limitar o uso de índices; não há garantia de desempenho em grandes volumes.

A data de conclusão não é preenchida automaticamente ao alterar o status. `clienteId` é referência lógica sem validação de integridade referencial. Arquivos compilados, configuração privada, logs e arquivos temporários são ignorados pelo Git. O histórico preexistente em `main` é preservado; a exclusão de arquivos nas branches novas não apaga conteúdo de commits antigos.
