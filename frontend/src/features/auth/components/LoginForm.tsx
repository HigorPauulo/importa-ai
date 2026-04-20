import { useForm } from 'react-hook-form'

type LoginFormData = {
    email: string
    password: string
}

function LoginForm() {
    const { register, handleSubmit, formState: { errors } } = useForm<LoginFormData>() 
    const onSubmit = (data: LoginFormData) => {
        console.log(data)
    }

    return (
        <div>
            <h2 className="text-2xl font-bold mb-6 text-center">Acessar Conta</h2>

            <form onSubmit={handleSubmit(onSubmit)}>
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

                <button className="w-full bg-primary text-white font-bold rounded-md p-3 cursor-pointer" type="submit">Entrar</button>
            </form>

            <p className="text-center text-sm mt-8">Não tem uma conta? <a href="/register" className="text-primary">Cadastre-se</a></p>
        </div>
    )
}

export default LoginForm