import { useQuery } from '@tanstack/react-query'
import { PageHeader } from '@/components/layout/PageHeader'
import CardMoeda from '@/features/cotacao/components/CardMoeda'
import CardMoedaPrincipal from '@/features/cotacao/components/CardMoedaPrincipal'
import { buscarCotacoes } from '@/services/cotacao'

function CotacaoPage() {
    const { data: moedas, isLoading, isError } = useQuery({ queryKey: ['cotacoes'], queryFn: buscarCotacoes })

    const principal = moedas?.find((m) => m.sigla === 'USD')
    const outras = moedas?.filter((m) => m.sigla !== 'USD') ?? []

    return (
        <>
            <PageHeader titulo="Cotação de câmbio" subtitulo="Valores atualizados para conversão em BRL." />

            {isLoading && <p className="text-secondary">Carregando cotações...</p>}
            {isError && <p className="text-error">Não foi possível carregar as cotações.</p>}

            {!isLoading && !isError && (
                <>
                    {principal && <CardMoedaPrincipal moeda={principal} />}

                    <section className="mt-6">
                        <h2 className="mb-3 text-[20px] font-semibold leading-[28px] text-ink">Outras moedas</h2>
                        <div className="grid max-w-xl grid-cols-1 gap-4 sm:grid-cols-2">
                            {outras.map((moeda) => (
                                <CardMoeda key={moeda.sigla} moeda={moeda} />
                            ))}
                        </div>
                    </section>
                </>
            )}
        </>
    )
}

export default CotacaoPage
