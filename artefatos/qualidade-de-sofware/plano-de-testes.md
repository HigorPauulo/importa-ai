# Plano de Testes — Importa Aí v1.0

## Objetivo
Garantir que o sistema atenda aos requisitos funcionais e não funcionais definidos
na ERS v1.0, com foco em confiabilidade do fluxo de mensageria e integridade das
transições de estado dos pedidos.

## Escopo
- Testes unitários do domínio (Java)
- Testes de integração dos endpoints REST
- Testes de integração do fluxo RabbitMQ (producer → consumer)
- Testes de contrato da API

## Fora do escopo (nesta versão)
- Testes de carga/performance
- Testes E2E automatizados de frontend

## Requisitos Não-Funcionais de qualidade
- Cobertura de testes unitários ≥ 80% no domínio
- Zero issues bloqueantes no SonarQube
- Complexidade ciclomática máxima por método: 10

## Cenários de teste prioritários

### Módulo: Gestão de Pedidos
| ID | Cenário | Tipo | Critério de aceitação |
|----|---------|------|-----------------------|
| TC01 | Criar pedido com dados válidos | Unitário | Retorna HTTP 202, evento publicado no RabbitMQ |
| TC02 | Criar pedido com código duplicado (mesmo usuário) | Unitário | Retorna HTTP 422 com mensagem de erro |
| TC03 | Transição de estado válida PROCESSANDO → ENVIADO | Unitário | Estado alterado, evento publicado |
| TC04 | Transição de estado inválida PROCESSANDO → ENTREGUE | Unitário | Domínio lança exceção, HTTP 422 |
| TC05 | Tentativa de ENTREGUE sem etapa CD_BRASIL | Unitário | Domínio rejeita, HTTP 422 |

### Módulo: Mensageria
| ID | Cenário | Tipo | Critério de aceitação |
|----|---------|------|-----------------------|
| TC06 | Consumer processa evento `pedido.criado` | Integração | Pedido persistido no MySQL |
| TC07 | Reprocessamento de evento já processado | Integração | Nenhum efeito colateral (idempotência) |
| TC08 | Falha no consumer → DLQ | Integração | Mensagem vai para DLQ após 3 tentativas |

### Módulo: Notificações
| ID | Cenário | Tipo | Critério de aceitação |
|----|---------|------|-----------------------|
| TC09 | Mudança de status dispara notificação WebSocket | Integração | Mensagem recebida em < 2s |
| TC10 | Limite de 50 notificações por usuário (RN09) | Unitário | 51ª notificação remove a mais antiga |

### Módulo: Autenticação
| ID | Cenário | Tipo | Critério de aceitação |
|----|---------|------|-----------------------|
| TC11 | Login com credenciais válidas | Integração | Retorna JWT + refresh token |
| TC12 | Acesso a endpoint privado sem JWT | Integração | HTTP 401 |
| TC13 | Acesso a pedido de outro usuário (perfil Cliente) | Integração | HTTP 403 |
| TC14 | 5 tentativas de login falhas → bloqueio | Integração | HTTP 429, conta bloqueada 15 min |

## Métricas de aceitação da N2
- [ ] TC01–TC05 passando (domínio)
- [ ] TC06–TC08 passando (mensageria)
- [ ] TC11–TC13 passando (autenticação)
- [ ] Relatório JaCoCo com cobertura ≥ 80% no pacote `domain/`