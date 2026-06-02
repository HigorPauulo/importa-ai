import { Link, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { IconChevronLeft } from '@/components/layout/icons'
import { HeaderActions } from '@/components/layout/HeaderActions'
import { Card } from '@/components/ui/Card'
import { Button } from '@/components/ui/Button'
import { useToast } from '@/context/ToastContext'
import { buscarPedidoAdmin } from '@/services/pedidos'
import { PedidoResumoCard } from '@/features/pedidos/components/PedidoResumoCard'
import { Timeline } from '@/features/pedidos/components/Timeline'
import { AvisoRastreioNaoLocalizado } from '@/features/pedidos/components/AvisoRastreioNaoLocalizado'
import { InserirEtapaForm } from '@/features/admin/components/InserirEtapaForm'

function AdminDetalhePedidoPage() {
    const { id } = useParams()
    const { showToast } = useToast()

    const { data: pedido, isLoading, isError } = useQuery({
        queryKey: ['pedido', id],
        queryFn: () => buscarPedidoAdmin(id!),
        enabled: !!id,
    })

    const compartilhar = async () => {
        try {
            await navigator.clipboard.writeText(window.location.href)
            showToast('Link de rastreio copiado para a área de transferência.')
        } catch {
            showToast('Não foi possível copiar o link.')
        }
    }

    return (
        <>
            <div className="mb-6 flex items-center gap-2">
                <Link to="/admin/pedidos" aria-label="Voltar" className="-ml-1 text-secondary hover:text-ink">
                    <IconChevronLeft className="h-7 w-7" />
                </Link>
                <h1 className="flex-1 text-[22px] font-bold leading-[30px] text-ink lg:text-[28px] lg:leading-[36px]">Detalhes do pedido</h1>
                <div className="lg:hidden">
                    <HeaderActions />
                </div>
            </div>

            {isLoading && <p className="text-secondary">Carregando detalhes...</p>}
            {(isError || (!isLoading && !pedido)) && <p className="text-secondary">Pedido não encontrado.</p>}

            {pedido && (
                <div className="flex flex-col gap-6">
                    {pedido.rastreioNaoLocalizado && <AvisoRastreioNaoLocalizado />}

                    <div className="grid grid-cols-1 gap-6 lg:grid-cols-[420px_1fr]">
                        <PedidoResumoCard pedido={pedido}>
                            <Button variant="primary" fullWidth onClick={compartilhar}>
                                Compartilhar Rastreio
                            </Button>
                        </PedidoResumoCard>

                        <section className="rounded-[10px] bg-white p-6 shadow-[0px_1px_2px_rgba(0,0,0,0.08)]">
                            <h2 className="mb-4 text-[18px] font-semibold leading-[26px] text-ink">Histórico de Rastreio</h2>
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
