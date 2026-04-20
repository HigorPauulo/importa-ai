import { useForm } from 'react-hook-form'
import { Button } from '../../../components/ui/Button'
import { Input } from '../../../components/ui/Input'

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
                <Input label="Email" name="email" type="email" error={errors.email?.message as string} {...register('email', { required: 'Email é obrigatório' })} />

                <Input label="Senha" name="password" type="password" error={errors.password?.message as string} {...register('password', { required: 'Senha é obrigatório' })} />
               
                <Button type="submit" fullWidth>Entrar</Button>
            </form>

            <p className="text-center text-sm mt-8">Não tem uma conta? <a href="/register" className="text-primary">Cadastre-se</a></p>
        </div>
    )
}

export default LoginForm