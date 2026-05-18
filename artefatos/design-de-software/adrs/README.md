# ADRs — Architecture Decision Records

Este diretório contém os ADRs do projeto Importa Aí. Cada ADR documenta uma decisão arquitetural significativa: contexto, decisão, alternativas e consequências.

## Formato

Padrão simplificado de Michael Nygard. Arquivos nomeados `NNN-titulo-curto.md` com as seções: **Status / Contexto / Decisão / Alternativas / Consequências / Referências**.

## Índice

| ID | Título | Status |
|----|--------|--------|
| [ADR-001](001-broker-rabbitmq.md) | Escolha do broker — RabbitMQ | Aceito |
| [ADR-002](002-arquitetura-hexagonal.md) | Arquitetura Hexagonal (Ports & Adapters) | Aceito |
| [ADR-003](003-status-derivado-da-etapa.md) | Status do pedido derivado da última etapa | Aceito |
| [ADR-004](004-adapters-correios.md) | Adapters intercambiáveis para integração com Correios | Aceito |
| [ADR-005](005-criptografia-em-repouso.md) | Criptografia de PII em repouso (AES-256-GCM + HMAC) | Aceito |

ADRs aceitos não são editados retroativamente. Se a decisão mudar, crie um novo ADR substituindo o anterior e altere o status do antigo para `Substituído por ADR-XXX`.
