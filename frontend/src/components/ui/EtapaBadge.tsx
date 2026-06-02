import type { TipoEtapa } from '@/types/pedidos'
import { getEtapaColor, getEtapaLabel } from '@/features/pedidos/utils/statusUtils'

export function EtapaBadge({ etapa }: { etapa?: TipoEtapa }) {
    return (
        <span
            className={`inline-flex items-center rounded-[5px] px-[10px] py-[4px] text-[11px] font-bold leading-[14px] uppercase ${getEtapaColor(etapa)}`}
        >
            {getEtapaLabel(etapa)}
        </span>
    )
}
