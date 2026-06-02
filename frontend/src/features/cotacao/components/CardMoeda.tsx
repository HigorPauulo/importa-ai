import type { Moeda } from '@/types/moeda'

interface CardMoedaProps {
    moeda: Moeda
}

function CardMoeda({ moeda }: CardMoedaProps) {
    return (
        <div className="flex items-center gap-4 rounded-[10px] bg-white p-5 shadow-[0px_1px_2px_rgba(0,0,0,0.08)]">
            <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-primary text-[13px] font-bold text-white">
                {moeda.sigla.slice(0, 2)}
            </div>

            <div className="flex-1">
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
