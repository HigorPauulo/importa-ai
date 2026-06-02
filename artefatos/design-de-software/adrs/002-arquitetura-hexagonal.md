# ADR-002: Arquitetura Hexagonal (Ports & Adapters)

**Status:** Aceito
**Data:** 2026-05-11
**Autor:** Equipe Importa Aí

## Contexto

O backend tem múltiplos pontos de entrada (REST, AMQP consumer, scheduler) e de saída (MySQL, AMQP producer, WebSocket, APIs externas). Sem isolamento explícito, o domínio fica acoplado a frameworks, dificultando testes isolados, troca de tecnologia e evolução incremental.

## Decisão

Adotar **Arquitetura Hexagonal** (*Ports & Adapters*) com três camadas:

- `domain/` — núcleo em Java puro, sem dependências de framework de infraestrutura.
- `application/` — *use cases* que orquestram o domínio.
- `infrastructure/` — adapters: REST, persistência, mensageria, WebSocket, integrações.

**Regra fundamental:** o pacote `domain/` **nunca** importa frameworks de infraestrutura.

## Alternativas consideradas

- **MVC clássico** (Controller → Service → Repository com JPA direto no Service) — rejeitado. Acopla o domínio à persistência; mata a testabilidade isolada.
- **Clean Architecture** — conceitualmente equivalente. "Hexagonal" foi escolhida porque enfatiza melhor o conceito de *adapters intercambiáveis* (útil no caso do `RastreamentoCorreiosPort`, com 4 adapters: stub, http CWS, cache-only, 17track — ver [ADR-004](004-adapters-correios.md)).

## Consequências

- **(+)** Domínio testável sem subir framework, broker ou banco.
- **(+)** Adapters intercambiáveis comprovadamente úteis (4 adapters de rastreamento — stub, http CWS, cache-only, 17track).
- **(−)** Mais arquivos e cerimônia que MVC simples.
- **(−)** Risco de criar "portas para tudo" — exige critério.

## Referências

- [ADR-004](004-adapters-correios.md) — Adapters intercambiáveis para integração com Correios
- [Design Patterns](../design-patterns.md) — Repository, Adapter, Circuit Breaker
