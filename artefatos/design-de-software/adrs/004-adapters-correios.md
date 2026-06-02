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
| `Rastreamento17TrackAdapter` | Demonstração e produção sem contrato Correios | Consulta a API agregadora 17track (`register` + `gettrackinfo`), que cobre Correios e transportadoras chinesas (CAINIAO, China EMS). Classifica os eventos pelo `sub_status` normalizado e reaproveita o *Circuit Breaker* e o *cache* do adapter HTTP. Fonte de terceiro com cota gratuita limitada (1.000 registros no plano grátis). |

Seleção via propriedade `correios.adapter` (`stub` | `http` | `cache-only` | `17track`), configurável pela variável de ambiente `CORREIOS_ADAPTER`.

## Alternativas consideradas

- **Único adapter HTTP, com *mocks* em testes.** Rejeitada: não cobre o caso "ambiente de demonstração sem API real" sem tornar o código condicional a *flags* de ambiente.
- **Branch de código separada por ambiente.** Rejeitada: *branches* por ambiente é antipattern e fragmenta a manutenção.

## Consequências

- **(+)** O fluxo de rastreamento funciona ponta-a-ponta independentemente da disponibilidade da API real.
- **(+)** Demonstra o valor concreto da Arquitetura Hexagonal (ADR-002) — a *port* permite trocar a fonte sem tocar no domínio.
- **(+)** Troca de adapter é mudança de configuração, não de código.
- **(−)** Três implementações para manter (mitigado: o *stub* é simples e o `cache-only` reaproveita o estado mantido pelo adapter HTTP).
- **(~)** A escolha do adapter precisa estar documentada em cada ambiente.

## Atualização (2026-06-02) — fonte real via 17track e estado de devolução

Sem contrato comercial com os Correios (pré-requisito do `CorreiosHttpAdapter`), adotou-se o **17track** como fonte real de rastreamento — um agregador que cobre os Correios e o trecho chinês (CAINIAO, China EMS), aderente ao corredor China–Brasil. A integração é um novo adapter da mesma porta (`Rastreamento17TrackAdapter`), **sem qualquer alteração no domínio** — exatamente o valor previsto por este ADR.

Os dados reais expuseram um caso que o modelo de etapas (caminho feliz `NA_CHINA → … → ENTREGUE`) não representava: pacotes **barrados na alfândega e devolvidos** ("Importação não autorizada" → "Devolução determinada pela autoridade competente"). Decisão: acrescentar o estado **`DEVOLVIDO`** ao `TipoEtapa` e ao `StatusPedido`, **terminal** como `ENTREGUE` (derivação normativa no Apêndice A da ERS e em [ADR-003](003-status-derivado-da-etapa.md)). Os `sub_status` do 17track `Exception_Returning`, `Exception_Returned` e `Exception_Security` derivam `DEVOLVIDO`.

**Dívida consciente (v2):** a cota gratuita do 17track não cobre volume de produção; em escala, migra-se para um plano pago ou para o contrato Correios (`CorreiosHttpAdapter`) — troca de configuração, sem mudança de domínio.

## Referências

- ERS — RF12 (etapas nacionais), RF15 (sincronização automática), Apêndice A (derivação de status)
- [ADR-002](002-arquitetura-hexagonal.md) — Arquitetura Hexagonal (*ports & adapters*)
- [ADR-003](003-status-derivado-da-etapa.md) — Status derivado da etapa (RN01)
- [Design Patterns Escolhidos](../design-patterns-escolhidos.md) — Adapter, Decorator
