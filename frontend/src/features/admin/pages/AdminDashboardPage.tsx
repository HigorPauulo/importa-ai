import { useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import Header from '@/components/Header'
import Footer from '@/components/Footer'
import { useAuth } from '@/context/AuthContext'
import { buscarDashboard } from '@/services/dashboard'
import type { StatusPedido } from '@/types/pedidos'

const labelStatus: Record<StatusPedido, string> = {
    PROCESSANDO: 'Processando',
    ENVIADO: 'Enviado',
    ENTREGUE: 'Entregue',
    CANCELADO: 'Cancelado',
}

function AdminDashboardPage() {
    const navigate = useNavigate()
    const { logout } = useAuth()

    const { data, isLoading, isError } = useQuery({
        queryKey: ['admin', 'dashboard'],
        queryFn: buscarDashboard,
    })

    const sair = async () => {
        await logout()
        navigate('/login')
    }

    return (
        <div className="min-h-dvh bg-background flex flex-col">
            <Header />

            <main className="flex-1 flex justify-center">
                <div className="w-full max-w-3xl px-5">
                    <header className="flex items-center justify-between my-8">
                        <h1 className="text-2xl lg:text-3xl font-bold">Painel administrativo</h1>
                        <button type="button" onClick={sair} className="text-sm text-primary font-semibold">Sair</button>
                    </header>

                    {isLoading && <p className="text-center text-secondary mt-10">Carregando indicadores...</p>}
                    {isError && <p className="text-center text-error mt-10">Não foi possível carregar o painel.</p>}

                    {data && (
                        <>
                            <div className="grid grid-cols-1 lg:grid-cols-3 gap-4 mb-8">
                                <div className="bg-white shadow-md rounded-[5px] p-5">
                                    <h3 className="text-base text-secondary mb-2">Pedidos ativos</h3>
                                    <p className="text-3xl font-bold text-primary">{data.totalAtivos}</p>
                                </div>
                                <div className="bg-white shadow-md rounded-[5px] p-5">
                                    <h3 className="text-base text-secondary mb-2">Taxa pendente</h3>
                                    <p className="text-3xl font-bold text-warning">{data.taxaPendente}</p>
                                </div>
                                <div className="bg-white shadow-md rounded-[5px] p-5">
                                    <h3 className="text-base text-secondary mb-2">Entregues no mês</h3>
                                    <p className="text-3xl font-bold text-success">{data.entreguesNoMes}</p>
                                </div>
                            </div>

                            <div className="bg-white shadow-md rounded-[5px] p-5">
                                <header className="border-b border-gray-300 pb-4 mb-4 flex items-center justify-between">
                                    <h3 className="text-lg lg:text-xl font-semibold">Pedidos por status</h3>
                                    <span className="text-sm text-secondary">Total: {data.total}</span>
                                </header>

                                {data.porStatus.length === 0 ? (
                                    <p className="text-secondary">Nenhum pedido cadastrado ainda.</p>
                                ) : (
                                    <ul className="flex flex-col gap-3">
                                        {data.porStatus.map((kpi) => (
                                            <li key={kpi.status} className="flex items-center justify-between">
                                                <span className="font-semibold">{labelStatus[kpi.status]}</span>
                                                <span className="text-secondary">
                                                    {kpi.quantidade} ({kpi.percentual}%)
                                                </span>
                                            </li>
                                        ))}
                                    </ul>
                                )}
                            </div>
                        </>
                    )}
                </div>
            </main>

            <Footer />
        </div>
    )
}

export default AdminDashboardPage
