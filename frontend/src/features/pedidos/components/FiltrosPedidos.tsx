import type { StatusPedido } from "@/types/pedidos"

interface FiltrosPedidosProps {
    filtroAtivo: StatusPedido | "TODOS";
    onFiltroChange: (novo: StatusPedido | "TODOS") => void;
}

const filtros: { label: string; valor: StatusPedido | "TODOS" }[] = [
    { label: "Todos", valor: "TODOS" },
    { label: "Processando", valor: "PROCESSANDO" },
    { label: "Enviado", valor: "ENVIADO" },
    { label: "Entregue", valor: "ENTREGUE" },
    { label: "Cancelado", valor: "CANCELADO" },
];

function FiltrosPedidos({ filtroAtivo, onFiltroChange }: FiltrosPedidosProps) {
    return (
        <div className="flex gap-2 mb-6 overflow-x-auto scrollbar-hide pb-2">
            {filtros.map((filtro) => (
                <button
                    key={filtro.valor}
                    type="button"
                    className={`px-4 py-2 rounded-[25px] border font-semibold text-sm shadow-md
                        ${
                            filtroAtivo === filtro.valor
                                ? "bg-primary text-white border-primary shadow"
                                : "bg-white text-secondary border-gray-300 hover:bg-gray-50"
                        }
                    `}
                    onClick={() => onFiltroChange(filtro.valor)}
                >
                    {filtro.label}
                </button>
            ))}
        </div>
    );
}

export default FiltrosPedidos