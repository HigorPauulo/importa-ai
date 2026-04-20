import { useForm } from 'react-hook-form'

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
                <div className="flex flex-col gap-1 mb-4">
                    <label className="text-sm font-bold text-gray-700" htmlFor="trackingCode">Código de rastreamento *</label>
                    <input className="border border-gray-300 rounded-md p-2 focus:outline-none focus:border-primary" type="text" id="trackingCode" name="trackingCode" {...register('trackingCode', { required: 'Código de Rastreio é obrigatório' })} placeholder="Ex: ABC123BR" />
                    {errors.trackingCode && <p className="text-xs text-red-700">{errors.trackingCode.message as string}</p>}
                </div>

                <div className="flex flex-col gap-1 mb-4">
                    <label className="text-sm font-bold text-gray-700" htmlFor="descriptionProduct">Descrição do produto</label>
                    <input className="border border-gray-300 rounded-md p-2 focus:outline-none focus:border-primary" type="text" id="descriptionProduct" name="descriptionProduct" {...register('descriptionProduct', { required: 'Descrição do produto é obrigatória' })} placeholder="Ex: Camiseta Nike" />
                    {errors.descriptionProduct && <p className="text-xs text-red-700">{errors.descriptionProduct.message as string}</p>}
                </div>

                <div className="flex flex-col lg:flex-row gap-5 mb-4">
                    <div className="flex flex-col gap-1 w-full">
                        <label className="text-sm font-bold text-gray-700" htmlFor="productValue">Valor declarado</label>
                        <input className="border border-gray-300 rounded-md p-2 focus:outline-none focus:border-primary" type="number" id="productValue" name="productValue" {...register('productValue', { required: 'Valor declarado é obrigatório' })} placeholder="Ex: 100.00" />
                        {errors.productValue && <p className="text-xs text-red-700">{errors.productValue.message as string}</p>}
                    </div>

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

                <button className="w-full bg-primary text-white font-bold rounded-md p-3 cursor-pointer" type="submit">Salvar encomenda</button>
            </form>
        </div>
    )
}

export default CadastrarEncomendaForm