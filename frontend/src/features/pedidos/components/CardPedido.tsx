import { Link } from 'react-router-dom'
import type { Pedido } from '@/types/pedidos'
import { EtapaBadge } from '@/components/ui/EtapaBadge'
import { getStatusLabel } from '../utils/statusUtils'

interface CardPedidoProps {
    pedido: Pedido
    to?: string
}

function CardPedido({ pedido, to }: CardPedidoProps) {
    const destino = to ?? `/pedidos/${pedido.id}`
    const descricao = pedido.historico?.[0]?.descricao ?? getStatusLabel(pedido.status)

    return (
        <Link to={destino} className="block">
            <article className="rounded-[5px] bg-white p-4 shadow-[0px_1px_2px_rgba(0,0,0,0.08)] transition-shadow hover:shadow-md">
                <div className="flex items-center justify-between gap-2">
                    <EtapaBadge etapa={pedido.etapa} />
                    <span className="text-[12px] leading-[16px] text-secondary">{pedido.codigo}</span>
                </div>

                <h3 className="mt-2 text-[16px] font-medium leading-[24px] text-primary-dark">{pedido.produto}</h3>
                <p className="mt-0.5 text-[14px] leading-[20px] text-secondary">{descricao}</p>

                <p className="mt-2 text-[12px] leading-[16px] text-secondary">
                    Atualizado: {pedido.atualizacao} · {pedido.cidade}
                </p>
            </article>
        </Link>
    )
}

export default CardPedido
