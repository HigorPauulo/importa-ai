import type { ReactNode } from 'react'
import type { Pedido } from '@/types/pedidos'
import { IconStar } from '@/components/layout/icons'

interface PedidoResumoCardProps {
    pedido: Pedido
    valorBrl?: string
    children?: ReactNode
}

export function PedidoResumoCard({ pedido, valorBrl, children }: PedidoResumoCardProps) {
    const valorDeclarado =
        pedido.valorEstimado != null
            ? pedido.valorEstimado.toLocaleString('pt-BR', { style: 'currency', currency: pedido.moeda ?? 'BRL' })
            : '—'

    return (
        <div className="rounded-[10px] bg-white p-6 shadow-[0px_1px_2px_rgba(0,0,0,0.08)]">
            <div className="flex h-16 w-16 items-center justify-center rounded-[10px] bg-primary-light">
                <IconStar className="h-7 w-7 text-primary" />
            </div>

            <h2 className="mt-4 text-[20px] font-semibold leading-[28px] text-ink">{pedido.produto}</h2>
            <p className="mt-1 text-[13px] leading-[18px] text-secondary">{pedido.codigo}</p>

            <div className="my-4 h-px bg-[#e5e7eb]" />

            <dl className="space-y-4">
                <div>
                    <dt className="text-[11px] font-bold uppercase leading-[14px] text-secondary">Origem</dt>
                    <dd className="mt-1 text-[15px] leading-[22px] text-ink">{pedido.origem ?? '—'}</dd>
                </div>
                <div>
                    <dt className="text-[11px] font-bold uppercase leading-[14px] text-secondary">Valor estimado</dt>
                    <dd className="mt-1 text-[15px] leading-[22px] text-ink">
                        {valorDeclarado}
                        {valorBrl && <span className="text-secondary"> ({valorBrl})</span>}
                    </dd>
                </div>
            </dl>

            {children && <div className="mt-6">{children}</div>}
        </div>
    )
}
