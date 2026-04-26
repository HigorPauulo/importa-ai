import { Link, useParams } from 'react-router-dom'
import { pedidos } from '@/mocks/pedidos'
import Footer from '@/components/Footer'
import backIcon from '@/assets/icon-back.svg'
import starIcon from '@/assets/icon-star.svg'

function DetalhesPedidoPage() {
    const { id } = useParams()
    const pedido = pedidos.find(p => p.codigo === id)

    if (!pedido) {
        return <div>Pedido não encontrado</div>
    }

    return (
        <div className="min-h-dvh bg-background flex flex-col px-5"> 
            <header className="w-full max-w-3xl mx-auto flex items-center justify-between my-8 pt-5">
                <Link to="/pedidos">
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
                            <h2 className="text-2xl font-bold">Detalhes do pedido</h2>
                        </div>
                    </header>

                    <div className="mt-10">
                        <div className="bg-white flex flex-col gap-4 justify-between shadow-md rounded-[5px] p-5">
                            <header className="flex items-center gap-4">
                                <figure>
                                    <img src={starIcon} alt="Star" className="h-10 lg:h-16" />
                                </figure>

                                <div>
                                    <h3 className="text-lg lg:text-xl font-semibold mb-1">{pedido.produto}</h3>
                                    <span className="text-base text-secondary">{pedido.codigo}</span>
                                </div>
                            </header>

                            <div className="flex items-center justify-between mt-5 border-t border-primary-light pt-4">
                                <div className="text-center">
                                    <span className="text-sm text-secondary uppercase">Origem</span>
                                    <p className="text-base font-bold">{pedido.origem}</p>
                                </div>

                                <div className="text-center">
                                    <span className="text-sm text-secondary uppercase">Valor estimado</span>
                                    <p className="text-base font-bold">{pedido.valorEstimado?.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}</p>
                                </div>
                            </div>
                        </div>

                        <div className="mt-10">
                            <h3 className="text-lg lg:text-xl font-semibold mb-4">Histórico de Rastreio</h3>

                            <div className="relative pl-6">
                                {/* Linha azul na esquerda */}
                                <div className="absolute left-2.5 top-0.7 h-full w-0.5 bg-primary-light rounded z-0" style={{ minHeight: '100%' }} />

                                <ul className="flex flex-col gap-8 relative z-10">
                                    {pedido.historico?.map((etapa, idx) => {
                                        const isFirst = idx === 0;

                                        return (
                                            <li
                                                key={etapa.data + etapa.hora + etapa.descricao + etapa.local}
                                                className="relative"
                                            >
                                                {/* Círculo de etapa */}
                                                <span
                                                    className={
                                                        "absolute -left-5.5 top-0.5 w-4 h-4 rounded-full border-2 " +
                                                        (isFirst
                                                            ? "bg-primary border-white"
                                                            : "bg-secondary border-white")
                                                    }
                                                />

                                                {/* Card e conteúdo */}
                                                <div
                                                    className={
                                                        isFirst
                                                            ? "bg-white flex flex-col gap-1 shadow-md rounded-[5px] p-5 ml-2"
                                                            : "ml-2"
                                                    }
                                                >
                                                    <div className={
                                                        "text-sm " +
                                                        (isFirst
                                                            ? "text-primary text-base"
                                                            : "text-secondary text-sm")
                                                    }>
                                                        <span className="font-bold">{etapa.data} - {etapa.hora}</span>
                                                    </div>
                                                    <p className={"text-base font-bold" + (isFirst ? "" : " text-secondary")}>
                                                        {etapa.descricao}
                                                    </p>
                                                    <p className="text-base text-secondary">{etapa.local}</p>
                                                </div>
                                            </li>
                                        );
                                    })}
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>
            </main>

            <Footer />
        </div>
    )
}

export default DetalhesPedidoPage