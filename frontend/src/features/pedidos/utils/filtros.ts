import type { Pedido } from '@/types/pedidos'
import type { FiltroOpcao } from '@/features/pedidos/components/FiltrosPedidos'

export type FiltroPedido = 'TODOS' | 'EM_TRANSITO' | 'TAXADOS' | 'ENTREGUES' | 'DEVOLVIDOS'

export function filtrarPorSituacao(pedido: Pedido, filtro: FiltroPedido): boolean {
    switch (filtro) {
        case 'EM_TRANSITO':
            return pedido.status === 'ENVIADO' && pedido.etapa !== 'TAXA'
        case 'TAXADOS':
            return pedido.etapa === 'TAXA'
        case 'ENTREGUES':
            return pedido.status === 'ENTREGUE'
        case 'DEVOLVIDOS':
            return pedido.status === 'DEVOLVIDO'
        default:
            return true
    }
}

export function opcoesFiltro(total: number): FiltroOpcao[] {
    return [
        { label: `Todos (${total})`, valor: 'TODOS' },
        { label: 'Em Trânsito', valor: 'EM_TRANSITO' },
        { label: 'Taxados', valor: 'TAXADOS' },
        { label: 'Entregues', valor: 'ENTREGUES' },
        { label: 'Devolvidos', valor: 'DEVOLVIDOS' },
    ]
}
