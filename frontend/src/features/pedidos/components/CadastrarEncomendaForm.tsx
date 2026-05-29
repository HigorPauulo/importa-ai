import { useForm } from 'react-hook-form'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
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
        <div>
            <header className="mb-6">
                <h2 className="text-2xl font-bold mb-2">Cadastrar Encomenda</h2>
                <p className="text-secondary">Inicie o rastreio de sua nova importação.</p>
            </header>

            <form onSubmit={handleSubmit(onSubmit)}>
                <Input label="Código de rastreamento" name="trackingCode" type="text" error={errors.trackingCode?.message as string} {...register('trackingCode', { required: 'Código de Rastreio é obrigatório' })} placeholder="Ex: ABC123BR" />

                <Input label="Descrição do produto" name="descriptionProduct" type="text" error={errors.descriptionProduct?.message as string} {...register('descriptionProduct', { required: 'Descrição do produto é obrigatória' })} placeholder="Ex: Camiseta Nike" />

                <div className="flex flex-col lg:flex-row gap-5 mb-4">
                    <Input label="Valor declarado" name="productValue" type="number" step="0.01" error={errors.productValue?.message as string} {...register('productValue', { required: 'Valor declarado é obrigatório', valueAsNumber: true, min: { value: 0.01, message: 'Valor deve ser positivo' } })} placeholder="Ex: 100.00" />

                    <div className="flex flex-col gap-1 w-full">
                        <label className="text-sm font-bold text-secondary" htmlFor="coin">Moeda</label>
                        <select className="border border-gray-300 rounded-md p-2 focus:outline-none focus:border-primary shadow-md" id="coin" name="coin" {...register('coin', { required: 'Moeda é obrigatória' })}>
                            <option value="BRL">BRL</option>
                            <option value="USD">USD</option>
                            <option value="CNY">CNY</option>
                        </select>
                        {errors.coin && <p className="text-xs text-error">{errors.coin.message as string}</p>}
                    </div>
                </div>

                <div className="flex flex-col gap-1 mb-4">
                    <label className="text-sm font-bold text-secondary" htmlFor="platform">Plataforma (opcional)</label>
                    <select className="border border-gray-300 rounded-md p-2 focus:outline-none focus:border-primary shadow-md" id="platform" name="platform" {...register('platform')}>
                        <option value="aliexpress">Aliexpress</option>
                        <option value="amazon">Amazon</option>
                        <option value="ebay">Ebay</option>
                        <option value="other">Outro</option>
                    </select>
                </div>

                {isError && <p className="text-sm text-error mb-3">Não foi possível salvar a encomenda. Verifique os dados e tente novamente.</p>}

                <Button type="submit" fullWidth loading={isPending}>Salvar encomenda</Button>
            </form>
        </div>
    )
}

export default CadastrarEncomendaForm