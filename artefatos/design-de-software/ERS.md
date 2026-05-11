# Especificação de Requisitos de Software (ERS / SRS)

**Sistema:** Importa Aí — Sistema de Gestão e Rastreamento de Encomendas Internacionais
**Versão:** 1.3
**Data:** 09 de Maio de 2026
**Autor:** Equipe Importa Aí

## Histórico de Revisão

| Data | Versão | Descrição | Autor |
|------|--------|-----------|-------|
| 19/03/2026 | 1.0 | Elaboração da primeira versão do documento | Higor Paulo Costa |
| 09/05/2026 | 1.1 | Substituição de Kafka por RabbitMQ; status do pedido passa a ser derivado das etapas; remoção do escopo de e-mail (cadastro, recuperação de senha e notificações por e-mail) | Higor Paulo Costa |
| 09/05/2026 | 1.2 | Refinamentos médios pós-auditoria: detalhamento da idempotência (INSERT-first), estratégia da RN09 (FIFO atômico), decisão sobre RabbitMQ indisponível (503 honesto), modelo de dados de autenticação (Apêndice B), plano de contingência da API dos Correios (Apêndice C), co-localização explícita do WebSocket no API Backend, alinhamento de moedas (CNY/USD/EUR ↔ BRL) | Higor Paulo Costa |
| 11/05/2026 | 1.3 | Detalhamento dos casos de uso UC04, UC05, UC06, UC08, UC09 e UC10 (anteriormente listados como "iteração posterior") | Higor Paulo Costa |

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
- A API dos Correios **pode** estar disponível. O sistema é resiliente a sua indisponibilidade via plano de contingência (ver Apêndice C). Em ambiente de desenvolvimento e demonstração, é usado um adapter `stub` que retorna etapas sintéticas.
- A API de Cotação de Câmbio estará disponível. Em caso de indisponibilidade, o sistema usará a última cotação armazenada em cache (RN07).
- O broker RabbitMQ estará disponível para receber publicações. Em caso de indisponibilidade, ver tratamento na UC01 (FA02).
- O endpoint STOMP/WebSocket é exposto pelo próprio API Backend (Spring Boot), no mesmo processo da API REST. Não há container WebSocket separado.
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
| RN03 | Desacoplamento Frontend | O frontend jamais deve aguardar a confirmação de escrita no banco de dados para liberar a interface. O backend deve sempre retornar HTTP 202 (Aceito) imediatamente após publicar o evento no RabbitMQ, garantindo responsividade da UI. **Exceção:** se o broker RabbitMQ estiver indisponível, o backend retorna HTTP 503 honesto (UC01 FA02) em vez de aceitar uma escrita que não pode garantir. O outbox pattern (que permitiria honrar RN03 mesmo com broker fora) é trabalho de v2 — fora do escopo da N2. |
| RN04 | Idempotência de Eventos | O reprocessamento de uma mensagem RabbitMQ já processada não deve gerar efeitos colaterais (duplicação de pedidos, etapas ou notificações). A implementação usa uma tabela `evento_processado` com constraint `UNIQUE(exchange, routing_key, message_id)`. O consumer **tenta INSERT primeiro**; se a constraint disparar, descarta a mensagem (acknowledge sem reprocessar). |
| RN05 | Imutabilidade de Eventos | Mensagens RabbitMQ publicadas são imutáveis e não devem ser alteradas retroativamente. Para corrigir um erro, um novo evento corretivo deve ser publicado. O histórico de eventos deve ser preservado integralmente. |
| RN06 | Unicidade de Código de Rastreamento | Cada pedido deve ter um código de rastreamento único por usuário. O mesmo código pode ser cadastrado por usuários diferentes (compras compartilhadas), mas não duas vezes pelo mesmo usuário. |
| RN07 | Cotação de Câmbio Fallback | Em caso de indisponibilidade da API de câmbio, o sistema deve usar a última cotação armazenada em cache, desde que não tenha mais de 24 horas. Cotações com mais de 24 horas devem ser sinalizadas como "desatualizada" na interface. |
| RN08 | Retenção de Dados Cancelados | Pedidos cancelados não devem ser excluídos do banco de dados. Devem ser mantidos por 12 meses para fins de auditoria e conformidade com a LGPD, após o que podem ser anonimizados. |
| RN09 | Limite de Notificações | O sistema deve manter no máximo 50 notificações por usuário no histórico. Notificações mais antigas são removidas automaticamente (FIFO) quando o limite é atingido — detalhes de implementação logo abaixo. |

**Implementação da RN09.** Um *trigger* `AFTER INSERT ON notificacao` no MySQL aplica a janela FIFO de forma atômica:

```sql
DELETE FROM notificacao
WHERE usuario_id = NEW.usuario_id
  AND id NOT IN (
    SELECT id FROM (
      SELECT id FROM notificacao
      WHERE usuario_id = NEW.usuario_id
      ORDER BY criado_em DESC
      LIMIT 50
    ) tmp
  );
```

Garante atomicidade dentro da mesma transação do `INSERT`, sem necessidade de *lock* pessimista na camada de aplicação.

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

### Apêndice B — Modelo de Dados de Autenticação (RF03, RNF06)

JWT é stateless por padrão, mas o sistema exige duas formas de estado server-side: revogação de refresh token (logout) e bloqueio temporário após 5 falhas. Esses dados são modelados em duas tabelas dedicadas.

#### Tabela `refresh_token_revogado` (suporta RF03)

| Coluna | Tipo | Descrição |
|--------|------|-----------|
| `id` | UUID | Identificador interno. |
| `token_hash` | VARCHAR(64) | SHA-256 do refresh token. **Nunca armazenar o token em claro.** |
| `usuario_id` | UUID FK | Dono do token. |
| `revogado_em` | TIMESTAMP | Momento da revogação. |
| `expira_em` | TIMESTAMP | Cópia da expiração original do token (para limpeza). |

- **Índice único:** `token_hash`.
- **Job de limpeza:** registros com `expira_em < NOW()` podem ser removidos (não há benefício em manter token expirado em blocklist).
- **Fluxo de validação no refresh:** ao receber um refresh token, calcula-se o hash e consulta-se a tabela. Se houver match, token recusado (HTTP 401).

#### Tabela `tentativa_login_falha` (suporta RNF06)

| Coluna | Tipo | Descrição |
|--------|------|-----------|
| `email` | VARCHAR(255) PK | Identificador da tentativa (chave). |
| `contador` | INT | Número de falhas consecutivas. |
| `bloqueado_ate` | TIMESTAMP NULL | Quando a conta volta a aceitar login. NULL = não bloqueada. |
| `atualizado_em` | TIMESTAMP | Última atualização (para reset após N minutos sem tentativa). |

- **Lógica:** a cada login falho, incrementa `contador`. Ao atingir 5, define `bloqueado_ate = NOW() + 15 min`. Login bem-sucedido zera o contador.
- **Verificação:** antes de validar credenciais, checa se `bloqueado_ate > NOW()` → HTTP 429.
- **Por que pela `email` e não `usuario_id`?** Para bloquear tentativas com e-mails inexistentes também (defesa contra enumeração de usuários).

### Apêndice C — Plano de Contingência da API dos Correios (M8)

A API dos Correios não possui contrato REST público estável (o antigo SRO foi descontinuado e a nova API exige contrato comercial). Para mitigar esse risco, o `RastreamentoCorreiosPort` (porta de saída do domínio) tem três adapters intercambiáveis selecionados por configuração:

| Adapter | Quando ativar | Comportamento |
|---------|---------------|---------------|
| `CorreiosStubAdapter` | Desenvolvimento e ambiente de demonstração (default em `dev`) | Retorna etapas sintéticas progressivas com base no tempo decorrido desde o cadastro do pedido. Permite simular o ciclo completo sem dependência externa. |
| `CorreiosHttpAdapter` | Produção (se contrato disponível) | Chama a API real. Implementa **Circuit Breaker** (Resilience4j): após 5 falhas consecutivas, abre o circuito por 60s e usa o último cache conhecido. |
| `CorreiosCacheOnlyAdapter` | Fallback explícito | Usa só o cache local. Mensagem de UI: "atualização indisponível, exibindo última informação conhecida". |

**Seleção:** propriedade `correios.adapter` em `application.properties` (`stub` | `http` | `cache-only`).

**Implicação:** mesmo sem acesso à API real, o fluxo de rastreamento funciona ponta-a-ponta com dados sintéticos. A indisponibilidade da API externa é tratada pela arquitetura (Hexagonal + Strategy/Decorator de adapters), sem impacto no domínio.

---

## 13. Casos de Uso (UC)

Os casos de uso descrevem as interações entre atores e o sistema em nível de comportamento observável. Seguem a notação textual estruturada (Cockburn).

### Atores e seus Casos de Uso

#### Usuário (Cliente)

- UC01 — Cadastrar Pedido
- UC02 — Rastrear Pedido
- UC03 — Receber Notificação em Tempo Real
- UC04 — Visualizar Cotação
- UC05 — Gerenciar Conta

*Sistemas externos acionados:* API Correios (via UC02), API Câmbio (via UC04).

#### Administrador

- UC06 — Monitorar Dashboard
- UC07 — Inserir Etapa Manual
- UC08 — Gerenciar Usuários
- UC09 — Definir Cotação Manual
- UC10 — Exportar Dados

*Sistemas externos acionados:* nenhum diretamente.

---

### UC01 — Cadastrar Pedido

**Atores:** Usuário autenticado com perfil Cliente ou Administrador.

**Pré-condições:**

- Usuário está autenticado (JWT válido).
- Usuário possui menos de 200 pedidos ativos.

**Fluxo Principal:**

1. Usuário acessa a tela "Novo Pedido".
2. Sistema exibe o formulário de cadastro.
3. Usuário preenche: código de rastreamento (obrigatório), descrição, valor e moeda.
4. Usuário confirma o cadastro.
5. Sistema valida os dados (RN06 — unicidade do código).
6. Sistema publica evento `pedido.criado` no RabbitMQ.
7. Sistema retorna HTTP 202 e exibe mensagem de sucesso na UI.
8. Em segundo plano, o consumer RabbitMQ persiste o pedido no MySQL (sem etapas — o status derivado inicial é `PROCESSANDO`).
9. Sistema envia notificação WebSocket ao usuário confirmando a criação (RNF03).

**Fluxos Alternativos / Exceção:**

- **FA01** — Código de rastreamento já cadastrado pelo mesmo usuário → sistema exibe erro de validação (HTTP 422); pedido não é criado.
- **FA02** — RabbitMQ indisponível → sistema retorna HTTP 503 e orienta o usuário a tentar novamente. *(Decisão técnica: nesta versão não foi adotado outbox pattern; em v2 a operação será sempre persistida localmente e publicada por worker.)*
- **FA03** — Campos obrigatórios ausentes → sistema destaca os campos e bloqueia o envio.

**Pós-condições:** Pedido criado com status derivado `PROCESSANDO` (sem etapas registradas). Evento `pedido.criado` persistido no broker. Usuário notificado via WebSocket.

**Regras de Negócio:** RN03 (Desacoplamento), RN06 (Unicidade do código).

---

### UC02 — Rastrear Pedido

**Atores:** Usuário autenticado (dono do pedido) ou Administrador (qualquer pedido).

**Pré-condições:** Usuário está autenticado. O pedido existe e pertence ao usuário (ou o ator é Administrador).

**Fluxo Principal:**

1. Usuário acessa o detalhe de um pedido.
2. Sistema busca as etapas de rastreamento no MySQL.
3. Sistema calcula o status derivado a partir da última etapa (RN01).
4. Sistema exibe a linha do tempo com todas as etapas em ordem cronológica.
5. Sistema verifica se o pedido está em estado nacional (último `TipoEtapa` ∈ {`NO_BRASIL`, `CD_BRASIL`, `SAIDA_ENTREGA`}) e o último *sync* foi há mais de 6 h; em caso afirmativo, dispara consulta à API dos Correios.
6. Novas etapas retornadas são persistidas e exibidas na linha do tempo.
7. Sistema exibe a cotação atual do valor do pedido em BRL.

**Fluxos Alternativos / Exceção:**

- **FA01** — API dos Correios indisponível → sistema exibe as últimas etapas conhecidas com aviso de que pode haver atualizações pendentes.
- **FA02** — Pedido não pertence ao usuário e o ator não é Administrador → HTTP 403 *Forbidden*.

**Pós-condições:** Linha do tempo exibida e atualizada. Novas etapas persistidas se encontradas.

**Regras de Negócio:** RN01 (Status derivado), RN07 (Fallback de cotação).

---

### UC03 — Receber Notificação em Tempo Real

**Atores:** Usuário autenticado com sessão WebSocket ativa.

**Pré-condições:** Usuário está autenticado e com WebSocket conectado. Um evento de atualização de pedido foi publicado no RabbitMQ.

**Fluxo Principal:**

1. Consumer RabbitMQ processa o evento `pedido.atualizado` ou `rastreamento.atualizado`.
2. `NotificacaoService` identifica o usuário dono do pedido.
3. Sistema envia mensagem WebSocket ao canal privado do usuário (`/user/{userId}/queue/notificacoes`).
4. Frontend recebe a mensagem e exibe *toast*/banner de notificação.
5. Sistema persiste a notificação no histórico do usuário (respeitando RN09).
6. *Badge* de notificações não lidas é incrementado na UI.

**Fluxos Alternativos / Exceção:**

- **FA01** — Usuário não está conectado via WebSocket → notificação é persistida no histórico para ser vista no próximo login.
- **FA02** — Envio WebSocket falha → sistema loga o erro; notificação continua no histórico.

**Pós-condições:** Usuário informado em tempo real (se conectado). Notificação registrada no histórico. *Badge* de não lidas atualizado.

**Regras de Negócio:** RN04 (Idempotência), RN09 (Limite de notificações).

---

### UC07 — Inserir Etapa Manual (Administrador)

**Atores:** Administrador autenticado.

**Pré-condições:** Pedido existe. O ator possui perfil Administrador.

**Fluxo Principal:**

1. Admin acessa o detalhe do pedido.
2. Admin clica em "Inserir etapa manual".
3. Admin escolhe `TipoEtapa`, descrição livre, localização (opcional) e *timestamp*.
4. Sistema valida que a etapa não retroage (`timestamp` ≥ última etapa registrada).
5. Sistema publica o evento `rastreamento.atualizado`.
6. Sistema retorna HTTP 202.
7. Consumer persiste a etapa e dispara o cálculo do novo status derivado.
8. Se o status derivado mudou, o evento `pedido.atualizado` é publicado, gerando notificação ao dono.

**Fluxos Alternativos / Exceção:**

- **FA01** — *Timestamp* anterior à última etapa → HTTP 422 (etapas são *append-only* para preservar a derivação).
- **FA02** — Tentativa de inserir etapa em pedido `cancelado` → HTTP 422.

**Pós-condições:** Etapa registrada. Status derivado recalculado. Notificação enviada ao dono se o status mudou.

**Regras de Negócio:** RN01 (Status derivado), RN05 (Imutabilidade de eventos).

---

### UC04 — Visualizar Cotação

**Atores:** Usuário autenticado (Cliente ou Administrador).

**Pré-condições:** Usuário está autenticado. O pedido existe e o usuário tem permissão de leitura sobre ele (dono ou Admin).

**Fluxo Principal:**

1. Usuário acessa o detalhe de um pedido (ou abre a tela de Cotação).
2. Sistema identifica o par de moedas relevante (moeda de origem do pedido → BRL).
3. Sistema lê a cotação atual em *cache* (RF20).
4. Sistema verifica se a cotação tem mais de 24 h.
5. Sistema calcula o valor convertido (`valor_declarado × cotação`).
6. Sistema exibe o valor convertido junto ao valor original, com indicação do par de moedas, da data/hora da cotação e da fonte (automática ou manual — ver UC09).

**Fluxos Alternativos / Exceção:**

- **FA01** — Cotação em *cache* com mais de 24 h → sistema exibe o valor convertido com *flag* "cotação desatualizada" e *tooltip* explicativo (RN07).
- **FA02** — Sem cotação em *cache* (primeiro uso da moeda no sistema) → sistema dispara consulta síncrona à API de câmbio. Em caso de sucesso, atualiza o *cache* e exibe; em caso de falha, exibe o valor original sem conversão e a mensagem "Cotação indisponível".
- **FA03** — Existe cotação manual ativa para o par (UC09) → sistema usa a cotação manual em vez da automática e sinaliza visualmente (`manual=true`).

**Pós-condições:** Valor convertido exibido. *Cache* de cotação eventualmente atualizado (assíncrono).

**Regras de Negócio:** RN07 (fallback de cotação).

---

### UC05 — Gerenciar Conta

**Atores:** Usuário autenticado (Cliente).

**Pré-condições:** Usuário está autenticado.

**Fluxo Principal:**

1. Usuário acessa "Minha Conta".
2. Sistema exibe dados pessoais (nome, e-mail) e opções de alterar senha, exportar dados pessoais e solicitar exclusão de conta (LGPD).
3. Usuário edita os dados ou aciona uma das opções.
4. Sistema valida:
   - unicidade do novo e-mail (se alterado);
   - senha atual correta (se a troca de senha for solicitada);
   - força mínima da nova senha (mesmos critérios de RF01).
5. Sistema persiste as alterações.
6. Sistema confirma na UI. Em caso de troca de senha, *refresh tokens* existentes são revogados (RF03).

**Fluxos Alternativos / Exceção:**

- **FA01** — Senha atual incorreta → HTTP 401 + mensagem clara; bloqueio temporário se atingir 5 falhas (RNF06).
- **FA02** — E-mail novo já em uso → HTTP 422 + mensagem.
- **FA03** — Usuário solicita exportação de dados pessoais → sistema gera arquivo (CSV ou JSON) com seus dados e disponibiliza para *download* (RNF09).
- **FA04** — Usuário solicita exclusão de conta → conta é marcada como inativa (*soft delete*); dados anonimizados após 12 meses (RN08, RNF09).

**Pós-condições:** Dados atualizados. Em caso de troca de senha, sessões antigas precisam refazer *login*.

**Regras de Negócio:** RN08 (retenção e anonimização para LGPD), RNF06 (segurança da autenticação), RNF09 (LGPD).

---

### UC06 — Monitorar Dashboard (Administrador)

**Atores:** Administrador.

**Pré-condições:** Usuário autenticado com perfil Administrador.

**Fluxo Principal:**

1. Admin acessa a rota `/admin/dashboard`.
2. Sistema consulta, em paralelo:
   - total de pedidos ativos, agregado por status (RF22);
   - pedidos com taxa aduaneira pendente;
   - pedidos entregues no mês corrente;
   - evolução do volume de pedidos nos últimos 30 dias por status (RF23);
   - 10 pedidos mais recentemente atualizados (RF24).
3. Sistema renderiza *cards* de KPI e o gráfico de evolução.
4. Admin pode filtrar o gráfico por status.
5. Admin pode clicar em qualquer pedido da lista "Recentes" para abrir o caso de uso UC02 (Rastrear Pedido).

**Fluxos Alternativos / Exceção:**

- **FA01** — Uma métrica individual falha (*timeout* em *query* agregada, indisponibilidade temporária) → sistema exibe os *cards* disponíveis e marca os indisponíveis com `—` + ícone de aviso. O *dashboard* nunca falha por completo.
- **FA02** — Admin sem permissão de visualizar uma métrica específica (em futura expansão de RBAC) → métrica oculta sem erro.

**Pós-condições:** KPIs e gráficos exibidos. Sem efeito colateral persistente.

**Regras de Negócio:** RN01 (o status derivado é base para os agregados); demais agregam dados já existentes.

---

### UC08 — Gerenciar Usuários (Administrador)

**Atores:** Administrador.

**Pré-condições:** Usuário autenticado com perfil Administrador.

**Fluxo Principal:**

1. Admin acessa `/admin/usuarios`.
2. Sistema lista usuários com paginação (20 por página), busca por nome ou e-mail e filtros por perfil e status (ativo/inativo).
3. Para cada usuário, o Admin pode:
   - promover Cliente → Administrador (ou rebaixar Administrador → Cliente);
   - desativar / reativar (*soft delete*; nunca exclusão física);
   - redefinir senha (RF25 — substituto operacional do RF04 removido).
4. Sistema valida cada ação:
   - Admin não pode rebaixar a si mesmo (proteção operacional);
   - Sistema não permite a operação que deixaria nenhum Admin ativo.
5. Para redefinição de senha, o sistema gera uma senha temporária forte, exibe uma única vez (não persiste em claro) e marca a conta com *flag* "senha temporária" — no próximo *login*, o usuário é obrigado a trocar a senha.
6. Sistema persiste e confirma. Evento de auditoria registrado.

**Fluxos Alternativos / Exceção:**

- **FA01** — Tentativa de rebaixar o último Admin ativo → HTTP 422 + mensagem.
- **FA02** — Tentativa de rebaixar a si mesmo → HTTP 403 + mensagem.
- **FA03** — Tentativa de exclusão definitiva (DELETE permanente) → não permitido; apenas *soft delete*.
- **FA04** — Redefinição de senha com falha de persistência → senha temporária não é exibida (operação atômica).

**Pós-condições:** Perfil, estado ou senha do usuário atualizados. *Refresh tokens* do usuário podem ser revogados (em caso de troca de senha). Evento de auditoria registrado.

**Regras de Negócio:** RF05 (RBAC), RF25 (redefinição de senha pelo Admin), RNF06 (segurança), RNF09 (LGPD).

---

### UC09 — Definir Cotação Manual (Administrador)

**Atores:** Administrador.

**Pré-condições:** Usuário autenticado com perfil Administrador.

**Fluxo Principal:**

1. Admin acessa "Cotação Manual" no painel administrativo.
2. Admin escolhe o par de moedas (CNY/BRL, USD/BRL ou EUR/BRL).
3. Admin informa a taxa de câmbio (decimal positivo) e, opcionalmente, uma data de validade.
4. Sistema valida:
   - taxa positiva e não-zero;
   - taxa dentro de *range* plausível (configurável; *default* ±50 % sobre a última cotação automática);
   - se a data de validade for informada, deve ser futura.
5. Sistema persiste a cotação como tipo `manual`, sobrescrevendo a automática para o par escolhido.
6. Sistema sinaliza visualmente em toda a UI (*badge* "M" ou ícone "manual") que a cotação é manual (RF21).

**Fluxos Alternativos / Exceção:**

- **FA01** — Taxa fora do *range* plausível → HTTP 422; Admin precisa marcar "Confirmar valor incomum" para prosseguir (proteção contra erro de digitação).
- **FA02** — Admin remove a cotação manual → sistema volta a usar a automática; UI deixa de exibir o marcador "manual".
- **FA03** — Cotação manual com data de validade vencida → cotação é desativada automaticamente e o sistema retorna ao uso da automática.

**Pós-condições:** Cotação manual ativa para o par. Toda exibição do UC04 passa a usar a manual até remoção.

**Regras de Negócio:** RF21 (cotação manual), RN07 (*fallback* de cotação — a manual sempre prevalece sobre a automática enquanto ativa).

---

### UC10 — Exportar Dados (Administrador)

**Atores:** Administrador.

**Pré-condições:** Usuário autenticado com perfil Administrador.

**Fluxo Principal:**

1. Admin acessa "Exportar Pedidos" no painel.
2. Admin aplica filtros (status, período de criação, faixa de valor, usuário, etc.).
3. Admin escolhe o formato de saída (CSV ou XLSX).
4. Sistema estima a quantidade de registros que correspondem aos filtros.
5. Geração do arquivo:
   - **resultados pequenos** (< 5.000 linhas): sistema gera o arquivo de forma síncrona e o retorna como *download* imediato;
   - **resultados grandes** (≥ 5.000 linhas): sistema enfileira um *job* assíncrono, exibe progresso e envia notificação (UC03) ao Admin quando o arquivo estiver pronto.
6. Admin baixa o arquivo. Evento de exportação registrado para auditoria (quem exportou, quando, quais filtros, quantos registros).

**Fluxos Alternativos / Exceção:**

- **FA01** — Filtros resultam em 0 registros → sistema exibe alerta e não gera arquivo.
- **FA02** — *Job* assíncrono falha → sistema notifica o Admin com mensagem e oferece opção "tentar novamente".
- **FA03** — Resultado excede o limite máximo permitido (configurável; *default* 100.000 linhas) → sistema sugere refinar os filtros e bloqueia a exportação.
- **FA04** — Tentativa de exportar dados de outro usuário sem permissão administrativa adequada → HTTP 403.

**Pós-condições:** Arquivo disponível para *download* (síncrono ou assíncrono). *Log* de exportação registrado.

**Regras de Negócio:** RF26 (exportação), RNF09 (LGPD — toda exportação que contenha dados pessoais é auditada).
