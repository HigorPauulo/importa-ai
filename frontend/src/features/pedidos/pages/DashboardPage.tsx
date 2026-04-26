import Header from '@/components/Header'
import Footer from '@/components/Footer'
import notificationIcon from '@/assets/icon-notification.svg'
import { Link } from 'react-router-dom'
import { getEtapaColor, getEtapaLabel } from '@/features/pedidos/utils/statusUtils'
import { user, pedidos } from '@/mocks/pedidos'

function DashboardPage() {
    return (
        <div className="min-h-dvh bg-background flex flex-col">
            <Header />

            <main className="flex-1 flex justify-center">
                <div className="w-full max-w-3xl px-5">
                    <header className="flex items-center justify-between my-8">
                        <h1 className="text-2xl lg:text-3xl font-bold">Olá, {user.name}</h1>

                        <div className="w-10 lg:w-13 h-10 lg:h-13 bg-white shadow-md rounded-[5px] flex items-center justify-center relative">
                            <span className="absolute top-[-10px] right-[-10px] w-5 lg:w-7 h-5 lg:h-7 text-xs text-white font-bold bg-red-500 rounded-full flex items-center justify-center">
                                {user.notifications}
                            </span>
                       
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
                            <h3 className="text-base lg:text-lg text-gray-500 mb-4">Cotação Yuan</h3>
                            <p className="text-2xl lg:text-3xl font-bold"><span className="text-xl lg:text-2xl">R$</span> 100,00</p>
                            <Link className="text-primary text-base lg:text-lg text-center block pt-4" to="/cotacao">Ver mais</Link>
                        </div>

                        <div className="bg-white shadow-md rounded-[5px] p-5">
                            <h3 className="text-base lg:text-lg text-gray-500 mb-4">Encomendas ativas</h3>
                            <p className="text-2xl lg:text-3xl font-bold text-primary">10</p>
                        </div>
                    </div>

                    <div className="bg-white shadow-md rounded-[5px] p-5">
                        <header className="border-b border-gray-300 pb-5 mb-5">
                            <h3 className="text-1xl lg:text-2xl font-medium">Últimas Atualizações</h3>
                        </header>

                        <ul>
                            {pedidos.map((pedido) => (
                                <li key={pedido.codigo} className="border-b border-gray-300 pb-4 mb-4">
                                    <div className="flex items-end justify-between mb-6">
                                        <span className="text-xs lg:text-sm bg-gray-200 text-gray-500 px-2 py-1 rounded-[5px] font-bold">{pedido.codigo}</span>

                                        <p className={`text-xs px-2 py-1 rounded-[5px] font-bold ${getEtapaColor(pedido.etapa)}`}>{getEtapaLabel(pedido.etapa)}</p>
                                    </div>

                                    <p className="text-lg lg:text-xl">{pedido.produto}</p>

                                    <div className="flex items-center gap-2 mt-1 text-sm lg:text-base text-gray-500">
                                        <span>Atualizado em:</span><span>{pedido.atualizacao}</span> - <span>{pedido.cidade}</span>
                                    </div>
                                </li>
                            ))}
                        </ul>

                        <Link className="text-primary text-base lg:text-lg text-center block pt-4" to="/pedidos">Ver mais encomendas</Link>
                    </div>
                </div>
            </main>

            <Footer />
        </div>
    )
}

export default DashboardPage