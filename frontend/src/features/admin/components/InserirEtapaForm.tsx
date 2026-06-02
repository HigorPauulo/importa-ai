import { useForm } from 'react-hook-form'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { Select } from '@/components/ui/Select'
import { useToast } from '@/context/ToastContext'
import { inserirEtapa } from '@/services/pedidos'
import { getEtapaLabel } from '@/features/pedidos/utils/statusUtils'
import type { TipoEtapa } from '@/types/pedidos'

const TIPOS: TipoEtapa[] = [
    'NA_CHINA',
    'AEROPORTO_ORIGEM',
    'EM_TRANSITO',
    'AEROPORTO_DESTINO',
    'NO_BRASIL',
    'TAXA',
    'CD_BRASIL',
    'SAIDA_ENTREGA',
    'ENTREGUE',
]

interface FormData {
    tipo: TipoEtapa
    descricao: string
    localizacao?: string
    dataHora?: string
}

export function InserirEtapaForm({ pedidoId }: { pedidoId: number }) {
    const queryClient = useQueryClient()
    const { showToast } = useToast()
    const { register, handleSubmit, reset, formState: { errors } } = useForm<FormData>()

    const { mutate, isPending } = useMutation({
        mutationFn: (data: FormData) => inserirEtapa(pedidoId, data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['pedido', String(pedidoId)] })
            queryClient.invalidateQueries({ queryKey: ['admin', 'pedidos'] })
            showToast('Etapa registrada. O status será atualizado em instantes.')
            reset()
        },
        onError: () => showToast('Não foi possível registrar a etapa.'),
    })

    return (
        <form onSubmit={handleSubmit((data) => mutate(data))} className="space-y-[18px]">
            <p className="text-sm text-secondary">
                Registre uma etapa não capturada automaticamente — é a única forma de promover o status.
            </p>

            <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
                <Select label="Tipo de etapa" error={errors.tipo?.message}
                        {...register('tipo', { required: 'Selecione o tipo' })}>
                    {TIPOS.map((tipo) => (
                        <option key={tipo} value={tipo}>{getEtapaLabel(tipo)}</option>
                    ))}
                </Select>

                <Input label="Localização (opcional)" placeholder="Cidade / UF"
                       {...register('localizacao')} />

                <Input label="Data e hora (opcional)" type="datetime-local"
                       {...register('dataHora')} />
            </div>

            <Input label="Descrição" placeholder="Descreva a etapa registrada..."
                   error={errors.descricao?.message}
                   {...register('descricao', { required: 'Descrição é obrigatória' })} />

            <div>
                <Button type="submit" loading={isPending}>Registrar etapa</Button>
            </div>
        </form>
    )
}
