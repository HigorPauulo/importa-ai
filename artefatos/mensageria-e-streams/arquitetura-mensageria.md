# Arquitetura de Mensageria — Importa Aí

## Broker: RabbitMQ

## Exchanges e Filas

### Exchange: `importaai.events` (type: direct)

| Routing Key | Fila consumidora | DLQ | Consumidor |
|-------------|-----------------|-----|-----------|
| `pedido.criado` | `q.pedido.criado` | `q.pedido.criado.dlq` | `PedidoCriadoConsumer` |
| `pedido.atualizado` | `q.pedido.atualizado` | `q.pedido.atualizado.dlq` | `PedidoAtualizadoConsumer` |
| `rastreamento.atualizado` | `q.rastreamento.atualizado` | `q.rastreamento.atualizado.dlq` | `RastreamentoConsumer` |
| `notificacao.usuario` | `q.notificacao.usuario` | `q.notificacao.usuario.dlq` | `NotificacaoConsumer` |

## Fluxo principal: Criar Pedido