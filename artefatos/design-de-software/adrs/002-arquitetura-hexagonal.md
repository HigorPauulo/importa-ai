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
- **Clean Architecture** — conceitualmente equivalente. "Hexagonal" foi escolhida porque enfatiza melhor o conceito de *adapters intercambiáveis* (útil no caso do `RastreamentoCorreiosPort`, com 3 adapters: stub, http, cache-only — Apêndice C da ERS).

## Consequências

- **(+)** Domínio testável sem subir framework, broker ou banco.
- **(+)** Adapters intercambiáveis comprovadamente úteis (3 adapters de Correios).
- **(−)** Mais arquivos e cerimônia que MVC simples.
- **(−)** Risco de criar "portas para tudo" — exige critério.

## Referências

- ERS — Apêndice C (caso dos adapters de Correios)
- [Design Patterns Escolhidos](../design-patterns-escolhidos.md) — Repository, Adapter, Decorator
