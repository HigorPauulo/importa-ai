import type { Moeda } from '@/types/moeda'

interface CardMoedaProps {
    moeda: Moeda
}

function CardMoeda({ moeda }: CardMoedaProps) {
    return (
        <div className="bg-white flex items-center justify-between shadow-md rounded-[5px] p-5">
            <div className="flex items-center gap-2">
                <div>
                    <span className="text-xl lg:text-2xl font-light px-2 py-1">{moeda.sigla.slice(0, 2)}</span>
                </div>
                <div>
                    <h3 className="text-xl lg:text-2xl font-bold">{moeda.nome}</h3>
                    <p className="text-base lg:text-lg text-secondary">{moeda.sigla} / BRL</p>
                </div>
            </div>

            <div>
                <p className="text-xl lg:text-2xl font-bold">{moeda.valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}</p>
            </div>
        </div>
    )
}

export default CardMoeda