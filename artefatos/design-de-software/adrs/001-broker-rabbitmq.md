# ADR-001: Escolha do broker — RabbitMQ

**Status:** Aceito
**Data:** 2026-05-11
**Autor:** Equipe Importa Aí

## Contexto

O sistema precisa de mensageria assíncrona para atender **RN03** (HTTP 202 imediato após publicação), **RNF05** (durabilidade de eventos) e **RNF11** (escalabilidade horizontal de consumidores). Este ADR registra a escolha entre brokers disponíveis.

## Decisão

Adotar **RabbitMQ 3.13+** como broker AMQP.

## Alternativas consideradas

- **Apache Kafka** — rejeitado. Otimizado para streaming de altíssima vazão e *ordering* por partição. O volume do Importa Aí é baixo e não há requisito de *ordering* global. A complexidade operacional (Zookeeper/KRaft, partições, retenção, compaction) é injustificável.
- **AWS SQS** — rejeitado. Exige conta AWS e dependência de nuvem — incompatível com a estratégia de ambiente local primário do projeto.
- **Sem broker** (síncrono) — rejeitado. Quebraria RN03 e RNF02 (HTTP 202 em < 200 ms) e tornaria o sistema frágil a picos de carga.

## Consequências

- **(+)** ACK manual, Publisher Confirms e DLQ são nativos do AMQP.
- **(+)** Painel Management UI reduz a necessidade de instrumentação externa.
- **(−)** Não escala para centenas de milhares de eventos por segundo (não é o caso).
- **(~)** A porta `EventPublisher` (ADR-002) permite troca futura sem tocar no domínio.

## Referências

- ERS — RN03, RNF02, RNF05, RNF11
- [Arquitetura de Mensageria](../../mensageria-e-streams/arquitetura-mensageria.md) — §2
