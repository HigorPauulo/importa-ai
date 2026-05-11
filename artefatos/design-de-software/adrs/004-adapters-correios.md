# ADR-004: Estratégia de adapters intercambiáveis para integração com Correios

**Status:** Aceito
**Data:** 2026-05-11
**Autor:** Equipe Importa Aí

## Contexto

A API dos Correios não possui um contrato REST público estável: o antigo SRO foi descontinuado e a nova API exige contrato comercial. O sistema precisa, ainda assim, oferecer rastreamento nacional confiável (RF12), inclusive em ambientes onde a API real não está disponível (desenvolvimento, demonstração, *staging*).

## Decisão

A porta de saída `RastreamentoCorreiosPort` (no domínio) é atendida por **três adapters intercambiáveis**, selecionados por configuração (`correios.adapter`):

| Adapter | Quando ativar | Comportamento |
|---------|---------------|---------------|
| `CorreiosStubAdapter` | Desenvolvimento e ambiente de demonstração (*default* em `dev`) | Retorna etapas sintéticas progressivas com base no tempo decorrido desde o cadastro do pedido — permite simular o ciclo completo sem dependência externa. |
| `CorreiosHttpAdapter` | Produção (se contrato disponível) | Chama a API real. Implementa *Circuit Breaker*: após 5 falhas consecutivas, abre o circuito por 60 s e passa a usar o último *cache* conhecido. |
| `CorreiosCacheOnlyAdapter` | *Fallback* explícito | Usa apenas o *cache* local. UI exibe aviso "atualização indisponível, exibindo última informação conhecida". |

Seleção via propriedade `correios.adapter` (`stub` | `http` | `cache-only`).

## Alternativas consideradas

- **Único adapter HTTP, com *mocks* em testes.** Rejeitada: não cobre o caso "ambiente de demonstração sem API real" sem tornar o código condicional a *flags* de ambiente.
- **Branch de código separada por ambiente.** Rejeitada: *branches* por ambiente é antipattern e fragmenta a manutenção.

## Consequências

- **(+)** O fluxo de rastreamento funciona ponta-a-ponta independentemente da disponibilidade da API real.
- **(+)** Demonstra o valor concreto da Arquitetura Hexagonal (ADR-002) — a *port* permite trocar a fonte sem tocar no domínio.
- **(+)** Troca de adapter é mudança de configuração, não de código.
- **(−)** Três implementações para manter (mitigado: o *stub* é simples e o `cache-only` reaproveita o estado mantido pelo adapter HTTP).
- **(~)** A escolha do adapter precisa estar documentada em cada ambiente.

## Referências

- ERS — RF12 (etapas nacionais), RF15 (sincronização automática)
- [ADR-002](002-arquitetura-hexagonal.md) — Arquitetura Hexagonal (*ports & adapters*)
- [Design Patterns Escolhidos](../design-patterns-escolhidos.md) — Adapter, Decorator
