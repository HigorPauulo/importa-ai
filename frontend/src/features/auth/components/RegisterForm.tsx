import { useForm } from 'react-hook-form'

type RegisterFormData = {
    name: string
    email: string
    password: string
    confirmPassword: string
    acceptTerms: boolean
}
    
function RegisterForm() {
    const { register, handleSubmit, formState: { errors } } = useForm<RegisterFormData>()
    const onSubmit = (data: RegisterFormData) => {
        console.log(data)
    }

    return (
        <div>
            <h2 className="text-2xl font-bold mb-6 text-center">Criar Conta</h2>

            <form onSubmit={handleSubmit(onSubmit)}>
                <div className="flex flex-col gap-1 mb-4">
                    <label className="text-sm font-medium text-gray-700" htmlFor="name">Nome</label>
                    <input className="border border-gray-300 rounded-md p-2 focus:outline-none focus:border-primary" type="text" id="name" name="name" {...register('name', { required: 'Nome é obrigatório' })} />
                    {errors.name && <p className="text-xs text-red-700">{errors.name.message as string}</p>}
                </div>

                <div className="flex flex-col gap-1 mb-4">
                    <label className="text-sm font-medium text-gray-700" htmlFor="email">Email</label>
                    <input className="border border-gray-300 rounded-md p-2 focus:outline-none focus:border-primary" type="email" id="email" name="email" {...register('email', { required: 'Email é obrigatório' })} />
                    {errors.email && <p className="text-xs text-red-700">{errors.email.message as string}</p>}
                </div>

                <div className="flex flex-col gap-1 mb-4">
                    <label className="text-sm font-medium text-gray-700" htmlFor="password">Senha</label>
                    <input className="border border-gray-300 rounded-md p-2 focus:outline-none focus:border-primary" type="password" id="password" name="password" {...register('password', { required: 'Senha é obrigatório' })} />
                    {errors.password && <p className="text-xs text-red-700">{errors.password.message as string}</p>}
                </div>

                <div className="flex flex-col gap-1 mb-4">
                    <label className="text-sm font-medium text-gray-700" htmlFor="confirmPassword">Confirmar Senha</label>
                    <input className="border border-gray-300 rounded-md p-2 focus:outline-none focus:border-primary" type="password" id="confirmPassword" name="confirmPassword" {...register('confirmPassword', { required: 'Confirmar Senha é obrigatório' })} />
                    {errors.confirmPassword && <p className="text-xs text-red-700">{errors.confirmPassword.message as string}</p>}
                </div>

                <div className="flex items-center gap-2 mb-4">
                    <input
                        className="border border-gray-300 rounded-md p-2 focus:outline-none focus:border-primary"
                        type="checkbox"
                        id="acceptTerms"
                        {...register('acceptTerms', { required: 'Obrigatório aceitar os termos.' })}
                        name="acceptTerms"
                    />
                    <label className="text-sm text-gray-700 flex gap-1 items-center" htmlFor="acceptTerms">
                        Aceito os&nbsp;
                        <a href="/termos-de-uso" className="text-primary" target="_blank" rel="noopener noreferrer">
                            Termos de Uso
                        </a>
                        &nbsp;e a&nbsp;
                        <a href="/politica-de-privacidade" className="text-primary" target="_blank" rel="noopener noreferrer">
                            Política de privacidade
                        </a>
                    </label>
                </div>
                {errors.acceptTerms && (
                    <p className="text-xs text-red-700 mb-4">{errors.acceptTerms.message as string}</p>
                )}
          

                <button className="w-full bg-primary text-white font-bold rounded-md p-3 cursor-pointer" type="submit">Cadastrar</button>
            </form>

            <p className="text-center text-sm mt-8">Já tem uma conta? <a href="/login" className="text-primary">Acessar Conta</a></p>
        </div>
    )
}

export default RegisterForm