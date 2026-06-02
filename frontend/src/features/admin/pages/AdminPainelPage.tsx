import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { PageHeader, BellButton } from '@/components/layout/PageHeader'
import { KpiCard } from '@/components/ui/KpiCard'
import { Card } from '@/components/ui/Card'
import CardPedido from '@/features/pedidos/components/CardPedido'
import { EvolucaoChart } from '@/features/admin/components/EvolucaoChart'
import { buscarDashboard } from '@/services/dashboard'
import { listarPedidos } from '@/services/pedidos'

function AdminPainelPage() {
    const { data: dash, isLoading, isError } = useQuery({ queryKey: ['admin', 'dashboard'], queryFn: buscarDashboard })
    const { data: pedidos = [] } = useQuery({ queryKey: ['pedidos'], queryFn: listarPedidos })

    const emTransito = dash?.porStatus.find((s) => s.status === 'ENVIADO')?.quantidade ?? 0
    const recentes = pedidos.slice(0, 4)

    return (
        <>
            <PageHeader titulo="Painel administrativo" subtitulo="Visão geral do fluxo de pacotes." acao={<BellButton estatico />} />

            {isLoading && <p className="text-secondary">Carregando indicadores...</p>}
            {isError && <p className="text-error">Não foi possível carregar o painel.</p>}

            {dash && (
                <>
                    <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 xl:grid-cols-4">
                        <KpiCard titulo="Pedidos ativos" valor={dash.totalAtivos} />
                        <KpiCard titulo="Em trânsito" valor={emTransito} />
                        <KpiCard titulo="Taxa pendente" valor={dash.taxaPendente} tom="warning" />
                        <KpiCard titulo="Entregues no mês" valor={dash.entreguesNoMes} tom="success" />
                    </div>

                    <Card className="mt-6 p-6">
                        <h2 className="mb-[18px] text-lg font-semibold text-ink">Evolução de pedidos (últimos 30 dias)</h2>
                        <EvolucaoChart dados={dash.evolucao} />
                    </Card>

                    <section className="mt-6">
                        <div className="mb-4 flex items-center justify-between">
                            <h2 className="text-xl font-semibold text-ink">Pedidos recentes</h2>
                            <Link to="/admin/pedidos" className="text-[13px] font-semibold text-primary hover:underline">Ver todos</Link>
                        </div>

                        {recentes.length === 0 ? (
                            <div className="rounded-xl border border-gray-200 bg-white p-10 text-center text-secondary">
                                Nenhum pedido recente.
                            </div>
                        ) : (
                            <div className="grid grid-cols-1 gap-6 xl:grid-cols-2">
                                {recentes.map((pedido) => (
                                    <CardPedido key={pedido.id} pedido={pedido} to={`/admin/pedidos/${pedido.id}`} />
                                ))}
                            </div>
                        )}
                    </section>
                </>
            )}
        </>
    )
}

export default AdminPainelPage
