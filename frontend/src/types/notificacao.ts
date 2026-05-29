// Espelha o payload STOMP enviado pelo backend em /user/queue/notificacoes
// (Notificacao do domínio serializada pelo NotificacaoConsumer) e também o
// NotificacaoResponse do GET /api/notificacoes.
export interface Notificacao {
    id: number
    pedidoId: number | null
    mensagem: string
    lida: boolean
    criadoEm: string
    usuarioId?: number
}
