import type { Notificacao } from '@/types/notificacao'

// O modelo de notificação não tem campo de categoria — derivamos do único
// sinal semântico disponível (pedidoId). Diferenciar Câmbio/Segurança exigiria
// um 'tipo' no domínio (dívida v2). Hoje: ligada a pedido => STATUS DO PEDIDO.
export function categoriaDe(notificacao: Notificacao): string {
    return notificacao.pedidoId != null ? 'STATUS DO PEDIDO' : 'GERAL'
}

// "Há X minutos" / "Ontem às HH:MM" / data — a partir do criadoEm (ISO) do backend.
export function tempoRelativo(iso: string): string {
    const data = new Date(iso)
    const segundos = Math.max(0, Math.floor((Date.now() - data.getTime()) / 1000))

    if (segundos < 60) return 'Agora'

    const minutos = Math.floor(segundos / 60)
    if (minutos < 60) return `Há ${minutos} ${minutos === 1 ? 'minuto' : 'minutos'}`

    const horas = Math.floor(minutos / 60)
    if (horas < 24) return `Há ${horas} ${horas === 1 ? 'hora' : 'horas'}`

    const hora = data.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
    const ontem = new Date()
    ontem.setDate(ontem.getDate() - 1)
    if (data.toDateString() === ontem.toDateString()) return `Ontem às ${hora}`

    return data.toLocaleDateString('pt-BR')
}
