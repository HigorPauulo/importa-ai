import { useState } from 'react'
import { PageHeader } from '@/components/layout/PageHeader'
import CardNotificacao from '@/features/notificacoes/components/CardNotificacao'
import { useNotificacoes } from '@/context/NotificacaoContext'

// Sem paginação no backend (GET devolve tudo, máx. 50): "carregar antigas" é
// revelação incremental no cliente, em lotes deste tamanho.
const LOTE = 8

function NotificacoesPage() {
    const { notificacoes, naoLidas, carregando, erro, marcarTodasComoLidas } = useNotificacoes()
    const [visiveis, setVisiveis] = useState(LOTE)

    const exibidas = notificacoes.slice(0, visiveis)
    const temMais = notificacoes.length > visiveis

    return (
        <>
            <PageHeader
                titulo="Notificações"
                acao={
                    naoLidas > 0 ? (
                        <button
                            type="button"
                            onClick={() => marcarTodasComoLidas()}
                            className="text-[13px] font-semibold text-primary hover:underline"
                        >
                            Marcar todas como lidas
                        </button>
                    ) : undefined
                }
            />

            <div className="max-w-3xl">
                {naoLidas > 0 && (
                    <div className="mb-3 flex justify-end lg:hidden">
                        <button
                            type="button"
                            onClick={() => marcarTodasComoLidas()}
                            className="text-[13px] font-semibold text-primary hover:underline"
                        >
                            Marcar todas como lidas
                        </button>
                    </div>
                )}
                {carregando && <p className="text-secondary">Carregando notificações...</p>}
                {erro && <p className="text-error">Não foi possível carregar as notificações.</p>}

                {!carregando && !erro && notificacoes.length === 0 && (
                    <div className="rounded-xl border border-gray-200 bg-white p-10 text-center text-secondary">
                        Você ainda não tem notificações.
                    </div>
                )}

                {!carregando && !erro && notificacoes.length > 0 && (
                    <>
                        <ul className="flex flex-col gap-3">
                            {exibidas.map((notificacao) => (
                                <li key={notificacao.id}>
                                    <CardNotificacao notificacao={notificacao} />
                                </li>
                            ))}
                        </ul>

                        {temMais && (
                            <button
                                type="button"
                                onClick={() => setVisiveis((atual) => atual + LOTE)}
                                className="mt-4 w-full rounded-xl border-2 border-dashed border-gray-300 py-3 text-sm font-semibold text-secondary transition-colors hover:bg-white"
                            >
                                Carregar notificações antigas
                            </button>
                        )}
                    </>
                )}
            </div>
        </>
    )
}

export default NotificacoesPage
