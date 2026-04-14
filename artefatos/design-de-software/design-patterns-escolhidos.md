# Arquitetura e Design Patterns — Importa Aí

## Arquitetura: Hexagonal (Ports & Adapters)

### Justificativa
A Arquitetura Hexagonal foi escolhida porque:
1. **Testabilidade**: o domínio não depende de frameworks — pode ser testado em isolamento
2. **Independência de mensageria**: RabbitMQ e API REST são adaptadores intercambiáveis
3. **Projeto solo**: limites claros entre domínio e infraestrutura reduzem acoplamento acidental
4. **Adequação ao problema**: o sistema possui múltiplos adaptadores de entrada
   (REST, RabbitMQ consumer, scheduler) e saída (MySQL, RabbitMQ producer,
   WebSocket, API Correios, API Câmbio)

### Design Patterns aplicados

| Pattern | Onde é aplicado | Por quê |
|---------|----------------|---------|
| **Strategy** | Cotação de câmbio (API vs manual vs cache) | Permite trocar a fonte de cotação sem alterar o domínio |
| **Factory Method** | Criação de eventos de rastreamento por tipo de etapa | Centraliza a lógica de instanciação de etapas |
| **Observer (via RabbitMQ)** | Publicação de eventos de domínio | Desacopla o produtor dos consumidores |
| **Repository** | Acesso a dados (pedido, usuário, notificação) | Abstrai o banco do domínio |
| **Facade** | `PedidoService` como ponto de entrada do caso de uso | Simplifica a interface para o adaptador REST |
| **Chain of Responsibility** | Validação de transições de estado do pedido | Cada validador é independente e encadeável |