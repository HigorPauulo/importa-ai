# ADR-006: Política de CORS para REST e WebSocket

**Status:** Aceito
**Data:** 2026-05-30
**Autor:** Equipe Importa Aí

## Contexto

O frontend (React + Vite) e o backend (Spring Boot) são aplicações separadas. Em desenvolvimento, o frontend fala com o backend pelo proxy do Vite (`/api` e `/ws` → `localhost:8080`), de modo que o navegador enxerga tudo como *same-origin* e não dispara CORS. Em produção, porém, frontend e backend rodam em origens distintas e o navegador passa a exigir os cabeçalhos CORS no backend.

O estado anterior era inconsistente entre os dois canais:

- **REST:** nenhuma configuração de CORS. Funciona só por causa do proxy de dev; quebraria qualquer chamada cross-origin em produção.
- **WebSocket/SockJS:** `setAllowedOriginPatterns("*")` — aceitava qualquer origem, permissivo demais e incompatível com envio de credenciais.

Duas políticas diferentes para o mesmo sistema, nenhuma das duas adequada para produção.

## Decisão

Adotar uma **política de origens única, configurável e aplicada aos dois canais**.

- Lista de origens permitidas lida da propriedade `importaai.cors.allowed-origins` (default de desenvolvimento: `http://localhost:5173`). Em produção, o valor real é injetado por variável de ambiente, sem recompilar.
- **REST:** um único `CorsConfigurationSource` registrado no `SecurityConfig` (`http.cors(...)`), válido para `/**`. Métodos `GET/POST/PUT/PATCH/DELETE/OPTIONS`, cabeçalho `Location` exposto (o `POST /api/pedidos` devolve a URI do recurso).
- **WebSocket:** o handshake do endpoint `/ws` passa a usar a **mesma** lista (`setAllowedOriginPatterns(allowedOrigins)`), eliminando o `"*"`.
- `allowCredentials = true` com origens **explícitas** (o navegador proíbe credenciais com origem coringa). A autenticação atual é por *Bearer token* no cabeçalho `Authorization`, mas a política explícita mantém o sistema correto caso um cookie de sessão seja introduzido no futuro.

## Alternativas consideradas

- **Manter `"*"` (com ou sem credenciais)** — rejeitada. Origem coringa expõe a API a qualquer site e é incompatível com `allowCredentials = true` por especificação do navegador.
- **Delegar o CORS a um proxy/gateway em produção** — rejeitada. Acopla uma regra de segurança da aplicação à infraestrutura externa e não fica versionada junto ao código.
- **`@CrossOrigin` por controller** — rejeitada. Espalha a política por vários pontos; é fácil esquecer um endpoint novo e a regra fica divergente entre canais.

## Consequências

- **(+)** Política única, versionada e coerente entre REST e WebSocket.
- **(+)** Produção só precisa definir `importaai.cors.allowed-origins`; nada de recompilar ou alterar código.
- **(+)** Em dev, o proxy do Vite continua funcionando sem ninguém precisar configurar nada (o default cobre).
- **(−)** Toda origem de frontend nova (outro domínio, app mobile via WebView) exige atualizar a configuração e reimplantar.
- **(~)** `allowCredentials = true` obriga origens explícitas — não é possível voltar ao coringa sem desligar as credenciais.

## Referências

- `infrastructure/config/SecurityConfig.java` — bean `corsConfigurationSource`
- `infrastructure/config/WebSocketConfig.java` — handshake do endpoint `/ws`
- `application.properties` — `importaai.cors.allowed-origins`
- OWASP — Cross-Origin Resource Sharing — https://cheatsheetseries.owasp.org/cheatsheets/HTML5_Security_Cheat_Sheet.html#cross-origin-resource-sharing
