import { api } from './api'
import type { Notificacao } from '@/types/notificacao'

// Espelha o NotificacaoResponse do backend (GET /api/notificacoes).
interface NotificacaoResponse {
    id: number
    pedidoId: number | null
    mensagem: string
    lida: boolean
    criadoEm: string
}

// O backend já devolve a lista ordenada (mais recente primeiro) e limitada a 50 (RN09).
export async function listarNotificacoes(): Promise<Notificacao[]> {
    const { data } = await api.get<NotificacaoResponse[]>('/notificacoes')
    return data
}

// PATCH /lidas zera o badge do usuário; resposta é 204 sem corpo.
export async function marcarTodasComoLidas(): Promise<void> {
    await api.patch('/notificacoes/lidas')
}
