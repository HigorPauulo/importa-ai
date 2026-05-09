# Especificação de Requisitos de Software (ERS / SRS)

**Sistema:** Importa Aí — Sistema de Gestão e Rastreamento de Encomendas Internacionais
**Versão:** 1.1
**Data:** 09 de Maio de 2026
**Autor:** Equipe Importa Aí

## Histórico de Revisão

| Data | Versão | Descrição | Autor |
|------|--------|-----------|-------|
| 19/03/2026 | 1.0 | Elaboração da primeira versão do documento | Higor Paulo Costa |
| 09/05/2026 | 1.1 | Substituição de Kafka por RabbitMQ; status do pedido passa a ser derivado das etapas; remoção do escopo de e-mail (cadastro, recuperação de senha e notificações por e-mail) | Higor Paulo Costa |

---

## Sumário

1. Introdução
2. Escopo do Sistema
3. Definições, Siglas e Abreviações
4. Perspectiva do Produto
5. Stakeholders
6. Funções Principais do Sistema
7. Características dos Usuários
8. Premissas
9. Dependências
10. Requisitos Funcionais (RF)
11. Requisitos Não-Funcionais (RNF)
12. Regras de Negócio (RN)
13. Casos de Uso (UC)

---

## 1. Introdução

Este documento é a Especificação de Requisitos de Software (ERS) do sistema "Importa Aí". Seu objetivo é descrever de forma completa, precisa e não ambígua todos os requisitos funcionais, não funcionais, regras de negócio, restrições, casos de uso e critérios de aceitação do sistema, servindo como contrato entre as partes interessadas (stakeholders), a equipe de desenvolvimento e os testadores.

---

## 2. Escopo do Sistema

O sistema "Importa Aí" é uma plataforma de gestão e rastreamento de encomendas internacionais, com foco no corredor logístico China–Brasil. O sistema permite que usuários cadastrem, acompanhem e gerenciem pedidos importados, integrando-se com a API dos Correios para rastreamento nacional e com APIs de cotação de câmbio para conversão de valores.

### Fora do escopo

- Processamento de pagamentos de compras internacionais.
- Emissão de notas fiscais ou documentos aduaneiros.
- Gestão de estoque ou armazenagem em armazéns.
- Negociação direta com fornecedores chineses.
- **Recuperação de senha por e-mail (v2 do sistema).**
- **Notificações por e-mail (v2 do sistema).**
- **Confirmação de cadastro por e-mail (cadastro tem ativação automática nesta versão).**

---

## 3. Definições, Siglas e Abreviações

| Termo / Sigla | Definição |
|---------------|-----------|
| ERS / SRS | Especificação de Requisitos de Software (Software Requirements Specification). |
| RF | Requisito Funcional — descreve uma funcionalidade que o sistema deve executar. |
| RNF | Requisito Não Funcional — qualidade ou restrição de sistema (desempenho, segurança etc.). |
| RN | Regra de Negócio — política do domínio que restringe ou orienta o comportamento do sistema. |
| UC | Use Case (Caso de Uso) — descreve a interação entre ator e sistema para alcançar um objetivo. |
| US | User Story (História de Usuário) — descrição informal de uma funcionalidade do produto na visão do usuário. |
| API | Application Programming Interface — interface de integração entre sistemas. |
| RabbitMQ | Broker de mensagens AMQP utilizado para desacoplar as operações de escrita do frontend da persistência. |
| AMQP | Advanced Message Queuing Protocol — protocolo padrão usado pelo RabbitMQ. |
| WebSocket | Protocolo de comunicação bidirecional em tempo real entre cliente e servidor. |
| STOMP | Simple Text Oriented Messaging Protocol — usado sobre WebSocket para mensagens estruturadas. |
| DLQ | Dead Letter Queue — fila de mensagens com falha de processamento após N tentativas. |
| JWT | JSON Web Token — padrão RFC 7519 para autenticação e autorização stateless. |
| Correios | Empresa Brasileira de Correios — provedor de rastreamento nacional. |
| CD | Centro de Distribuição dos Correios no Brasil. |
| LGPD | Lei Geral de Proteção de Dados — Lei nº 13.709/2018. |

---

## 4. Perspectiva do Produto

O "Importa Aí" é um sistema web independente que atua como camada de gestão e visibilidade sobre o fluxo logístico de encomendas internacionais. Ele não substitui os sistemas dos Correios ou das transportadoras, mas os integra, consolidando informações em uma interface unificada para o usuário final.

---

## 5. Stakeholders

| Stakeholder | Papel | Interesse principal |
|-------------|-------|---------------------|
| Usuário / Cliente | Importador pessoa física ou jurídica | Rastrear encomendas, saber status e cotação em tempo real. |
| Administrador | Operador interno do sistema | Monitorar o fluxo de pacotes, gerenciar eventos e resolver anomalias. |
| Equipe de Dev | Desenvolvedores e engenheiros | Implementar e manter o sistema com requisitos para validar entregas. |
| Equipe de QA | Testadores | Critérios de aceitação bem definidos para validar entregas. |
| API Correios | Sistema externo | Fornecer dados de rastreamento nacional. |
| API Câmbio | Sistema externo | Fornecer cotações de moedas. |

---

## 6. Funções Principais do Sistema

- Cadastro e gerenciamento do ciclo de vida de pedidos de importação.
- Rastreamento multiestágio em tempo real (China → Aeroporto → Brasil → Entrega).
- Dashboard administrativo com visão consolidada do fluxo de pacotes.
- Integração com API dos Correios para sub-etapas nacionais (taxa, CD, saída para entrega).
- Cotação de câmbio integrada (BRL/USD/CNY/EUR) — manual ou via API.
- Notificações em tempo real via WebSocket quando o status do pedido é alterado.
- Autenticação e autorização com perfis de acesso distintos (Cliente e Administrador).

---

## 7. Características dos Usuários

| Perfil | Nível técnico | Características relevantes |
|--------|--------------|----------------------------|
| Usuário / Cliente | Básico a intermediário | Acessa via browser ou mobile, espera interface simples e notificações proativas. Não tem conhecimento técnico sobre logística internacional. |
| Administrador | Intermediário a avançado | Conhece o fluxo operacional da empresa, usa dashboard para monitorar volumes, identificar anomalias e tomar ações corretivas. |

---

## 8. Premissas

- O usuário possui acesso à internet para usar o sistema.
- A API dos Correios estará disponível e retornará dados de rastreamento em formato padronizado.
- A API de Cotação de Câmbio estará disponível. Em caso de indisponibilidade, o sistema usará a última cotação armazenada em cache (RN07).
- O broker RabbitMQ estará disponível para receber publicações. Em caso de indisponibilidade, ver tratamento na UC01 (FA02).
- Os pedidos são sempre iniciados manualmente pelo usuário (não há integração automática com marketplaces nesta versão).

---

## 9. Dependências

- Backend: Java 17+ / Spring Boot 3.x.
- Frontend: React 18+ / TypeScript / Tailwind CSS 3.x.
- **Mensageria: RabbitMQ 3.13+ (protocolo AMQP).**
- Banco de dados: MySQL 8.x.
- Tempo real: STOMP sobre WebSocket (SockJS como fallback).
- Serviço externo: API dos Correios (SRO — Sistema de Rastreamento de Objetos).
- Serviço externo: API de cotação (ex.: AwesomeAPI, Open Exchange Rates ou similar).

---

## 10. Requisitos Funcionais (RF)

Cada requisito funcional está classificado por prioridade (Alta / Média / Baixa), módulo, e possui critério de aceitação mensurável.

| Prioridade | Critério |
|------------|----------|
| Alta | Essencial para o MVP. Bloqueia a entrega se ausente. |
| Média | Importante, mas pode ser entregue em sprint posterior. |
| Baixa | Desejável. Será implementado se houver capacidade. |

> **Nota de versionamento:** RFs removidos na v1.1 são marcados com tarja "REMOVIDO" e mantêm o ID original para preservar referências externas. Não há renumeração.

### Módulo: Autenticação e Controle de Acesso

| ID | Nome | Descrição | Prioridade |
|----|------|-----------|------------|
| RF01 | Cadastro de Usuário | O sistema deve permitir que novos usuários se cadastrem informando: nome completo, e-mail (único) e senha (mínimo 8 caracteres, com letras e números). **A conta é ativada imediatamente após o cadastro (sem confirmação por e-mail nesta versão).** | Alta |
| RF02 | Login e Autenticação | O sistema deve autenticar usuários via e-mail e senha, retornando um JWT de acesso (expiração: 1h) e um refresh token (expiração: 7 dias). Após 5 tentativas falhas, a conta deve ser bloqueada por 15 minutos. | Alta |
| RF03 | Logout e Revogação de Token | O sistema deve invalidar o refresh token no servidor ao realizar logout, impedindo que tokens roubados sejam reutilizados. A revogação é feita por uma tabela `refresh_token_revogado` consultada no fluxo de refresh. | Alta |
| ~~RF04~~ | ~~Recuperação de Senha~~ | **REMOVIDO na v1.1.** Recuperação de senha por e-mail está fora do escopo desta versão. Em caso de perda de senha, o Administrador pode redefinir manualmente via gestão de usuários (RF25). | — |
| RF05 | Controle de Perfis (RBAC) | O sistema deve distinguir dois perfis: Cliente (acessa apenas seus próprios pedidos) e Administrador (acessa todos os pedidos, dashboard e configurações). A promoção de perfil deve ser feita apenas por outro Administrador. | Alta |

### Módulo: Gestão de Pedidos

| ID | Nome | Descrição | Prioridade |
|----|------|-----------|------------|
| RF06 | Cadastro de Pedido | O usuário deve poder cadastrar um novo pedido informando: código de rastreamento (obrigatório), descrição do produto, valor declarado em moeda de origem, moeda (CNY/USD/EUR), fornecedor/plataforma de compra (opcional) e data estimada de entrega (opcional). O sistema deve retornar resposta imediata (HTTP 202) sem aguardar a persistência. | Alta |
| RF07 | Listagem de Pedidos | O usuário deve visualizar todos os seus pedidos em uma lista paginada (20 itens/página), com filtros por: status, período de criação e código de rastreamento. A lista deve exibir: código, status atual, última atualização e valor convertido. | Alta |
| RF08 | Detalhe do Pedido | Ao selecionar um pedido, o usuário deve ver: informações completas do pedido, linha do tempo com todas as etapas de rastreamento em ordem cronológica, valor convertido na cotação atual e histórico de eventos. | Alta |
| RF09 | Edição de Pedido | O usuário pode editar descrição, valor declarado e data estimada de entrega enquanto o status derivado do pedido for `PROCESSANDO` ou `ENVIADO`. Pedidos com status derivado `ENTREGUE` são imutáveis. | Média |
| RF10 | Cancelamento / Arquivamento | O usuário pode arquivar pedidos com status `ENTREGUE`. O Administrador pode cancelar pedidos em qualquer estado (operação representada por flag `cancelado` no domínio). Pedidos cancelados devem ser mantidos no banco por 12 meses para fins de auditoria (RN08). | Média |

### Módulo: Rastreamento Multiestágio

| ID | Nome | Descrição | Prioridade |
|----|------|-----------|------------|
| RF11 | Etapas Internacionais | O sistema deve registrar e exibir as etapas internacionais: 1) NA_CHINA — pedido processado pelo fornecedor; 2) AEROPORTO_ORIGEM — despacho aduaneiro China; 3) EM_TRANSITO — em voo internacional; 4) AEROPORTO_DESTINO — chegada ao Brasil. | Alta |
| RF12 | Etapas Nacionais (Correios) | Após chegada ao Brasil, o sistema deve consultar a API dos Correios e registrar as sub-etapas: NO_BRASIL (recebido), Taxa (aguardando pagamento de imposto, quando aplicável), CD_BRASIL (em CD regional), SAIDA_ENTREGA (saída para entrega) e ENTREGUE (entrega ao destinatário). | Alta |
| RF13 | Atualização Manual de Etapa | O Administrador deve poder inserir manualmente uma nova etapa de rastreamento para qualquer pedido, com descrição livre e timestamp. Útil para etapas não capturadas automaticamente pela API dos Correios. **Esta é a única forma de promover o status do pedido (ver RN01).** | Média |
| RF14 | Linha do Tempo Visual | O sistema deve exibir todas as etapas de rastreamento em uma linha do tempo vertical, com ícone por tipo de etapa, timestamp, localização (quando disponível) e destaque visual para a etapa atual. | Alta |
| RF15 | Sincronização Automática | O sistema deve consultar automaticamente a API dos Correios a cada 6 horas para pedidos cujo status derivado seja `ENVIADO` (ou seja, com etapas internacionais já concluídas e ainda sem entrega). O intervalo deve ser configurável por variável de ambiente. | Média |

### Módulo: Notificações em Tempo Real

| ID | Nome | Descrição | Prioridade |
|----|------|-----------|------------|
| RF16 | Notificação de Mudança de Status | Sempre que uma nova etapa de rastreamento for registrada (e portanto o status derivado puder mudar), o sistema deve enviar uma notificação em tempo real via WebSocket para o usuário dono do pedido, sem necessidade de recarregar a página. | Alta |
| RF17 | Central de Notificações | O sistema deve manter um histórico das últimas 50 notificações por usuário, indicando: mensagem, pedido relacionado, timestamp e se foi lida. O usuário pode marcar notificações como lidas individualmente ou em massa. | Média |
| ~~RF18~~ | ~~Notificação por E-mail~~ | **REMOVIDO na v1.1.** Notificações por e-mail estão fora do escopo desta versão. O canal único de notificação é WebSocket + Central de Notificações (RF17). | — |

### Módulo: Cotações de Câmbio

| ID | Nome | Descrição | Prioridade |
|----|------|-----------|------------|
| RF19 | Exibição de Cotação | O sistema deve exibir, em cada pedido, o valor convertido para BRL utilizando a cotação atual. Os pares de moedas suportados são: CNY/BRL, USD/BRL e EUR/BRL. | Alta |
| RF20 | Cotação via API | O sistema deve buscar cotações automaticamente via API externa a cada 30 minutos e armazenar em cache. Em caso de falha da API, usar a última cotação válida armazenada, indicando ao usuário que a cotação pode estar desatualizada. | Média |
| RF21 | Cotação Manual | O Administrador deve poder inserir manualmente a taxa de câmbio de qualquer par de moedas suportado. A cotação manual deve sobrescrever a automática e ser indicada visualmente na interface. | Média |

### Módulo: Dashboard Administrativo

| ID | Nome | Descrição | Prioridade |
|----|------|-----------|------------|
| RF22 | Visão Geral de Pedidos | O dashboard deve exibir cards de resumo com: total de pedidos ativos, pedidos por status (contagem e percentual), pedidos com taxa pendente e pedidos entregues no mês. | Alta |
| RF23 | Gráfico de Evolução | O dashboard deve exibir um gráfico de linha com a evolução do volume de pedidos nos últimos 30 dias, com opção de filtrar por status. | Média |
| RF24 | Lista de Pedidos Recentes | O dashboard deve exibir os 10 pedidos mais recentemente atualizados com acesso rápido ao detalhe de cada um. | Média |
| RF25 | Gestão de Usuários | O Administrador deve poder listar, buscar, ativar/desativar, promover/rebaixar usuários **e redefinir senha de qualquer usuário** (substituto operacional do antigo RF04). Não é possível excluir usuários (soft delete); apenas desativar. | Média |
| RF26 | Exportação de Dados | O Administrador deve poder exportar a lista de pedidos (com filtros aplicados) nos formatos CSV e XLSX. | Baixa |

---

## 11. Requisitos Não-Funcionais (RNF)

Os RNFs definem as qualidades e restrições do sistema. Cada um possui critério de medição objetivo.

| ID | Nome | Categoria | Descrição | Prioridade |
|----|------|-----------|-----------|------------|
| RNF01 | Latência | Desempenho | 95% das requisições à API devem responder em menos de 500ms sob carga de até 100 usuários simultâneos. | Alta |
| RNF02 | Assincronismo | Desempenho | O backend deve retornar HTTP 202 em menos de 200ms para operações de escrita, sem aguardar a persistência no banco. | Alta |
| RNF03 | Tempo Real | Usabilidade | Notificações WebSocket devem chegar ao cliente em menos de 2 segundos após o evento ser publicado no **RabbitMQ**. | Alta |
| RNF04 | Disponibilidade | Confiabilidade | O sistema deve ter disponibilidade mínima de 99,5% ao mês (downtime máximo: ~3,6h/mês), excluindo janelas de manutenção programadas. | Alta |
| RNF05 | Persistência e Durabilidade | Dados | Nenhum evento publicado no **RabbitMQ deve ser perdido**: filas e exchanges marcadas como `durable`, mensagens com `persistent delivery mode`, **publisher confirms habilitados** no producer e **acknowledgement manual** nos consumers. Backup diário do MySQL com retenção de 30 dias. | Alta |
| RNF06 | Segurança — Autenticação | Segurança | JWT obrigatório em todos os endpoints privados. Tokens expiram em 1h. Refresh token em 7 dias. Bloqueio após 5 tentativas falhas (contador persistido por usuário). | Alta |
| RNF07 | Segurança — Transporte | Segurança | HTTPS/TLS 1.2+ obrigatório. HTTP deve ser redirecionado para HTTPS automaticamente. HSTS habilitado. | Alta |
| RNF08 | Segurança — OWASP | Segurança | Zero vulnerabilidades críticas ou altas no relatório SonarQube. Proteção contra SQLi, XSS e CSRF obrigatória. | Alta |
| RNF09 | LGPD | Conformidade | Dados pessoais (nome, e-mail) devem ser criptografados em repouso (AES-256). Usuário pode solicitar exclusão de conta e exportação de dados pessoais. | Alta |
| RNF10 | Manutenibilidade | Qualidade | Cobertura de testes unitários ≥ 80% no pacote `domain/` do backend. Zero issues bloqueantes no SonarQube. Complexidade ciclomática máxima por método: 10. | Média |
| RNF11 | Escalabilidade | Capacidade | O sistema deve suportar escalonamento horizontal do backend sem alterações de código (stateless). Consumers RabbitMQ devem ser adicionáveis sem downtime (concorrência controlada por `prefetch`). | Média |
| RNF12 | Observabilidade | Operação | Logs estruturados (JSON) com correlation ID. Métricas de latência, throughput e erros disponíveis em painel de monitoramento (Grafana ou equivalente). | Média |
| RNF13 | Usabilidade | UX | Interface responsiva para desktop e mobile (breakpoint mínimo: 375px). Tempo de carregamento inicial da página ≤ 3s em conexão 4G. | Média |

---

## 12. Regras de Negócio (RN)

As regras de negócio estabelecem as políticas e condições que o sistema deve respeitar, independentemente da interface ou canal de acesso. São invariantes do domínio.

| ID | Nome | Descrição |
|----|------|-----------|
| RN01 | Status Derivado da Etapa | O `StatusPedido` **não é armazenado como campo independente** — ele é uma função pura da última `TipoEtapa` registrada do pedido (ou da flag `cancelado`). A tabela de derivação é normativa e está documentada no Apêndice A. Não existe transição manual de status: o Administrador insere etapas (RF13), e o status acompanha automaticamente. |
| ~~RN02~~ | ~~Pré-requisito de Entrega~~ | **REMOVIDO na v1.1.** Esta regra perdeu sentido com a derivação automática (RN01). O status `ENTREGUE` agora só é alcançado quando a etapa `TipoEtapa.ENTREGUE` é registrada — o que pressupõe naturalmente todas as etapas anteriores. |
| RN03 | Desacoplamento Frontend | O frontend jamais deve aguardar a confirmação de escrita no banco de dados para liberar a interface. O backend deve sempre retornar HTTP 202 (Aceito) imediatamente após publicar o evento no RabbitMQ, garantindo responsividade da UI. |
| RN04 | Idempotência de Eventos | O reprocessamento de uma mensagem RabbitMQ já processada não deve gerar efeitos colaterais (duplicação de pedidos, etapas ou notificações). A implementação usa uma tabela `evento_processado` com constraint `UNIQUE(exchange, routing_key, message_id)`. O consumer **tenta INSERT primeiro**; se a constraint disparar, descarta a mensagem (acknowledge sem reprocessar). |
| RN05 | Imutabilidade de Eventos | Mensagens RabbitMQ publicadas são imutáveis e não devem ser alteradas retroativamente. Para corrigir um erro, um novo evento corretivo deve ser publicado. O histórico de eventos deve ser preservado integralmente. |
| RN06 | Unicidade de Código de Rastreamento | Cada pedido deve ter um código de rastreamento único por usuário. O mesmo código pode ser cadastrado por usuários diferentes (compras compartilhadas), mas não duas vezes pelo mesmo usuário. |
| RN07 | Cotação de Câmbio Fallback | Em caso de indisponibilidade da API de câmbio, o sistema deve usar a última cotação armazenada em cache, desde que não tenha mais de 24 horas. Cotações com mais de 24 horas devem ser sinalizadas como "desatualizada" na interface. |
| RN08 | Retenção de Dados Cancelados | Pedidos cancelados não devem ser excluídos do banco de dados. Devem ser mantidos por 12 meses para fins de auditoria e conformidade com a LGPD, após o que podem ser anonimizados. |
| RN09 | Limite de Notificações | O sistema deve manter no máximo 50 notificações por usuário no histórico. Notificações mais antigas devem ser removidas automaticamente (FIFO) quando o limite for atingido. A operação count + delete + insert deve ser atômica (transação serializável ou trigger no banco). |

### Apêndice A — Tabela de Derivação de Status (RN01)

A função `derivarStatus(ultimaEtapa, cancelado): StatusPedido` é normativa:

| Última `TipoEtapa` registrada | `cancelado` | `StatusPedido` derivado |
|------------------------------|-------------|-------------------------|
| (nenhuma) | `false` | `PROCESSANDO` |
| `NA_CHINA` | `false` | `PROCESSANDO` |
| `AEROPORTO_ORIGEM` | `false` | `ENVIADO` |
| `EM_TRANSITO` | `false` | `ENVIADO` |
| `AEROPORTO_DESTINO` | `false` | `ENVIADO` |
| `NO_BRASIL` | `false` | `ENVIADO` |
| `CD_BRASIL` | `false` | `ENVIADO` |
| `SAIDA_ENTREGA` | `false` | `ENVIADO` |
| `ENTREGUE` | `false` | `ENTREGUE` |
| (qualquer) | `true` | `CANCELADO` |

**Implicações:**
- Não existe endpoint `PATCH /api/pedidos/{id}/status`. A única forma de promover o status é registrando uma nova etapa via `POST /api/pedidos/{id}/etapas` (RF13).
- Para fins de query (dashboard, filtros), o backend pode manter uma coluna `status_cache` em `pedido`, **atualizada por listener interno após cada inserção de etapa**. Essa coluna é cache derivado, nunca fonte de verdade.

---

## 13. Casos de Uso (UC)

Os casos de uso descrevem as interações entre atores e o sistema em nível de comportamento observável. Seguem a notação textual estruturada (Cockburn).

### Diagrama de Atores

| Ator | Casos de Uso | Sistemas Externos Acionados |
|------|--------------|-----------------------------|
| Usuário (Cliente) | UC01 — Cadastrar Pedido<br>UC02 — Rastrear Pedido<br>UC03 — Receber Notificação em Tempo Real<br>UC04 — Visualizar Cotação<br>UC05 — Gerenciar Conta | API Correios (via UC02)<br>API Câmbio (via UC04) |
| Administrador | UC06 — Monitorar Dashboard<br>UC07 — Inserir Etapa Manual<br>UC08 — Gerenciar Usuários<br>UC09 — Definir Cotação Manual<br>UC10 — Exportar Dados | — |

---

### UC01 — Cadastrar Pedido

| Campo | Conteúdo |
|-------|----------|
| **Atores** | Usuário autenticado com perfil Cliente ou Administrador. |
| **Pré-condições** | Usuário está autenticado (JWT válido). Usuário possui menos de 200 pedidos ativos. |
| **Fluxo Principal** | 1. Usuário acessa a tela "Novo Pedido". 2. Sistema exibe o formulário de cadastro. 3. Usuário preenche: código de rastreamento (obrigatório), descrição, valor e moeda. 4. Usuário confirma o cadastro. 5. Sistema valida os dados (RN06 — unicidade do código). 6. Sistema publica evento `pedido.criado` no RabbitMQ. 7. Sistema retorna HTTP 202 e exibe mensagem de sucesso na UI. 8. Em segundo plano, o consumer RabbitMQ persiste o pedido no MySQL (sem etapas — o status derivado inicial é `PROCESSANDO`). 9. Sistema envia notificação WebSocket ao usuário confirmando a criação (RNF03). |
| **Fluxos Alternativos / Exceção** | **FA01:** Código de rastreamento já cadastrado pelo mesmo usuário → sistema exibe erro de validação (HTTP 422), pedido não é criado.<br>**FA02:** RabbitMQ indisponível → sistema retorna HTTP 503 e orienta o usuário a tentar novamente. *(Decisão técnica: nesta versão não foi adotado outbox pattern; em v2 a operação será sempre persistida localmente e publicada por worker.)*<br>**FA03:** Campos obrigatórios ausentes → sistema destaca os campos e bloqueia o envio. |
| **Pós-condições** | Pedido criado com status derivado `PROCESSANDO` (sem etapas registradas). Evento `pedido.criado` persistido no broker. Usuário notificado via WebSocket. |
| **Regras de Negócio** | RN03 (Desacoplamento), RN06 (Unicidade do código). |

---

### UC02 — Rastrear Pedido

| Campo | Conteúdo |
|-------|----------|
| **Atores** | Usuário autenticado (dono do pedido) ou Administrador (qualquer pedido). |
| **Pré-condições** | Usuário está autenticado. O pedido existe e pertence ao usuário (ou ator é Administrador). |
| **Fluxo Principal** | 1. Usuário acessa o detalhe de um pedido. 2. Sistema busca as etapas de rastreamento no MySQL. 3. Sistema calcula o status derivado a partir da última etapa (RN01). 4. Sistema exibe a linha do tempo com todas as etapas em ordem cronológica. 5. Sistema verifica se o pedido está em estado nacional (último `TipoEtapa` ∈ {`NO_BRASIL`, `CD_BRASIL`, `SAIDA_ENTREGA`}) e o último sync foi há mais de 6h; em caso afirmativo, dispara consulta à API dos Correios. 6. Novas etapas retornadas são persistidas e exibidas na linha do tempo. 7. Sistema exibe a cotação atual do valor do pedido em BRL. |
| **Fluxos Alternativos / Exceção** | **FA01:** API dos Correios indisponível → sistema exibe as últimas etapas conhecidas com aviso de que pode haver atualizações pendentes.<br>**FA02:** Pedido não pertence ao usuário (e ator não é Admin) → HTTP 403 Forbidden. |
| **Pós-condições** | Linha do tempo exibida e atualizada. Novas etapas persistidas se encontradas. |
| **Regras de Negócio** | RN01 (Status derivado), RN07 (Fallback de cotação). |

---

### UC03 — Receber Notificação em Tempo Real

| Campo | Conteúdo |
|-------|----------|
| **Atores** | Usuário autenticado com sessão WebSocket ativa. |
| **Pré-condições** | Usuário está autenticado e com WebSocket conectado. Um evento de atualização de pedido foi publicado no RabbitMQ. |
| **Fluxo Principal** | 1. Consumer RabbitMQ processa evento `pedido.atualizado` ou `rastreamento.atualizado`. 2. NotificacaoService identifica o usuário dono do pedido. 3. Sistema envia mensagem WebSocket ao canal privado do usuário (`/user/{userId}/queue/notificacoes`). 4. Frontend recebe a mensagem e exibe toast/banner de notificação. 5. Sistema persiste a notificação no histórico do usuário (respeitando RN09). 6. Badge de notificações não lidas é incrementado na UI. |
| **Fluxos Alternativos / Exceção** | **FA01:** Usuário não está conectado via WebSocket → notificação é persistida no histórico para ser vista no próximo login.<br>**FA02:** Envio WebSocket falha → sistema loga o erro, notificação continua no histórico. |
| **Pós-condições** | Usuário informado em tempo real (se conectado). Notificação registrada no histórico. Badge de não lidas atualizado. |
| **Regras de Negócio** | RN04 (Idempotência), RN09 (Limite de notificações). |

---

### UC07 — Inserir Etapa Manual (Administrador)

| Campo | Conteúdo |
|-------|----------|
| **Atores** | Administrador autenticado. |
| **Pré-condições** | Pedido existe. Ator possui perfil Administrador. |
| **Fluxo Principal** | 1. Admin acessa o detalhe do pedido. 2. Admin clica em "Inserir etapa manual". 3. Admin escolhe `TipoEtapa`, descrição livre, localização (opcional) e timestamp. 4. Sistema valida que a etapa não retroage (timestamp ≥ última etapa registrada). 5. Sistema publica evento `rastreamento.atualizado`. 6. Sistema retorna HTTP 202. 7. Consumer persiste a etapa e dispara o cálculo do novo status derivado. 8. Se o status derivado mudou, evento `pedido.atualizado` é publicado, gerando notificação ao dono. |
| **Fluxos Alternativos / Exceção** | **FA01:** Timestamp anterior à última etapa → HTTP 422 (etapas são append-only para preservar a derivação).<br>**FA02:** Tentativa de inserir etapa em pedido `cancelado` → HTTP 422. |
| **Pós-condições** | Etapa registrada. Status derivado recalculado. Notificação enviada ao dono se o status mudou. |
| **Regras de Negócio** | RN01 (Status derivado), RN05 (Imutabilidade de eventos). |

---

> **Outros casos de uso** (UC04, UC05, UC06, UC08, UC09, UC10) seguem a mesma estrutura e serão detalhados em iterações posteriores conforme prioridade.
