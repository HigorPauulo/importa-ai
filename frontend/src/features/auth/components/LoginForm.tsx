import { Link, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import type { LoginFormData } from '@/types/auth'

function LoginForm() {
    const navigate = useNavigate()
    const { register, handleSubmit, formState: { errors } } = useForm<LoginFormData>()
    const onSubmit = (data: LoginFormData) => {
        console.log(data)
        localStorage.setItem('token', 'meu-token')
        navigate('/dashboard')
    }

    return (
        <div>
            <h2 className="text-2xl font-bold mb-6 text-center">Acessar Conta</h2>

            <form onSubmit={handleSubmit(onSubmit)}>
                <Input label="Email" name="email" type="email" error={errors.email?.message} {...register('email', { required: 'Email é obrigatório' })} />

                <Input label="Senha" name="password" type="password" error={errors.password?.message} {...register('password', { required: 'Senha é obrigatório' })} />
               
                <Button type="submit" fullWidth>Entrar</Button>
            </form>

            <p className="text-center text-sm mt-8">Não tem uma conta? <Link to="/register" className="text-primary">Cadastre-se</Link></p>
        </div>
    )
}

export default LoginForm