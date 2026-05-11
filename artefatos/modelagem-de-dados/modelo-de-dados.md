# Modelo de Dados — Importa Aí

**Versão:** 1.0 (rascunho)
**Data:** 11 de Maio de 2026
**Autor:** Equipe Importa Aí

## Propósito

Este documento descreve o esquema relacional do banco de dados do sistema. O Diagrama Entidade-Relacionamento (DER) completo, com todas as entidades de negócio (`usuario`, `pedido`, `etapa_rastreamento`, `notificacao`, `cotacao_cache`), será produzido em iteração posterior e versionado neste diretório.

Por ora, este documento contém o esquema das **tabelas de suporte técnico** que dão respaldo a regras de negócio e requisitos não-funcionais específicos da ERS.

---

## 1. Tabelas de suporte à autenticação

Apoiam as regras RF03 (revogação de *refresh token*) e RNF06 (bloqueio após 5 falhas de *login*). JWT é *stateless* por padrão, mas o sistema exige duas formas de estado *server-side*.

### 1.1 `refresh_token_revogado`

| Coluna | Tipo | Descrição |
|--------|------|-----------|
| `id` | UUID | Identificador interno. |
| `token_hash` | VARCHAR(64) | SHA-256 do *refresh token*. **Nunca armazenar o token em claro.** |
| `usuario_id` | UUID FK | Dono do token. |
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

## 3. Trigger de suporte à RN09

Aplica a janela FIFO de no máximo 50 notificações por usuário, atomicamente dentro da transação do `INSERT`. Permite cumprir a regra de negócio RN09 sem *lock* pessimista na camada de aplicação.

```sql
CREATE TRIGGER notificacao_limita_50
AFTER INSERT ON notificacao
FOR EACH ROW
BEGIN
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
END;
```

---

## 4. Pendências

- DER completo das entidades de negócio (`usuario`, `pedido`, `etapa_rastreamento`, `notificacao`, `cotacao_cache`).
- Definição final de cardinalidades, índices secundários e *constraints* de integridade referencial.
- Modelo físico anotado (tipos exatos por SGBD).

## Referências

- [ERS](../design-de-software/ERS.md) — RF03, RN04, RN05, RN09, RNF06
- [Arquitetura de Mensageria](../mensageria-e-streams/arquitetura-mensageria.md) — §7 (idempotência)
- [ADR-003](../design-de-software/adrs/003-status-derivado-da-etapa.md) — Status derivado da etapa (motiva a coluna `status_cache` no `pedido`)
