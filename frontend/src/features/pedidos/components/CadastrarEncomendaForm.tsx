import { useForm } from 'react-hook-form'
import { Button } from '../../../components/ui/Button'
import { Input } from '../../../components/ui/Input'

type CadastrarEncomendaFormData = {
    trackingCode: string
    descriptionProduct: string
    productValue: number
    coin: 'BRL' | 'USD' | 'CNY'
    platform?: string
}

function CadastrarEncomendaForm() {
    const { register, handleSubmit, formState: { errors } } = useForm<CadastrarEncomendaFormData>()
    const onSubmit = (data: CadastrarEncomendaFormData) => {
        console.log(data)
    }

    return (
        <div>
            <header className="mb-6">
                <h2 className="text-2xl font-bold mb-2">Cadastrar Encomenda</h2>
                <p className="text-gray-500">Inicie o rastreio de sua nova importação.</p>
            </header>

            <form onSubmit={handleSubmit(onSubmit)}>
                <Input label="Código de rastreamento" name="trackingCode" type="text" error={errors.trackingCode?.message as string} {...register('trackingCode', { required: 'Código de Rastreio é obrigatório' })} placeholder="Ex: ABC123BR" />

                <Input label="Descrição do produto" name="descriptionProduct" type="text" error={errors.descriptionProduct?.message as string} {...register('descriptionProduct', { required: 'Descrição do produto é obrigatória' })} placeholder="Ex: Camiseta Nike" />

                <div className="flex flex-col lg:flex-row gap-5 mb-4">
                    <Input label="Valor declarado" name="productValue" type="number" error={errors.productValue?.message as string} {...register('productValue', { required: 'Valor declarado é obrigatório' })} placeholder="Ex: 100.00" />

                    <div className="flex flex-col gap-1 w-full">
                        <label className="text-sm font-bold text-gray-700" htmlFor="coin">Moeda</label>
                        <select className="border border-gray-300 rounded-md p-2 focus:outline-none focus:border-primary" id="coin" name="coin" {...register('coin', { required: 'Moeda é obrigatória' })}>
                            <option value="BRL">BRL</option>
                            <option value="USD">USD</option>
                            <option value="CNY">CNY</option>
                        </select>
                        {errors.coin && <p className="text-xs text-red-700">{errors.coin.message as string}</p>}
                    </div>
                </div>

                <div className="flex flex-col gap-1 mb-4">
                    <label className="text-sm font-bold text-gray-700" htmlFor="platform">Plataforma (opcional)</label>
                    <select className="border border-gray-300 rounded-md p-2 focus:outline-none focus:border-primary" id="platform" name="platform" {...register('platform')}>
                        <option value="aliexpress">Aliexpress</option>
                        <option value="amazon">Amazon</option>
                        <option value="ebay">Ebay</option>
                        <option value="other">Outro</option>
                    </select>
                </div>

                <Button type="submit" fullWidth>Salvar encomenda</Button>
            </form>
        </div>
    )
}

export default CadastrarEncomendaForm