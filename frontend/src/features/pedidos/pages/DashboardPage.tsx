import Header from '../../../components/Header'
import Footer from '../../../components/Footer'
import notificationIcon from '../../../assets/icon-notification.svg'

function DashboardPage() {
    const user = {
        name: 'Higor',
        notifications: 10
    }

    const pedidos = [
        {
            codigo: 'XZY123BR',
            status: 'No Brasil',
            produto: 'Teclado Mecanico Machenik',
            atualizacao: '14:45',
            cidade: 'Goiânia',
        },
        {
            codigo: 'ABC123BR',
            status: 'Taxa pendente',
            produto: 'Camiseta',
            atualizacao: '08:20',
            cidade: 'São Paulo',
        },
        {
            codigo: 'DEF123BR',
            status: 'Em Trânsito',
            produto: 'Notebook',
            atualizacao: '10:15',
            cidade: 'Belo Horizonte',
        },
        {
            codigo: 'GHI123BR',
            status: 'Aeroporto Destino',
            produto: 'Celular',
            atualizacao: '12:00',
            cidade: 'Rio de Janeiro',
        }
    ]

    function getStatusColor(status: string) {
        switch (status) {
            case 'Na China':
                return 'bg-blue-50 text-blue-700 border border-blue-200'
            case 'Aeroporto Origem':
                return 'bg-cyan-100 text-cyan-800 border border-cyan-200'
            case 'Em Trânsito':
                return 'bg-amber-50 text-amber-700 border border-amber-200'
            case 'Aeroporto Destino':
                return 'bg-violet-50 text-violet-700 border border-violet-200'
            case 'No Brasil':
                return 'bg-green-50 text-green-700 border border-green-200'
            case 'CD_BRASIL':
                return 'bg-lime-50 text-lime-700 border border-lime-200'
            case 'Saida Entrega':
                return 'bg-yellow-50 text-yellow-700 border border-yellow-200'
            case 'Entregue':
                return 'bg-primary/10 text-primary border border-primary/30'
            case 'Taxa pendente':
                return 'bg-orange-100 text-orange-900 border border-orange-200'
            case 'Cancelado':
                return 'bg-red-50 text-red-700 border border-red-200 line-through'
            default:
                return 'bg-neutral-50 text-neutral-700 border border-neutral-200'
        }
    }

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
                        <a className="w-full bg-primary text-white text-lg lg:text-xl p-4 rounded-[5px] flex justify-center items-center gap-1 shadow-sm" href="/pedidos/criar">
                            <span className="text-2xl">+</span>
                            <span>Cadastrar nova encomenda</span>
                        </a>
                    </div>

                    <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 mb-8">
                        <div className="bg-white shadow-md rounded-[5px] p-5">
                            <h3 className="text-base lg:text-lg text-gray-500 mb-4">Cotação Yuan</h3>
                            <p className="text-2xl lg:text-3xl font-bold"><span className="text-xl lg:text-2xl">R$</span> 100,00</p>
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

                                        <p className={`text-xs px-2 py-1 rounded-[5px] font-bold ${getStatusColor(pedido.status)}`}>{pedido.status}</p>
                                    </div>

                                    <p className="text-lg lg:text-xl">{pedido.produto}</p>

                                    <div className="flex items-center gap-2 mt-1 text-sm lg:text-base text-gray-500">
                                        <span>Atualizado em:</span><span>{pedido.atualizacao}</span> - <span>{pedido.cidade}</span>
                                    </div>
                                </li>
                            ))}
                        </ul>

                        <a className="text-primary text-base lg:text-lg text-center block pt-4" href="/pedidos">Ver mais encomendas</a>
                    </div>
                </div>
            </main>

            <Footer />
        </div>
    )
}

export default DashboardPage