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
- Uma coluna `status_cache` **poderá** ser mantida em `pedido` apenas para queries (filtros, dashboard) como cache derivado — nunca fonte de verdade. **Nesta versão a coluna não foi implementada**: o status é derivado *on-the-fly* da última etapa (sem cache, sem listener). `status_cache` é otimização v2.

## Alternativas consideradas

- **Campo `status` + máquina de estados explícita** — rejeitado. Continua tendo duas fontes de verdade (etapas + status).
- **Derivar em tempo de query sem cache** — rejeitado. Adiciona JOIN/agregação em todas as listas; performance ruim em dashboards.

## Consequências

- **(+)** Impossível ter status divergente das etapas — invariante garantida pela linguagem.
- **(+)** Testável trivialmente: as linhas da tabela do Apêndice A (12, incl. `DEVOLVIDO`) viram casos parametrizados (TC15–TC20, TC32, TC33).
- **(~)** `status_cache` (cache de query) é dívida v2; nesta versão a derivação *on-the-fly* evita qualquer divergência por construção, ao custo de recomputar a partir das etapas.

## Referências

- ERS — RN01, RF13, Apêndice A (tabela normativa)
- [Plano de Testes](../../qualidade-de-software/plano-de-testes.md) — TC15–TC21
