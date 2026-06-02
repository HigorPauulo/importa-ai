# Modelo de Dados — Importa Aí

**Versão:** 1.1
**Data:** 31 de Maio de 2026
**Autor:** Equipe Importa Aí

## Histórico de Revisão

| Data | Versão | Descrição | Autor |
|------|--------|-----------|-------|
| 11/05/2026 | 1.0 (rascunho) | Esquema inicial das tabelas de suporte técnico (autenticação, idempotência, trigger RN09) | Higor Paulo Costa |
| 12/05/2026 | 1.0 | Publicação: incluído §4 com DER completo das entidades de negócio (`usuario`, `pedido` com `status_cache`, `etapa_rastreamento` append-only, `notificacao`, `cotacao_cache`); referência ao ADR-005 para criptografia de PII | Higor Paulo Costa |
| 31/05/2026 | 1.1 | Nota de status em §4.1 `usuario`: cifragem de PII e `email_hash` são alvo do ADR-005, não implementados na V1 (texto claro) — dívida v2 | Higor Paulo Costa |

## Propósito

Este documento descreve o esquema relacional do banco de dados do sistema. Contém:

1. O **esquema das entidades de negócio** (§4) — pedido, usuário, etapa de rastreamento, notificação e cotação.
2. O **esquema das tabelas de suporte técnico** (§1–§3) — autenticação stateless, idempotência de mensageria, FIFO de notificações.

O Diagrama Entidade-Relacionamento (DER) visual está em [`der.drawio`](der.drawio) neste mesmo diretório.

---

## 1. Tabelas de suporte à autenticação

Apoiam as regras RF03 (revogação de *refresh token*) e RNF06 (bloqueio após 5 falhas de *login*). JWT é *stateless* por padrão, mas o sistema exige duas formas de estado *server-side*.

### 1.1 `refresh_token_revogado`

| Coluna | Tipo | Descrição |
|--------|------|-----------|
| `id` | BIGINT AUTO_INCREMENT (PK) | Identificador interno. |
| `token_hash` | CHAR(64) | SHA-256 do *refresh token*. **Nunca armazenar o token em claro.** |
| `usuario_id` | BIGINT FK | Dono do token. |
| `revogado_em` | TIMESTAMP | Momento da revogação. |
| `expira_em` | TIMESTAMP | Cópia da expiração original do token (para limpeza). |

- **Índice único:** `token_hash`.
- **Job de limpeza:** registros com `expira_em < NOW()` podem ser removidos.
- **Fluxo de validação no *refresh*:** ao receber um *refresh token*, calcula-se o *hash* e consulta-se a tabela. Se houver *match*, o token é recusado (HTTP 401).

### 1.2 `tentativa_login_falha`

| Coluna | Tipo | Descrição |
|--------|------|-----------|
| `email` | VARCHAR(255) PK | Identificador da tentativa. |
| `contador` | INT | Número de falhas consecutivas. |
| `bloqueado_ate` | TIMESTAMP NULL | Quando a conta volta a aceitar *login*. `NULL` = não bloqueada. |
| `atualizado_em` | TIMESTAMP | Última atualização (para *reset* após N minutos sem tentativa). |

- **Lógica:** a cada *login* falho, incrementa `contador`. Ao atingir 5, define `bloqueado_ate = NOW() + 15 min`. *Login* bem-sucedido zera o contador.
- **Verificação:** antes de validar credenciais, checa se `bloqueado_ate > NOW()` → HTTP 429.
- **Por que pelo `email` e não `usuario_id`?** Para bloquear tentativas com e-mails inexistentes (defesa contra enumeração de usuários).

---

## 2. Tabela de suporte à idempotência (RN04)

Apoia a regra RN04. Detalhes operacionais da estratégia *INSERT-first*: ver [Arquitetura de Mensageria §7](../mensageria-e-streams/arquitetura-mensageria.md).

### 2.1 `evento_processado`

| Coluna | Tipo | Descrição |
|--------|------|-----------|
| `id` | BIGINT AUTO_INCREMENT | PK técnica. |
| `exchange` | VARCHAR(100) NOT NULL | Origem da mensagem (AMQP). |
| `routing_key` | VARCHAR(100) NOT NULL | *Routing key* da mensagem. |
| `message_id` | VARCHAR(36) NOT NULL | UUID gerado pelo *producer*. |
| `processado_em` | TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP | Auditoria e limpeza. |

- **Constraint chave:** `UNIQUE (exchange, routing_key, message_id)`.
- **Job de limpeza:** remover registros com `processado_em < NOW() - INTERVAL 30 DAY`.

---

## 3. Aplicação da RN09 (limite FIFO de 50)

A janela FIFO de no máximo 50 notificações por usuário é aplicada na **camada de aplicação**, dentro do caso de uso `PersistirNotificacao`: o `INSERT` da nova notificação e o `DELETE` das que excedem o limite ocorrem na mesma transação, com `SELECT ... FOR UPDATE` por `usuario_id` para serializar inserções concorrentes do mesmo usuário.

A regra **não** é implementada por *trigger* SQL. Motivos:

- **Coerência arquitetural:** mantém a regra de negócio no núcleo da aplicação, como a RN01 (status derivado), em vez de escondê-la no banco.
- **Menor privilégio:** `CREATE TRIGGER` exige `SUPER` no MySQL 8 com *binary logging* habilitado (default); o usuário de aplicação não o possui.

A migration `V4` cria apenas a tabela `notificacao` e seus índices.

---

## 4. Entidades de negócio (DER)

Esquema MySQL 8.x. Convenção: `snake_case` para nomes; `id BIGINT AUTO_INCREMENT` como PK técnica em todas as entidades. Todos os timestamps em UTC. *Charset* `utf8mb4` / *collation* `utf8mb4_0900_ai_ci`.

### 4.1 `usuario`

| Coluna | Tipo | Descrição |
|--------|------|-----------|
| `id` | BIGINT AUTO_INCREMENT (PK) | Identificador interno. |
| `email` | VARBINARY(255) NOT NULL | E-mail. Criptografado em repouso na camada de aplicação (AES-256-GCM + IV por linha — ADR-005). |
| `email_hash` | CHAR(64) NOT NULL | HMAC-SHA256 determinístico do e-mail. Suporta busca exata sem decriptar (ADR-005). |
| `senha_hash` | VARCHAR(60) NOT NULL | BCrypt (cost ≥ 12) da senha. |
| `nome_completo` | VARCHAR(200) NOT NULL | Nome do usuário. Criptografado em repouso na camada de aplicação (AES-256-GCM — ADR-005). |
| `perfil` | ENUM('CLIENTE','ADMINISTRADOR') NOT NULL DEFAULT 'CLIENTE' | RBAC — RF05. |
| `ativo` | BOOLEAN NOT NULL DEFAULT TRUE | *Soft delete* — RF25. |
| `criado_em` | TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP | — |
| `atualizado_em` | TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | — |

- **UNIQUE:** `email_hash` — unicidade e busca rápida.
- **INDEX:** `(perfil, ativo)` — listagens do RF25.

> **Status de implementação (2026-05-31):** a cifragem de PII (`email`/`nome_completo` em `VARBINARY`) e o campo `email_hash` acima são o **alvo do ADR-005 e ainda não foram implementados**. A migration vigente (`V1`) grava `email` e `nome` em texto claro (`VARCHAR`), com `UNIQUE (email)` e sem `email_hash`. A coluna `atualizado_em` também não existe no `usuario` nesta versão (só `criado_em`; `ativo` foi adicionado em V8). Dívida técnica para a próxima versão (ERS RNF09).

### 4.2 `pedido`

| Coluna | Tipo | Descrição |
|--------|------|-----------|
| `id` | BIGINT AUTO_INCREMENT (PK) | — |
| `usuario_id` | BIGINT NOT NULL | FK → `usuario(id)`. |
| `codigo_rastreamento` | VARCHAR(50) NOT NULL | Código do envio (Correios ou equivalente). |
| `descricao` | VARCHAR(500) NULL | — |
| `valor_declarado` | DECIMAL(15,2) NOT NULL | Valor na moeda de origem. |
| `moeda` | CHAR(3) NOT NULL | `CNY` / `USD` / `EUR`. |
| `cancelado` | BOOLEAN NOT NULL DEFAULT FALSE | Flag usada na derivação de `StatusPedido` (RN01). |
| `rastreio_nao_localizado` | BOOLEAN NOT NULL DEFAULT FALSE | Fonte de rastreamento não encontrou o código; a UI exibe aviso. (V10) |
| `status_cache` | ENUM('PROCESSANDO','ENVIADO','ENTREGUE','DEVOLVIDO','CANCELADO') NOT NULL DEFAULT 'PROCESSANDO' | **Cache derivado** (RN01, [ADR-003](../design-de-software/adrs/003-status-derivado-da-etapa.md)). Atualizado por *listener* após cada inserção de etapa. **Nunca é fonte de verdade.** Pode ser reconstruído da tabela `etapa_rastreamento` a qualquer momento. |
| `estimado_entrega` | DATE NULL | Data estimada (opcional). |
| `criado_em` | TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP | — |
| `atualizado_em` | TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | — |

- **UNIQUE:** `(usuario_id, codigo_rastreamento)` — RN06.
- **FK:** `usuario_id` → `usuario(id)` `ON DELETE RESTRICT`.
- **INDEX:** `(usuario_id, criado_em DESC)` — listagem ordenada por mais recente (RF07).

> **Status de implementação:** o schema real do `pedido` (migrations V1 + V6 + V10) tem `valor_declarado` e `moeda` (V6) e `rastreio_nao_localizado` (V10). **Não** possui `status_cache`, `estimado_entrega` nem `atualizado_em` nesta versão — o `StatusPedido` é **derivado on-the-fly** da última etapa (sem coluna de cache, sem o índice `(status_cache)`). A coluna `status_cache` e seu índice permanecem como dívida v2 (ADR-003).

### 4.3 `etapa_rastreamento`

Entidade dentro do agregado `Pedido`. **Append-only por `criado_em`** (RF13, ADR-003).

| Coluna | Tipo | Descrição |
|--------|------|-----------|
| `id` | BIGINT AUTO_INCREMENT (PK) | — |
| `pedido_id` | BIGINT NOT NULL | FK → `pedido(id)`. |
| `tipo` | ENUM('NA_CHINA','AEROPORTO_ORIGEM','EM_TRANSITO','AEROPORTO_DESTINO','NO_BRASIL','TAXA','CD_BRASIL','SAIDA_ENTREGA','ENTREGUE','DEVOLVIDO') NOT NULL | Sequência cronológica esperada (RF12). `DEVOLVIDO` é o estado terminal de exceção (pacote barrado/devolvido pela alfândega). |
| `localizacao` | VARCHAR(255) NULL | — |
| `descricao` | VARCHAR(500) NULL | — |
| `criado_em` | TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) | *Timestamp* do evento (precisão de milissegundos). |

- **FK:** `pedido_id` → `pedido(id)` `ON DELETE CASCADE` — etapas pertencem ao agregado; se o pedido for excluído fisicamente (não usual; ver RN08), suas etapas vão junto.
- **UNIQUE:** `(pedido_id, criado_em)` — reforça *append-only* (impede duplicação ou reescrita) e garante ordenação determinística.
- **INDEX:** `(pedido_id, criado_em DESC)` — recupera a última etapa em O(log n), crítico para o cálculo do status derivado (RN01).

### 4.4 `notificacao`

| Coluna | Tipo | Descrição |
|--------|------|-----------|
| `id` | BIGINT AUTO_INCREMENT (PK) | — |
| `usuario_id` | BIGINT NOT NULL | FK → `usuario(id)`. |
| `pedido_id` | BIGINT NULL | FK → `pedido(id)`, opcional (notificação pode não estar atrelada a um pedido). |
| `mensagem` | VARCHAR(500) NOT NULL | — |
| `lida` | BOOLEAN NOT NULL DEFAULT FALSE | — |
| `criado_em` | TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP | — |

- **FK:** `usuario_id` → `usuario(id)` `ON DELETE CASCADE`.
- **FK:** `pedido_id` → `pedido(id)` `ON DELETE SET NULL`.
- **INDEX:** `(usuario_id, lida, criado_em DESC)` — listagem + badge de não lidas.
- **RN09:** limite FIFO de 50 itens por usuário aplicado na camada de aplicação (§3), não por *trigger*.

### 4.5 `cotacao_cache`

| Coluna | Tipo | Descrição |
|--------|------|-----------|
| `id` | BIGINT AUTO_INCREMENT (PK) | — |
| `moeda_origem` | CHAR(3) NOT NULL | `CNY` / `USD` / `EUR`. |
| `moeda_destino` | CHAR(3) NOT NULL DEFAULT 'BRL' | — |
| `taxa` | DECIMAL(12,6) NOT NULL | Valor de 1 unidade da moeda de origem em destino. |
| `fonte` | ENUM('AUTOMATICA','MANUAL') NOT NULL DEFAULT 'AUTOMATICA' | RF20 / RF21. |
| `manual_por_usuario_id` | BIGINT NULL | FK → `usuario(id)`. Preenchido apenas quando `fonte = 'MANUAL'`. |
| `valido_ate` | TIMESTAMP NULL | Validade opcional para cotação manual (UC09 FA03). |
| `cotado_em` | TIMESTAMP NULL | Momento informado pela fonte de quando a cotação foi feita — alimenta o "há X min" na UI; distinto de `atualizado_em` (momento do *sync*/TTL). (V9) |
| `atualizado_em` | TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | — |

- **FK:** `manual_por_usuario_id` → `usuario(id)` `ON DELETE SET NULL`.
- **INDEX:** `(moeda_origem, moeda_destino, atualizado_em DESC)` — última cotação de um par.

### 4.6 Relacionamentos e cardinalidades

| Origem | Destino | Cardinalidade | Política | Justificativa |
|--------|---------|---------------|----------|---------------|
| `pedido.usuario_id` | `usuario.id` | N : 1 | `RESTRICT` | Não excluir usuário com pedidos ativos (LGPD: ver RN08 — soft delete). |
| `etapa_rastreamento.pedido_id` | `pedido.id` | N : 1 | `CASCADE` | Composição: etapas vivem dentro do agregado Pedido. |
| `notificacao.usuario_id` | `usuario.id` | N : 1 | `CASCADE` | Notificações pertencem ao usuário. |
| `notificacao.pedido_id` | `pedido.id` | N : 0..1 | `SET NULL` | Notificação pode existir sem pedido (ex.: mensagens administrativas). |
| `cotacao_cache.manual_por_usuario_id` | `usuario.id` | N : 0..1 | `SET NULL` | Histórico da cotação manual preservado mesmo se o admin for desativado. |

> **DER visual:** ver [`der.drawio`](der.drawio).

---

## 5. Pendências (próximas iterações)

- Estratégia de **arquivamento** de pedidos com `cancelado = TRUE` após 12 meses (RN08 / LGPD).
- Avaliar **particionamento** de `etapa_rastreamento` por intervalo de `criado_em` se o volume crescer.
- **Job de reconciliação** de `pedido.status_cache` (revalidação periódica contra `etapa_rastreamento` para detectar divergências).
- Trigger SQL de manutenção do `status_cache` após `INSERT` em `etapa_rastreamento` — alternativa ao *listener* da aplicação (ADR-003).

## Risco residual e mitigação — bloqueio por e-mail (RNF06)

A tabela `tentativa_login_falha` (§1.2) usa `email` como chave de bloqueio para evitar enumeração de usuários inexistentes. Trade-off conhecido: um atacante pode **bloquear o login de qualquer e-mail-alvo** disparando 5 tentativas falhas seguidas (DoS direcionado).

Mitigações previstas (não implementadas nesta versão; ver v2):

- *Rate-limit* por IP (ex.: 10 tentativas/min) — protege contra bots.
- *Captcha* obrigatório após 3 falhas — degradação graduada.
- *Notificar o dono do e-mail* quando o bloqueio for acionado — torna o ataque visível.

## Referências

- [ERS](../design-de-software/ERS.md) — RF03, RN04, RN05, RN06, RN08, RN09, RNF06, RNF09
- [Arquitetura de Mensageria](../mensageria-e-streams/arquitetura-mensageria.md) — §7 (idempotência)
- [ADR-003](../design-de-software/adrs/003-status-derivado-da-etapa.md) — Status derivado da etapa (motiva a coluna `status_cache` no `pedido`)
- [ADR-005](../design-de-software/adrs/005-criptografia-em-repouso.md) — Criptografia em repouso (AES-256-GCM + HMAC para e-mail). Decisão redigida; **implementação pendente (dívida v2)**.
