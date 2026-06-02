export interface FiltroOpcao {
    label: string
    valor: string
}

interface FiltrosPedidosProps {
    opcoes: FiltroOpcao[]
    ativo: string
    onChange: (valor: string) => void
}

function FiltrosPedidos({ opcoes, ativo, onChange }: FiltrosPedidosProps) {
    return (
        <div className="scrollbar-hide mb-6 flex gap-2 overflow-x-auto pb-1">
            {opcoes.map((opcao) => (
                <button
                    key={opcao.valor}
                    type="button"
                    onClick={() => onChange(opcao.valor)}
                    className={`shrink-0 rounded-full px-4 py-2 text-[13px] font-medium leading-[18px] transition-colors ${
                        ativo === opcao.valor
                            ? 'bg-primary text-white'
                            : 'border border-gray-200 bg-white text-secondary hover:bg-gray-50'
                    }`}
                >
                    {opcao.label}
                </button>
            ))}
        </div>
    )
}

export default FiltrosPedidos
