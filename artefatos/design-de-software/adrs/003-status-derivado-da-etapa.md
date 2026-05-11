# ADR-003: Status do pedido derivado da última etapa

**Status:** Aceito
**Data:** 2026-05-11
**Autor:** Equipe Importa Aí

## Contexto

Originalmente (v1.0 da ERS), o `Pedido` tinha um campo `status` armazenado, atualizado por endpoints e listeners. Esse design abriu duas dores: status desincronizado das etapas reais e transições inconsistentes via `PATCH /status`. A auditoria que motivou a v1.1 propôs a refatoração.

## Decisão

`StatusPedido` **não é armazenado** como campo independente. Ele é uma **função pura** da última `TipoEtapa` registrada e da flag `cancelado` — ver tabela normativa no Apêndice A da ERS.

Conclusões diretas:

- Não existe endpoint `PATCH /pedidos/{id}/status`.
- A única forma de promover o status é registrando uma nova etapa via `POST /pedidos/{id}/etapas` (RF13).
- Uma coluna `status_cache` é mantida em `pedido` apenas para queries (filtros, dashboard); é **cache derivado**, nunca fonte de verdade — pode ser recomputada das etapas a qualquer momento.

## Alternativas consideradas

- **Campo `status` + máquina de estados explícita** — rejeitado. Continua tendo duas fontes de verdade (etapas + status).
- **Derivar em tempo de query sem cache** — rejeitado. Adiciona JOIN/agregação em todas as listas; performance ruim em dashboards.

## Consequências

- **(+)** Impossível ter status divergente das etapas — invariante garantida pela linguagem.
- **(+)** Testável trivialmente: as 10 linhas da tabela do Apêndice A viram 10 casos parametrizados (TC15–TC20).
- **(−)** `status_cache` precisa ser atualizado de forma confiável pelo listener — mitigado por job futuro de reconciliação.

## Referências

- ERS — RN01, RF13, Apêndice A (tabela normativa)
- [Plano de Testes](../../qualidade-de-software/plano-de-testes.md) — TC15–TC21
