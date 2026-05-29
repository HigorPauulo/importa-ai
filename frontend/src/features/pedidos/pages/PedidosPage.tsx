import Footer from '@/components/Footer'
import type { StatusPedido } from '@/types/pedidos'
import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { listarPedidos } from '@/services/pedidos'

import CardPedido from '../components/CardPedido'
import FiltrosPedidos from '../components/FiltrosPedidos'
import backIcon from '@/assets/icon-back.svg'

function PedidosPage() {
    const [filtroAtivo, setFiltroAtivo] = useState<StatusPedido | "TODOS">("TODOS")

    const { data: pedidos, isLoading, isError } = useQuery({
        queryKey: ['pedidos'],
        queryFn: listarPedidos,
    })

    const pedidosFiltrados = (pedidos ?? []).filter(
        (p) => filtroAtivo === 'TODOS' || p.status === filtroAtivo
    )

    return (
        <div className="min-h-dvh bg-background flex flex-col px-5"> 
            <header className="w-full max-w-3xl mx-auto flex items-center justify-between my-8 pt-5">
                <Link to="/dashboard">
                    <div className="w-10 lg:w-13 h-10 lg:h-13 bg-white shadow-md rounded-[5px] flex items-center justify-center">
                        <figure>
                            <img src={backIcon} alt="Voltar" className="h-5 lg:h-7" />
                        </figure>
                    </div>
                </Link>
            </header>

            <main className="flex-1 flex justify-center">
                <div className="w-full max-w-3xl">
                    <header className="mb-6">
                        <div className="flex items-center justify-between">
                            <h2 className="text-2xl font-bold">Encomendas</h2>


                            <Link to="/cadastrar-encomendas" className="bg-primary text-white w-[36px] h-[36px] lg:w-[42px] lg:h-[42px] rounded-[5px] flex items-center justify-center">
                                <span className="text-2xl lg:text-3xl mt-[-3px]">+</span>
                            </Link>
                        </div>
                    </header>

                    <FiltrosPedidos filtroAtivo={filtroAtivo} onFiltroChange={setFiltroAtivo} />

                    <div className="mt-10">
                        {isLoading && <p className="text-center text-secondary">Carregando encomendas...</p>}
                        {isError && <p className="text-center text-error">Não foi possível carregar suas encomendas.</p>}
                        {!isLoading && !isError && pedidosFiltrados.length === 0 && (
                            <p className="text-center text-secondary">Nenhuma encomenda encontrada.</p>
                        )}

                        <ul className="grid grid-cols-1 gap-7">
                            {pedidosFiltrados.map((pedido) => (
                                <li key={pedido.id}>
                                    <CardPedido pedido={pedido} />
                                </li>
                            ))}
                        </ul>
                    </div>

                </div>
            </main>

            <Footer />
        </div>
    )
}

export default PedidosPage