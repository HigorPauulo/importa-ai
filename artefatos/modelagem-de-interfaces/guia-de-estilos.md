# Guia de Estilos — Importa Aí

## Identidade visual

Plataforma de rastreamento logístico internacional. Tom: **profissional, confiável, moderno**. Usuários precisam encontrar informações de status rapidamente — clareza vence ornamento.

## Figma (fonte de verdade do design)

https://www.figma.com/design/FMMi2RfWDbC1zmnnA9gv3t/Importa-Aí

> Os tokens abaixo refletem a implementação real em `frontend/src/index.css` e devem ser mantidos em sincronia com o Figma.

---

## Tipografia

| Uso | Família | Peso |
|-----|---------|------|
| Headings e body | `Inter` (fallback `sans-serif`) | 400 / 500 / 700 |

Importada via Google Fonts em `index.css`:

```css
@import url('https://fonts.googleapis.com/css2?family=Inter:ital,opsz,wght@0,14..32,100..900;1,14..32,100..900&display=swap');
```

---

## Paleta de cores

### Primárias

| Token | Valor | Uso |
|-------|-------|-----|
| `--color-primary` | `#0D6EFD` | CTAs principais, links ativos, foco de inputs |
| `--color-primary-dark` | `#0A3B8C` | Hover/active de CTAs, texto sobre backgrounds claros do brand |
| `--color-primary-light` | `#CFE2FF` | Backgrounds de badges, destaques sutis |
| `--color-secondary` | `#6C757D` | Texto secundário, ícones inativos, placeholders |
| `--color-background` | `#F3F4F6` | Background da página |

### Feedback (sucesso, aviso, erro)

| Token | Valor | Uso |
|-------|-------|-----|
| `--color-success` | `#22C55E` | Ícones e textos de sucesso |
| `--color-success-bg` | `#D1FAE5` | Background de toasts/badges de sucesso |
| `--color-success-dark` | `#065F46` | Texto sobre `--color-success-bg` |
| `--color-warning` | `#F59E42` | Avisos, cotação desatualizada, taxa pendente |
| `--color-warning-bg` | `#FEF3C7` | Background de toasts/badges de aviso |
| `--color-error` | `#DC2626` | Erros de validação, cancelamento |
| `--color-error-bg` | `#FFE6E6` | Background de toasts/badges de erro |

### Status do pedido (derivado da etapa — RN01)

| Status | Background token | Texto token |
|--------|------------------|-------------|
| `PROCESSANDO` | `--color-status-processando-bg` (= `--color-primary-light`) | `--color-status-processando-text` (= `--color-primary-dark`) |
| `ENVIADO` | `--color-status-enviado-bg` (= `--color-success-bg`) | `--color-status-enviado-text` (= `--color-success-dark`) |
| `ENTREGUE` | `--color-status-entregue-bg` (= `--color-success-bg`) | `--color-status-entregue-text` (= `--color-success-dark`) |
| `CANCELADO` | `--color-status-cancelado-bg` (= `--color-error-bg`) | `--color-status-cancelado-text` (= `--color-error`) |

### Etapas de rastreamento (`TipoEtapa`)

Para coerência visual com o status macro, etapas que geram status `PROCESSANDO` usam o tom **primary-light**, e etapas que geram `ENVIADO` ou `ENTREGUE` usam o tom **success**. **Exceção:** `TAXA`, embora derive status `ENVIADO`, exige ação do usuário (pagamento de imposto) e por isso usa o tom **warning** — alinhado com o uso de `--color-warning` para "taxa pendente" definido acima.

| Etapa | Background | Texto |
|-------|------------|-------|
| `NA_CHINA` | `primary-light` | `primary-dark` |
| `AEROPORTO_ORIGEM` | `primary-light` | `primary-dark` |
| `EM_TRANSITO` | `primary-light` | `primary-dark` |
| `AEROPORTO_DESTINO` | `primary-light` | `primary-dark` |
| `NO_BRASIL` | `success-bg` | `success-dark` |
| `TAXA` | `warning-bg` | `warning` |
| `CD_BRASIL` | `success-bg` | `success-dark` |
| `SAIDA_ENTREGA` | `success-bg` | `success-dark` |
| `ENTREGUE` | `success-bg` | `success-dark` |

> Tokens completos disponíveis em `frontend/src/index.css` no bloco `@theme`.

---

## Componentes-chave

| Componente | Onde | Notas de uso |
|------------|------|--------------|
| **Button** (`components/ui/Button.tsx`) | CTAs, formulários, ações de admin | Variantes baseadas em `--color-primary`. Suporta `fullWidth` para formulários. |
| **Input** (`components/ui/Input.tsx`) | Formulários de cadastro, login, criação de pedido | Borda `--color-secondary`, foco em `--color-primary`, mensagem de erro em `--color-error`. |
| **Badge de status do pedido** | Cards de pedido (lista), detalhe do pedido | 4 variantes — uma por valor de `StatusPedido`. Cor é derivada do status, que por sua vez é derivado da etapa. |
| **Linha do tempo vertical** (`pages/DetalhesPedidoPage.tsx`) | Detalhe do pedido | Ícone por tipo de etapa, timestamp, localização (quando disponível), destaque visual para a etapa atual (ponto preenchido com `--color-primary`). |
| **Card de pedido** (`components/CardPedido.tsx`) | Lista de pedidos, dashboard | Background `white`, sombra sutil, badge de status no topo direito. |
| **Toast de notificação em tempo real** | Camada flutuante (top-right) | Disparado pelo recebimento de mensagem WebSocket. Variantes seguem feedback (success / warning / error). Auto-dismiss em 4s. |
| **Dashboard card (KPI)** (`pages/DashboardPage.tsx`) | Dashboard administrativo | Número grande, label secundário, ícone à esquerda. Usa `--color-secondary` para o label e `--color-primary-dark` para o valor. |

---

## Layout e responsividade

- **Mobile-first.** Breakpoint mínimo: **375px** (RNF13).
- **Carregamento inicial:** ≤ 3s em conexão 4G (RNF13).
- Grid via Tailwind (utilitários `grid`, `flex`, `gap-*`).
- Espaçamentos múltiplos de 4px (escala padrão Tailwind).

---

## Acessibilidade (mínimo)

- Contraste de texto sobre background: ≥ **4.5:1** (WCAG AA).
- Foco visível em todos os elementos interativos (`focus-visible:outline`).
- Labels associados a inputs via `htmlFor`/`id`.
- Mensagens de erro lidas por screen reader (`aria-describedby`).

---

## Onde os tokens vivem

- **Fonte da verdade:** `frontend/src/index.css` (bloco `@theme` do Tailwind v4).
- Mudanças de paleta são feitas neste arquivo e propagam automaticamente via classes utilitárias (`bg-primary`, `text-success`, etc.).
- Este guia deve ser atualizado sempre que `@theme` mudar.
