import type { Pedido } from '@/types/pedidos'
import { getEtapaColor, getEtapaLabel } from '../utils/statusUtils'

interface CardPedidoProps {
    pedido: Pedido
}

function CardPedido({ pedido }: CardPedidoProps) {
    return (
        <div className="bg-white flex flex-col gap-4 shadow-md rounded-[5px] p-5">
            <div className="w-full flex items-center justify-between gap-2 mb-3">
                <p className={`text-xs uppercase px-2 py-1 rounded-[5px] font-bold ${getEtapaColor(pedido.etapa)}`}>{getEtapaLabel(pedido.etapa)}</p>

                <span className="text-xs lg:text-sm bg-gray-200 text-secondary px-2 py-1 rounded-[5px] font-bold">{pedido.codigo}</span>
            </div>

            <div className="w-full flex flex-col gap-2 mb-3">
                <h3 className="text-lg lg:text-xl font-semibold">{pedido.produto}</h3>

                <p className="text-sm lg:text-base text-secondary italic">{pedido.status}</p>
           
            </div>

            <div className="w-full flex items-center gap-2 text-secondary text-sm lg:text-base border-t border-gray-300 pt-4">
       
                <span>{pedido.atualizacao}</span>
                <span>-</span>
                <span>{pedido.cidade}</span>
            </div>
        </div>
    )
}

export default CardPedido