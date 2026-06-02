import { Link, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { buscarPedido } from '@/services/pedidos'
import { buscarCotacao } from '@/services/cotacao'
import { useToast } from '@/context/ToastContext'
import { PageHeader } from '@/components/layout/PageHeader'
import { Button } from '@/components/ui/Button'
import { PedidoResumoCard } from '@/features/pedidos/components/PedidoResumoCard'
import { Timeline } from '@/features/pedidos/components/Timeline'

function DetalhesPedidoPage() {
    const { id } = useParams()
    const { showToast } = useToast()

    const { data: pedido, isLoading, isError } = useQuery({
        queryKey: ['pedido', id],
        queryFn: () => buscarPedido(id!),
        enabled: !!id,
    })

    const moeda = pedido?.moeda
    const { data: cotacao } = useQuery({
        queryKey: ['cotacao', moeda],
        queryFn: () => buscarCotacao(moeda!),
        enabled: !!moeda && moeda !== 'BRL',
    })

    const valorBrl =
        pedido?.valorEstimado != null && cotacao
            ? (pedido.valorEstimado * cotacao.valor).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
            : undefined

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
            <Link to="/pedidos" className="mb-2 inline-block text-sm text-secondary hover:text-primary">
                ← Voltar para encomendas
            </Link>
            <PageHeader titulo="Detalhes do pedido" />

            {isLoading && <p className="text-secondary">Carregando detalhes...</p>}
            {(isError || (!isLoading && !pedido)) && <p className="text-secondary">Pedido não encontrado.</p>}

            {pedido && (
                <div className="grid grid-cols-1 gap-6 lg:grid-cols-[minmax(0,400px)_1fr]">
                    <PedidoResumoCard pedido={pedido} valorBrl={valorBrl}>
                        <Button variant="primary" fullWidth onClick={compartilhar}>
                            Compartilhar Rastreio
                        </Button>
                    </PedidoResumoCard>

                    <section className="rounded-[10px] bg-white p-6 shadow-[0px_1px_2px_rgba(0,0,0,0.08)]">
                        <h2 className="mb-4 text-[18px] font-semibold leading-[26px] text-ink">Histórico de Rastreio</h2>
                        <Timeline historico={pedido.historico} />
                    </section>
                </div>
            )}
        </>
    )
}

export default DetalhesPedidoPage
