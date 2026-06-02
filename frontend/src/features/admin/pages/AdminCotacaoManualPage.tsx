import { useForm } from 'react-hook-form'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { PageHeader } from '@/components/layout/PageHeader'
import { Card } from '@/components/ui/Card'
import { Input } from '@/components/ui/Input'
import { Select } from '@/components/ui/Select'
import { Button } from '@/components/ui/Button'
import { useToast } from '@/context/ToastContext'
import { buscarCotacoes, definirCotacaoManual } from '@/services/cotacao'
import type { TipoMoeda } from '@/types/moeda'

interface FormData {
    par: TipoMoeda
    taxa: number
    validade?: string
    confirmarIncomum?: boolean
}

function AdminCotacaoManualPage() {
    const { showToast } = useToast()
    const queryClient = useQueryClient()
    const { register, handleSubmit, reset, formState: { errors } } = useForm<FormData>({ defaultValues: { par: 'USD' } })

    const { data: cotacoes = [] } = useQuery({ queryKey: ['cotacoes'], queryFn: buscarCotacoes })

    const { mutate, isPending } = useMutation({
        mutationFn: (data: FormData) =>
            definirCotacaoManual({
                moedaOrigem: data.par,
                taxa: data.taxa,
                validoAte: data.validade ? new Date(`${data.validade}T00:00:00`).toISOString() : undefined,
            }),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['cotacoes'] })
            showToast('Cotação manual salva. Ela sobrescreve a automática para o par escolhido.')
            reset({ par: 'USD' })
        },
        onError: () => showToast('Não foi possível salvar a cotação manual.'),
    })

    return (
        <>
            <PageHeader titulo="Cotação manual" subtitulo="Defina manualmente a taxa de câmbio de um par de moedas" />

            <div className="flex flex-col gap-6">
                <Card className="p-6">
                    <div className="mb-[18px]">
                        <h2 className="text-lg font-semibold text-ink">Nova cotação manual</h2>
                        <p className="mt-1 text-sm text-secondary">
                            A cotação manual sobrescreve a automática para o par escolhido até ser removida.
                        </p>
                    </div>

                    <form onSubmit={handleSubmit((data) => mutate(data))} className="space-y-[18px]">
                        <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
                            <Select label="Par de moedas" {...register('par', { required: true })}>
                                <option value="USD">USD / BRL</option>
                                <option value="CNY">CNY / BRL</option>
                                <option value="EUR">EUR / BRL</option>
                            </Select>

                            <Input label="Taxa (R$)" type="number" step="0.0001" placeholder="5,02"
                                   error={errors.taxa?.message}
                                   {...register('taxa', { required: 'Informe a taxa', valueAsNumber: true, min: { value: 0.0001, message: 'A taxa deve ser positiva' } })} />

                            <Input label="Validade (opcional)" type="date" {...register('validade')} />
                        </div>

                        <label className="flex items-center gap-[10px] text-[13px] text-secondary" htmlFor="confirmarIncomum">
                            <input
                                type="checkbox"
                                id="confirmarIncomum"
                                className="h-[18px] w-[18px] rounded-[4px] border-secondary text-primary focus:ring-primary/30"
                                {...register('confirmarIncomum')}
                            />
                            Confirmar valor incomum (fora de ±50% sobre a cotação automática)
                        </label>

                        <div>
                            <Button type="submit" loading={isPending}>Salvar cotação manual</Button>
                        </div>
                    </form>
                </Card>

                <Card className="overflow-hidden">
                    <div className="px-5 py-4">
                        <h2 className="text-lg font-semibold text-primary-dark">Cotações atuais</h2>
                    </div>
                    {cotacoes.map((moeda) => (
                        <div key={moeda.sigla} className="flex items-center border-t border-gray-100 px-5 py-[14px]">
                            <span className="w-[180px] text-[14px] font-medium text-primary-dark">{moeda.sigla} / BRL</span>
                            <span className="w-[120px] text-[16px] font-medium text-primary-dark">
                                {moeda.valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}
                            </span>
                            <div className="w-[140px]">
                                <span className="inline-flex rounded-full bg-background px-[10px] py-1 text-[11px] font-bold text-secondary">
                                    Automática
                                </span>
                            </div>
                            <span className="flex-1 text-[13px] text-secondary">Sincronizado há {moeda.atualizacao}</span>
                            <button type="button" className="rounded-[8px] bg-primary-light px-3 py-1.5 text-[12px] text-primary-dark hover:opacity-80">
                                Tornar manual
                            </button>
                        </div>
                    ))}
                    {cotacoes.length === 0 && (
                        <p className="border-t border-gray-100 px-5 py-3 text-sm text-secondary">Sem cotações disponíveis.</p>
                    )}
                </Card>
            </div>
        </>
    )
}

export default AdminCotacaoManualPage
