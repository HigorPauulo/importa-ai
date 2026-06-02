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
            navigate(perfil === 'ADMINISTRADOR' ? '/admin' : '/dashboard')
        } catch {
            setErroLogin('Email ou senha inválidos.')
        }
    }

    return (
        <div>
            <h2 className="text-[28px] font-bold leading-[36px] text-ink">Acessar Conta</h2>
            <p className="mt-1 text-[15px] leading-[22px] text-secondary">Bem-vindo de volta! Entre com suas credenciais.</p>

            <form onSubmit={handleSubmit(onSubmit)} className="mt-5 space-y-5">
                <Input label="E-mail" type="email" placeholder="seuemail@exemplo.com" error={errors.email?.message}
                       {...register('email', { required: 'Email é obrigatório' })} />

                <Input label="Senha" type="password" placeholder="••••••••••" error={errors.senha?.message}
                       {...register('senha', { required: 'Senha é obrigatória' })} />

                <div className="flex justify-end">
                    <button type="button" className="text-[13px] font-semibold text-primary hover:underline">
                        Esqueceu sua senha?
                    </button>
                </div>

                {erroLogin && (
                    <div role="alert" className="rounded-[5px] border border-error bg-error-bg p-3">
                        <p className="text-sm text-error">{erroLogin}</p>
                    </div>
                )}

                <Button type="submit" fullWidth loading={isSubmitting}>Entrar</Button>
            </form>

            <p className="mt-5 text-[13px] text-secondary">
                Não tem uma conta? <Link to="/register" className="font-semibold text-primary hover:underline">Cadastre-se</Link>
            </p>
        </div>
    )
}

export default LoginForm
