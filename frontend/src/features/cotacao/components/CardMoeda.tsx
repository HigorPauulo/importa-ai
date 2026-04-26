import type { Moeda } from '@/types/moeda'

interface CardMoedaProps {
    moeda: Moeda
}

function CardMoeda({ moeda }: CardMoedaProps) {
    return (
        <div className="bg-white shadow-md rounded-[5px] p-5">
            <h3 className="text-base lg:text-lg text-gray-500 mb-4">{moeda.nome}</h3>
        </div>
    )
}

export default CardMoeda