import { useForm } from 'react-hook-form'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { Select } from '@/components/ui/Select'
import { criarPedido } from '@/services/pedidos'

type CadastrarEncomendaFormData = {
    trackingCode: string
    descriptionProduct: string
    productValue: number
    coin: 'BRL' | 'USD' | 'CNY'
    platform?: string
}

function CadastrarEncomendaForm() {
    const navigate = useNavigate()
    const queryClient = useQueryClient()
    const { register, handleSubmit, formState: { errors } } = useForm<CadastrarEncomendaFormData>()

    const { mutate, isPending, isError } = useMutation({
        mutationFn: criarPedido,
        onSuccess: () => {
            // invalida o cache da lista pra ela refazer o fetch já com o novo pedido
            queryClient.invalidateQueries({ queryKey: ['pedidos'] })
            navigate('/pedidos')
        },
    })

    const onSubmit = (data: CadastrarEncomendaFormData) => {
        // 'platform' fica só na UI — não existe no domínio do backend
        mutate({
            codigoRastreamento: data.trackingCode,
            descricao: data.descriptionProduct,
            valorDeclarado: data.productValue,
            moeda: data.coin,
        })
    }

    return (
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
            <Input label="Código de rastreamento" type="text" placeholder="Ex: LB123456789BR"
                   error={errors.trackingCode?.message}
                   {...register('trackingCode', {
                       required: 'Código de rastreio é obrigatório',
                       setValueAs: (v: string) => (v ?? '').replace(/\s+/g, '').toUpperCase(),
                       validate: (v: string) =>
                           /^[A-Z0-9]{8,40}$/.test(v) || 'Use 8 a 40 letras ou números, sem símbolos',
                   })} />

            <Input label="Descrição do produto" type="text" placeholder="Ex: Camiseta"
                   error={errors.descriptionProduct?.message}
                   {...register('descriptionProduct', { required: 'Descrição do produto é obrigatória' })} />

            <div className="grid grid-cols-2 gap-4">
                <Input label="Valor declarado" type="number" step="0.01" placeholder="Ex: 100.00"
                       error={errors.productValue?.message}
                       {...register('productValue', { required: 'Valor declarado é obrigatório', valueAsNumber: true, min: { value: 0.01, message: 'Valor deve ser positivo' } })} />

                <Select label="Moeda" error={errors.coin?.message}
                        {...register('coin', { required: 'Moeda é obrigatória' })}>
                    <option value="BRL">BRL</option>
                    <option value="USD">USD</option>
                    <option value="CNY">CNY</option>
                </Select>
            </div>

            <Select label="Plataforma (opcional)" {...register('platform')}>
                <option value="aliexpress">AliExpress</option>
                <option value="amazon">Amazon</option>
                <option value="ebay">eBay</option>
                <option value="other">Outro</option>
            </Select>

            {isError && (
                <p className="text-sm text-error">
                    Não foi possível salvar a encomenda. Verifique os dados e tente novamente.
                </p>
            )}

            <Button type="submit" fullWidth loading={isPending}>Salvar encomenda</Button>
        </form>
    )
}

export default CadastrarEncomendaForm
