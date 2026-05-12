# Diagrama Entidade-Relacionamento (DER) — Importa Aí

> Especificação textual do `der.drawio`. Este `.md` é a **fonte de verdade** do que o diagrama visual deve conter; o `.drawio` é a representação. Apagado após o `.drawio` ficar pronto.

## Propósito

Representar visualmente o esquema relacional das **5 entidades de negócio** do sistema, com chaves, atributos principais, relacionamentos e cardinalidades. Complementa o §4 do [`modelo-de-dados.md`](modelo-de-dados.md), que tem o texto canônico (tipos exatos, índices, constraints).

**Escopo:** somente as 5 entidades de negócio. As **tabelas técnicas** (§1–§3 do `modelo-de-dados.md` — `refresh_token_revogado`, `tentativa_login_falha`, `evento_processado`) ficam fora — são apoio infraestrutural, não modelo de dados de negócio.

## Notação

**Crow's Foot** (Information Engineering / Martin). É a notação padrão para DER em ferramentas modernas (drawio, dbdiagram.io, MySQL Workbench, DBeaver). Símbolos relevantes:

| Símbolo na ponta da linha | Significado |
|---|---|
| Traço único `─│` | **Exatamente 1** (one and only one) |
| Pé-de-galinha `─{` | **Muitos** (zero ou mais) |
| Pé-de-galinha + traço `─{│` | **Um ou mais** (1..N) |
| Círculo + traço `─o│` | **Zero ou um** (0..1, opcional) |
| Círculo + pé-de-galinha `─o{` | **Zero ou muitos** (0..N) |

Para **PK** e **FK**:
- Atributo PK: ícone de chave dourada ao lado, ou prefixo `🔑` / em **negrito** + sublinhado.
- Atributo FK: ícone de chave prata, ou prefixo `FK:`.
- Atributo NOT NULL: marcado com `*` antes do nome (convenção comum).

## Entidades

> Cada entidade é uma "tabela" no diagrama: cabeçalho com o nome, e abaixo lista de atributos. Inclua **apenas atributos relevantes ao DER** (não precisa repetir todos os 9 do schema — foco nos que definem identidade e relações).

### `usuario`

| Marca | Atributo | Tipo |
|---|---|---|
| 🔑 PK | `id` | BIGINT |
|  | `email` | VARBINARY(255) |
|  | `email_hash` | CHAR(64) UNIQUE |
|  | `nome_completo` | VARCHAR(200) |
|  | `perfil` | ENUM |
|  | `ativo` | BOOLEAN |
|  | `criado_em` | TIMESTAMP |

### `pedido`

| Marca | Atributo | Tipo |
|---|---|---|
| 🔑 PK | `id` | BIGINT |
| 🔗 FK | `usuario_id` | BIGINT → `usuario.id` |
|  | `codigo_rastreamento` | VARCHAR(50) |
|  | `valor_declarado` | DECIMAL(15,2) |
|  | `moeda` | CHAR(3) |
|  | `cancelado` | BOOLEAN |
|  | `status_cache` | ENUM (*derivado — RN01*) |
|  | `criado_em` | TIMESTAMP |

> Anexe uma **nota** à entidade `pedido` indicando: *"`status_cache` é cache derivado da última `etapa_rastreamento.tipo` + `cancelado`. Ver RN01 e ADR-003."*

### `etapa_rastreamento`

| Marca | Atributo | Tipo |
|---|---|---|
| 🔑 PK | `id` | BIGINT |
| 🔗 FK | `pedido_id` | BIGINT → `pedido.id` |
|  | `tipo` | ENUM (9 valores) |
|  | `localizacao` | VARCHAR(255) |
|  | `criado_em` | TIMESTAMP(3) |

> Anexe uma **nota**: *"Append-only por `criado_em`. UNIQUE (pedido_id, criado_em)."*

### `notificacao`

| Marca | Atributo | Tipo |
|---|---|---|
| 🔑 PK | `id` | BIGINT |
| 🔗 FK | `usuario_id` | BIGINT → `usuario.id` |
| 🔗 FK | `pedido_id` | BIGINT NULL → `pedido.id` |
|  | `mensagem` | VARCHAR(500) |
|  | `lida` | BOOLEAN |
|  | `criado_em` | TIMESTAMP |

> Anexe uma **nota**: *"FIFO de 50 por usuário via trigger (RN09)."*

### `cotacao_cache`

| Marca | Atributo | Tipo |
|---|---|---|
| 🔑 PK | `id` | BIGINT |
| 🔗 FK | `manual_por_usuario_id` | BIGINT NULL → `usuario.id` |
|  | `moeda_origem` | CHAR(3) |
|  | `moeda_destino` | CHAR(3) |
|  | `taxa` | DECIMAL(12,6) |
|  | `fonte` | ENUM('AUTOMATICA','MANUAL') |
|  | `valido_ate` | TIMESTAMP NULL |
|  | `atualizado_em` | TIMESTAMP |

## Relacionamentos

Cinco relacionamentos. Cada um com **cardinalidade visual nas duas pontas** (Crow's Foot):

| # | Origem | Destino | Lado origem | Lado destino | Política `ON DELETE` |
|---|--------|---------|-------------|--------------|----------------------|
| R1 | `pedido` | `usuario` | `─{│` (1..N pedidos) | `─│` (1 usuário) | `RESTRICT` |
| R2 | `etapa_rastreamento` | `pedido` | `─{│` (1..N etapas) | `─│` (1 pedido) | `CASCADE` (composição) |
| R3 | `notificacao` | `usuario` | `─{` (0..N notificações) | `─│` (1 usuário) | `CASCADE` |
| R4 | `notificacao` | `pedido` | `─{` (0..N notificações) | `─o│` (0..1 pedido — opcional) | `SET NULL` |
| R5 | `cotacao_cache` | `usuario` | `─{` (0..N cotações manuais) | `─o│` (0..1 usuário — opcional) | `SET NULL` |

> **Como ler Crow's Foot:** o símbolo perto de uma entidade indica **quantas instâncias DESSA entidade** podem participar do relacionamento. Exemplo R1: perto de `usuario` está `─│` (1) — significa que cada `pedido` tem **exatamente 1** usuário. Perto de `pedido` está `─{│` — significa que cada `usuario` tem **1 ou mais** pedidos.

## Layout sugerido

Disponha as entidades de forma a minimizar cruzamentos de linhas. Esquema recomendado:

```
                  ┌─────────────────────────────────────────┐
                  │                                          │
              ┌───────────┐                          ┌───────────────────────┐
              │  usuario  │◀─R1─{│─────│─────────┤      pedido            │
              └───────────┘                       │  (status_cache nota)  │
                  ▲   ▲                           └───────────────────────┘
                  │   │                                     ▲
              ┌───┴───┴─────────┐                          │
              │R3 (CASCADE)     │R5 (SET NULL)             │R2 (CASCADE)
              │                 │                          │{│
       ┌─────────────┐    ┌──────────────────┐    ┌────────────────────────┐
       │ notificacao │    │  cotacao_cache   │    │  etapa_rastreamento    │
       │             │    │                  │    │  (append-only nota)    │
       └─────────────┘    └──────────────────┘    └────────────────────────┘
              │
              └─R4─{─o│─→ pedido (opcional, SET NULL)
```

**Posicionamento concreto sugerido:**

- `usuario` à esquerda, no meio vertical (x≈100, y≈300)
- `pedido` no centro (x≈500, y≈200) — entidade mais conectada, fica em destaque
- `etapa_rastreamento` à direita do `pedido` (x≈900, y≈200)
- `notificacao` abaixo-esquerda (x≈100, y≈550)
- `cotacao_cache` abaixo (x≈500, y≈550)

## Convenções visuais

- **Fonte:** Helvetica/Arial, 11pt para atributos, 13pt para nome da entidade.
- **Cor das entidades:** preenchimento branco, borda preta, cabeçalho com fundo cinza claro (`#E0E0E0`) para destacar o nome.
- **PK:** ícone de chave (🔑) ou texto **negrito + sublinhado**.
- **FK:** ícone de chave secundária ou prefixo `FK` em verde.
- **NULL allowed:** atributo em itálico ou com sufixo `?`.
- **Notas:** caixa amarela com canto dobrado (`shape=note`), conectada por linha tracejada à entidade ou atributo relevante.
- **Linhas de relacionamento:** pretas, 1pt; nada de cor para distinguir tipo.
- **Cardinalidade Crow's Foot:** use a forma "ER" do drawio (categoria "Entity Relation"): há estilos prontos `endArrow=ERmany`, `endArrow=ERone`, `endArrow=ERmandOne` (1..N), `endArrow=ERzeroToOne` (0..1).

> **Atalho no drawio:** abra a categoria **"Entity Relation"** no painel esquerdo de formas. Lá vai aparecer:
> - **Entity 1** / **Entity 2** — tabelas prontas com colunas
> - Linhas com terminais Crow's Foot já configurados

## Notas obrigatórias no diagrama

1. **Nota anexa à `pedido`** (sobre `status_cache` derivado):
   > `status_cache` é cache derivado da última `etapa_rastreamento.tipo` + `cancelado`. Atualizado por listener. Ver RN01 e ADR-003.

2. **Nota anexa à `etapa_rastreamento`** (sobre append-only):
   > Append-only por `criado_em`. UNIQUE (pedido_id, criado_em) impede duplicação.

3. **Nota anexa à `notificacao`** (sobre RN09):
   > FIFO de 50 itens por usuário, mantido pela trigger `notificacao_limita_50`.

4. **Nota anexa à `usuario`** (sobre criptografia):
   > `email` e `nome_completo` criptografados em repouso via TDE (ADR-005). `email_hash` (HMAC-SHA256) permite busca exata.

## Checklist de aceitação (revisão pós-desenho)

- [ ] 5 entidades nomeadas: `usuario`, `pedido`, `etapa_rastreamento`, `notificacao`, `cotacao_cache`.
- [ ] Cada entidade mostra **PK explícita** (ícone, negrito ou sublinhado).
- [ ] Cada FK está visualmente marcada (ícone, prefixo `FK` ou cor).
- [ ] R1 a R5 desenhados, **todos com cardinalidade Crow's Foot em ambas as pontas**.
- [ ] R1: `pedido` (N) → `usuario` (1), obrigatório.
- [ ] R2: `etapa_rastreamento` (N) → `pedido` (1), composição (CASCADE).
- [ ] R3: `notificacao` (N) → `usuario` (1).
- [ ] R4: `notificacao` (N) → `pedido` (0..1), opcional.
- [ ] R5: `cotacao_cache` (N) → `usuario` (0..1), opcional.
- [ ] Pelo menos as 3 notas obrigatórias (status_cache, append-only, RN09).
- [ ] Arquivo salvo em `artefatos/modelagem-de-dados/der.drawio`.

## Como abrir e salvar no drawio

1. Abra **draw.io** (desktop ou app.diagrams.net).
2. **File → New** para começar limpo.
3. No painel esquerdo, role até **"Mais formas…"** (botão no fim) e marque **"Entity Relation"** + clique **Aplicar**. Vai aparecer uma nova categoria com tabelas-protótipo e conectores Crow's Foot.
4. Arraste a forma **"Entity"** (ou "Entity 2") para representar cada uma das 5 entidades. Renomeie o título e ajuste as colunas (clique 2x no atributo para editar).
5. Para os relacionamentos, conecte entidades passando o mouse sobre a borda. Depois clique na seta criada e mude o terminal no painel direito (**Estilo → Start arrow / End arrow** → escolha `ER many`, `ER one`, etc.).
6. **File → Save As** → tipo `.drawio` → caminho `artefatos/modelagem-de-dados/der.drawio`.

## Referências

- [`modelo-de-dados.md`](modelo-de-dados.md) — §4 (esquema textual canônico das 5 entidades)
- [ERS](../design-de-software/ERS.md) — RN01 (`status_cache`), RN06 (UNIQUE de código), RN08 (retenção), RN09 (FIFO 50)
- [ADR-003](../design-de-software/adrs/003-status-derivado-da-etapa.md) — Status derivado da etapa
- [ADR-005](../design-de-software/adrs/005-criptografia-em-repouso.md) — Criptografia em repouso (**a ser criado na Fase 4**)
