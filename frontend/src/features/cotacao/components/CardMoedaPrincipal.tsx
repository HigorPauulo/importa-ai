import type { Moeda } from '@/types/moeda'

interface CardMoedaPrincipalProps {
    moeda: Moeda
}

function CardMoedaPrincipal({ moeda }: CardMoedaPrincipalProps) {
    return (
        <div className="max-w-xl rounded-[10px] bg-primary p-6 shadow-[0px_1px_2px_rgba(0,0,0,0.08)]">
            <div className="flex items-center justify-between">
                <h2 className="text-[16px] font-semibold leading-[24px] text-white">{moeda.nome}</h2>
                <span className="rounded-md bg-white/20 px-2.5 py-1 text-xs font-bold text-white">
                    {moeda.sigla}/BRL
                </span>
            </div>

            <p className="mt-3 text-[28px] font-bold leading-[36px] text-white">
                {moeda.valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}
            </p>

            <p className="mt-4 flex items-center gap-2 text-[13px] text-white/85">
                <span className="h-2 w-2 rounded-full bg-success" />
                Sincronizado via API ({moeda.atualizacao})
            </p>
        </div>
    )
}

export default CardMoedaPrincipal
