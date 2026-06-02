import type { Moeda } from '@/types/moeda'

interface CardMoedaProps {
    moeda: Moeda
}

const CORES_BADGE: Record<string, string> = {
    CNY: 'bg-error',
    EUR: 'bg-primary',
    USD: 'bg-primary-dark',
}

function CardMoeda({ moeda }: CardMoedaProps) {
    const corBadge = CORES_BADGE[moeda.sigla] ?? 'bg-secondary'

    return (
        <div className="flex items-center gap-3 rounded-[10px] bg-white p-4 shadow-[0px_1px_2px_rgba(0,0,0,0.08)]">
            <span className={`flex h-6 shrink-0 items-center justify-center rounded-full px-2.5 text-[11px] font-bold text-white ${corBadge}`}>
                {moeda.sigla.slice(0, 2)}
            </span>

            <div className="min-w-0 flex-1">
                <p className="text-[15px] font-semibold leading-[22px] text-ink">{moeda.nome}</p>
                <p className="text-[12px] leading-[16px] text-secondary">{moeda.sigla} / BRL</p>
            </div>

            <p className="text-[16px] font-bold leading-[24px] text-primary">
                {moeda.valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}
            </p>
        </div>
    )
}

export default CardMoeda
