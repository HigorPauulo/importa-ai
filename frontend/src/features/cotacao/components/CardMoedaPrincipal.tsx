import type { Moeda } from '@/types/moeda'

interface CardMoedaPrincipalProps {
    moeda: Moeda
}

function CardMoedaPrincipal({ moeda }: CardMoedaPrincipalProps) {
    return (
        <div className="bg-primary shadow-md rounded-[5px] p-5 relative text-white">
            <span className="absolute top-5 right-5 text-[.8rem] font-medium px-2 py-1 rounded-full shadow-md">{moeda.sigla}</span>
            <h3 className="text-base mb-2">{moeda.nome}</h3>
            <p className="text-3xl lg:text-4xl font-extrabold">
                {moeda.valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}
            </p>
            <p className="text-sm flex items-center mt-6">
                <span className="w-3 h-3 bg-success rounded-full inline-block mr-2"></span>
                Sincronizado via API (há {moeda.atualizacao})
            </p>
        </div>
    )
}

export default CardMoedaPrincipal