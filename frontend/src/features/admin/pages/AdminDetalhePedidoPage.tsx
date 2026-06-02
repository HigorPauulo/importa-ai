import { useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { PageHeader } from '@/components/layout/PageHeader'
import { Card } from '@/components/ui/Card'
import { useToast } from '@/context/ToastContext'
import { buscarPedidoAdmin, cancelarPedido } from '@/services/pedidos'
import { PedidoResumoCard } from '@/features/pedidos/components/PedidoResumoCard'
import { Timeline } from '@/features/pedidos/components/Timeline'
import { AvisoRastreioNaoLocalizado } from '@/features/pedidos/components/AvisoRastreioNaoLocalizado'
import { InserirEtapaForm } from '@/features/admin/components/InserirEtapaForm'

function AdminDetalhePedidoPage() {
    const { id } = useParams()
    const { showToast } = useToast()
    const queryClient = useQueryClient()

    const { data: pedido, isLoading, isError } = useQuery({
        queryKey: ['pedido', id],
        queryFn: () => buscarPedidoAdmin(id!),
        enabled: !!id,
    })

    const { mutate: cancelar, isPending: cancelando } = useMutation({
        mutationFn: () => cancelarPedido(Number(id)),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['pedido', id] })
            queryClient.invalidateQueries({ queryKey: ['admin', 'pedidos'] })
            showToast('Pedido cancelado.')
        },
        onError: () => showToast('Não foi possível cancelar o pedido.'),
    })

    const aoCancelar = () => {
        if (window.confirm('Cancelar este pedido? A ação marca o pedido como cancelado.')) cancelar()
    }

    return (
        <>
            <PageHeader titulo="Detalhes do pedido" />

            {isLoading && <p className="text-secondary">Carregando detalhes...</p>}
            {(isError || (!isLoading && !pedido)) && <p className="text-secondary">Pedido não encontrado.</p>}

            {pedido && (
                <div className="flex flex-col gap-6">
                    {pedido.rastreioNaoLocalizado && <AvisoRastreioNaoLocalizado />}

                    <div className="grid grid-cols-1 gap-6 lg:grid-cols-[420px_1fr]">
                        <div className="flex flex-col gap-5">
                            <PedidoResumoCard pedido={pedido}>
                                {pedido.status !== 'CANCELADO' && (
                                    <button
                                        type="button"
                                        onClick={aoCancelar}
                                        disabled={cancelando}
                                        className="flex w-full items-center justify-center rounded-[8px] border border-error py-[14px] text-[15px] font-medium text-error transition-colors hover:bg-error-bg disabled:opacity-50"
                                    >
                                        Cancelar pedido
                                    </button>
                                )}
                            </PedidoResumoCard>
                        </div>

                        <section className="rounded-[10px] bg-white p-6 shadow-sm">
                            <h2 className="mb-4 text-lg font-semibold text-ink">Histórico de Rastreio</h2>
                            <Timeline historico={pedido.historico} />
                        </section>
                    </div>

                    <Card className="p-6">
                        <div className="mb-[18px]">
                            <h2 className="text-lg font-semibold text-ink">Inserir etapa manual</h2>
                        </div>
                        <InserirEtapaForm pedidoId={pedido.id} />
                    </Card>
                </div>
            )}
        </>
    )
}

export default AdminDetalhePedidoPage
