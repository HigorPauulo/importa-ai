import type { StatusPedido } from '@/types/pedidos'
import { getStatusColor, getStatusLabel } from '@/features/pedidos/utils/statusUtils'

export function StatusBadge({ status }: { status: StatusPedido }) {
    return (
        <span
            className={`inline-flex items-center rounded-[5px] px-2.5 py-1 text-[11px] font-bold uppercase tracking-wide ${getStatusColor(status)}`}
        >
            {getStatusLabel(status)}
        </span>
    )
}
