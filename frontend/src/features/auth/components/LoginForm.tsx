import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { useAuth } from '@/context/AuthContext'
import type { LoginFormData } from '@/types/auth'

function LoginForm() {
    const navigate = useNavigate()
    const { login } = useAuth()
    const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<LoginFormData>()
    const [erroLogin, setErroLogin] = useState<string | null>(null)

    const onSubmit = async (data: LoginFormData) => {
        setErroLogin(null)
        try {
            const perfil = await login(data.email, data.senha)
            navigate(perfil === 'ADMINISTRADOR' ? '/admin/dashboard' : '/dashboard')
        } catch {
            setErroLogin('Email ou senha inválidos.')
        }
    }

    return (
        <div>
            <h2 className="text-2xl font-bold mb-6 text-center">Acessar Conta</h2>

            <form onSubmit={handleSubmit(onSubmit)}>
                <Input label="Email" name="email" type="email" error={errors.email?.message}
                       {...register('email', { required: 'Email é obrigatório' })} />

                <Input label="Senha" name="senha" type="password" error={errors.senha?.message}
                       {...register('senha', { required: 'Senha é obrigatória' })} />

                {erroLogin && (
                    <div role="alert" className="bg-error-bg border border-error rounded-md p-3 mb-4">
                        <p className="text-sm text-error">{erroLogin}</p>
                    </div>
                )}

                <Button type="submit" fullWidth loading={isSubmitting}>Entrar</Button>
            </form>

            <p className="text-center text-sm mt-8">Não tem uma conta?<Link to="/register" className="text-primary">Cadastre-se</Link></p>
        </div>
    )
}

export default LoginForm
