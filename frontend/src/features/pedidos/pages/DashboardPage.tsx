import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import Header from '@/components/Header'
import Footer from '@/components/Footer'
import notificationIcon from '@/assets/icon-notification.svg'
import { getEtapaColor, getEtapaLabel } from '@/features/pedidos/utils/statusUtils'
import { listarPedidos } from '@/services/pedidos'
import { buscarCotacao } from '@/services/cotacao'
import { useAuth } from '@/context/AuthContext'
import { useNotificacoes } from '@/context/NotificacaoContext'

function DashboardPage() {
    const { email } = useAuth()
    const { naoLidas } = useNotificacoes()

    const { data: pedidos } = useQuery({
        queryKey: ['pedidos'],
        queryFn: listarPedidos,
    })

    const { data: cotacaoYuan, isLoading: cotacaoCarregando } = useQuery({
        queryKey: ['cotacao', 'CNY'],
        queryFn: () => buscarCotacao('CNY'),
    })

    // nome de exibição derivado do email do JWT (não há claim de nome)
    const nome = (email?.split('@')[0] ?? 'Cliente').replace(/^./, (c) => c.toUpperCase())

    const lista = pedidos ?? []
    const ativas = lista.filter((p) => p.status === 'PROCESSANDO' || p.status === 'ENVIADO').length
    const ultimas = lista.slice(0, 5)

    return (
        <div className="min-h-dvh bg-background flex flex-col">
            <Header />

            <main className="flex-1 flex justify-center">
                <div className="w-full max-w-3xl px-5">
                    <header className="flex items-center justify-between my-8">
                        <h1 className="text-2xl lg:text-3xl font-bold">Olá, {nome}</h1>

                        <div className="w-10 lg:w-13 h-10 lg:h-13 bg-white shadow-md rounded-[5px] flex items-center justify-center relative">
                            {naoLidas > 0 && (
                                <span className="absolute top-[-10px] right-[-10px] w-5 lg:w-7 h-5 lg:h-7 text-xs text-white font-bold bg-error rounded-full flex items-center justify-center">
                                    {naoLidas}
                                </span>
                            )}
                            <figure>
                                <img src={notificationIcon} alt="Notificação" className="h-5 lg:h-7" />
                            </figure>
                        </div>
                    </header>

                    <div className="mb-8">
                        <Link className="w-full bg-primary text-white text-lg lg:text-xl p-4 rounded-[5px] flex justify-center items-center gap-1 shadow-sm" to="/cadastrar-encomendas" aria-label="Cadastrar nova encomenda">
                            <span className="text-2xl">+</span>
                            <span>Cadastrar nova encomenda</span>
                        </Link>
                    </div>

                    <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 mb-8">
                        <div className="bg-white shadow-md rounded-[5px] p-5">
                            <h3 className="text-base lg:text-lg text-secondary mb-4">Cotação Yuan</h3>
                            <p className="text-2xl lg:text-3xl font-bold">
                                {cotacaoCarregando
                                    ? '...'
                                    : cotacaoYuan
                                        ? <><span className="text-xl lg:text-2xl">R$</span> {cotacaoYuan.valor.toFixed(2).replace('.', ',')}</>
                                        : '—'}
                            </p>
                            <Link className="text-primary text-base lg:text-lg text-center block pt-4" to="/cotacao">Ver mais</Link>
                        </div>

                        <div className="bg-white shadow-md rounded-[5px] p-5">
                            <h3 className="text-base lg:text-lg text-secondary mb-4">Encomendas ativas</h3>
                            <p className="text-2xl lg:text-3xl font-bold text-primary">{ativas}</p>
                        </div>
                    </div>

                    <div className="bg-white shadow-md rounded-[5px] p-5">
                        <header className="border-b border-gray-300 pb-5 mb-5">
                            <h3 className="text-lg lg:text-xl font-semibold">Últimas Atualizações</h3>
                        </header>

                        {ultimas.length === 0 ? (
                            <p className="text-secondary">Você ainda não tem encomendas cadastradas.</p>
                        ) : (
                            <ul>
                                {ultimas.map((pedido) => (
                                    <li key={pedido.id} className="border-b border-gray-300 pb-4 mb-4">
                                        <div className="flex items-end justify-between mb-6">
                                            <span className="text-xs lg:text-sm bg-gray-200 text-secondary px-2 py-1 rounded-[5px] font-bold">{pedido.codigo}</span>

                                            <p className={`text-xs px-2 py-1 rounded-[5px] font-bold ${getEtapaColor(pedido.etapa)}`}>{getEtapaLabel(pedido.etapa)}</p>
                                        </div>

                                        <p className="text-lg lg:text-xl">{pedido.produto}</p>

                                        <div className="flex items-center gap-2 mt-1 text-sm lg:text-base text-secondary">
                                            <span>Atualizado em:</span><span>{pedido.atualizacao}</span> - <span>{pedido.cidade}</span>
                                        </div>
                                    </li>
                                ))}
                            </ul>
                        )}

                        <Link className="text-primary text-base lg:text-lg text-center block pt-4" to="/pedidos">Ver mais encomendas</Link>
                    </div>
                </div>
            </main>

            <Footer />
        </div>
    )
}

export default DashboardPage
