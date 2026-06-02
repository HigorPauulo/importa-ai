import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { PageHeader } from '@/components/layout/PageHeader'
import { listarPedidos } from '@/services/pedidos'
import CardPedido from '../components/CardPedido'
import FiltrosPedidos from '../components/FiltrosPedidos'
import { filtrarPorSituacao, opcoesFiltro, type FiltroPedido } from '../utils/filtros'

function PedidosPage() {
    const [filtro, setFiltro] = useState<FiltroPedido>('TODOS')

    const { data: pedidos, isLoading, isError } = useQuery({ queryKey: ['pedidos'], queryFn: listarPedidos })

    const lista = pedidos ?? []
    const filtrados = lista.filter((p) => filtrarPorSituacao(p, filtro))
    const opcoes = opcoesFiltro(lista.length)

    return (
        <>
            <PageHeader
                titulo="Encomendas"
                acao={
                    <Link
                        to="/cadastrar-encomendas"
                        className="inline-flex h-10 items-center gap-1.5 rounded-[5px] bg-primary px-4 text-[14px] font-medium text-white transition-colors hover:bg-primary-dark"
                    >
                        + Nova encomenda
                    </Link>
                }
            />

            <FiltrosPedidos opcoes={opcoes} ativo={filtro} onChange={(v) => setFiltro(v as FiltroPedido)} />

            {isLoading && <p className="text-secondary">Carregando encomendas...</p>}
            {isError && <p className="text-error">Não foi possível carregar suas encomendas.</p>}
            {!isLoading && !isError && filtrados.length === 0 && (
                <div className="rounded-xl border border-gray-200 bg-white p-10 text-center text-secondary">
                    Nenhuma encomenda encontrada.
                </div>
            )}

            <div className="grid grid-cols-1 gap-6 xl:grid-cols-2">
                {filtrados.map((pedido) => (
                    <CardPedido key={pedido.id} pedido={pedido} />
                ))}
            </div>
        </>
    )
}

export default PedidosPage
