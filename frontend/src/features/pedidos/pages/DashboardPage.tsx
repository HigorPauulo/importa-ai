import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { PageHeader, BellButton } from '@/components/layout/PageHeader'
import { KpiCard } from '@/components/ui/KpiCard'
import CardPedido from '@/features/pedidos/components/CardPedido'
import { listarPedidos } from '@/services/pedidos'
import { buscarCotacao } from '@/services/cotacao'
import { useAuth } from '@/context/AuthContext'
import { nomeExibicao } from '@/lib/usuario'
import { buscarMeuPerfil } from '@/services/perfil'

function DashboardPage() {
    const { email, perfil } = useAuth()

    const { data: pedidos } = useQuery({ queryKey: ['pedidos'], queryFn: listarPedidos })
    const { data: meuPerfil } = useQuery({ queryKey: ['me'], queryFn: buscarMeuPerfil })
    const { data: cotacaoYuan, isLoading: cotacaoCarregando } = useQuery({
        queryKey: ['cotacao', 'CNY'],
        queryFn: () => buscarCotacao('CNY'),
    })

    const nome = meuPerfil?.nome ?? nomeExibicao(email, perfil)

    const lista = pedidos ?? []
    const ativas = lista.filter((p) => p.status === 'PROCESSANDO' || p.status === 'ENVIADO').length
    const emTransito = lista.filter((p) => p.etapa === 'EM_TRANSITO').length
    const taxaPendente = lista.filter((p) => p.etapa === 'TAXA').length
    const ultimas = lista.slice(0, 4)

    const yuan = cotacaoCarregando ? '...' : cotacaoYuan ? `R$ ${cotacaoYuan.valor.toFixed(2).replace('.', ',')}` : '—'

    return (
        <>
            <PageHeader titulo={`Olá, ${nome}`} subtitulo="Acompanhe suas encomendas e cotações." acao={<BellButton />} />

            <div className="grid grid-cols-2 gap-4 xl:grid-cols-4">
                <KpiCard titulo="Encomendas ativas" valor={ativas} />
                <KpiCard titulo="Em trânsito" valor={emTransito} />
                <KpiCard titulo="Taxa pendente" valor={taxaPendente} />
                <KpiCard titulo="Cotação Yuan" valor={yuan} tom="primary" />
            </div>

            <section className="mt-6">
                <div className="mb-4 flex items-center justify-between">
                    <h2 className="text-[20px] font-semibold leading-[28px] text-ink">Últimas atualizações</h2>
                    <Link to="/pedidos" className="text-[13px] font-semibold text-primary hover:underline">Ver todas</Link>
                </div>

                {ultimas.length === 0 ? (
                    <div className="rounded-[10px] bg-white p-10 text-center text-secondary shadow-[0px_1px_2px_rgba(0,0,0,0.08)]">
                        Você ainda não tem encomendas cadastradas.
                    </div>
                ) : (
                    <div className="grid grid-cols-1 gap-6 xl:grid-cols-2">
                        {ultimas.map((pedido) => (
                            <CardPedido key={pedido.id} pedido={pedido} />
                        ))}
                    </div>
                )}
            </section>
        </>
    )
}

export default DashboardPage
