import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router-dom'
import { isAxiosError } from 'axios'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import * as authService from '@/services/auth'
import type { RegisterFormData } from '@/types/auth'

function RegisterForm() {
    const navigate = useNavigate()
    // onTouched: valida ao sair do campo, então o erro aparece sem esperar o submit
    const { register, handleSubmit, getValues, formState: { errors, isSubmitting } } =
        useForm<RegisterFormData>({ mode: 'onTouched' })
    const [erroCadastro, setErroCadastro] = useState<string | null>(null)

    const onSubmit = async (data: RegisterFormData) => {
        setErroCadastro(null)
        try {
            await authService.register(data.name, data.email, data.password)
            navigate('/login')
        } catch (erro) {
            if (isAxiosError(erro) && erro.response) {
                const { status, data: corpo } = erro.response
                if (status === 422) {
                    setErroCadastro('Este email já está cadastrado.')
                } else if (status === 400 && Array.isArray(corpo?.detalhes) && corpo.detalhes.length > 0) {
                    // surfaca o motivo real da validação do backend (ex.: senha curta)
                    setErroCadastro(corpo.detalhes.join('. '))
                } else {
                    setErroCadastro('Não foi possível concluir o cadastro. Tente novamente.')
                }
            } else {
                setErroCadastro('Não foi possível concluir o cadastro. Tente novamente.')
            }
        }
    }

    return (
        <div>
            <h2 className="text-[28px] font-bold leading-[36px] text-ink">Criar Conta</h2>

            <form onSubmit={handleSubmit(onSubmit)} className="mt-5 space-y-4">
                <Input label="Nome completo" type="text" placeholder="Diogo Oliveira" error={errors.name?.message}
                       {...register('name', { required: 'Nome é obrigatório' })} />

                <Input label="E-mail" type="email" placeholder="seuemail@exemplo.com" error={errors.email?.message}
                       {...register('email', { required: 'Email é obrigatório', pattern: { value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/, message: 'Digite um email válido' } })} />

                <Input label="Senha" type="password" placeholder="••••••••••" hint="Mínimo de 8 caracteres" error={errors.password?.message}
                       {...register('password', { required: 'Senha é obrigatória', minLength: { value: 8, message: 'A senha deve ter no mínimo 8 caracteres' } })} />

                <Input label="Confirmar Senha" type="password" placeholder="••••••••••" error={errors.confirmPassword?.message}
                       {...register('confirmPassword', { required: 'Confirmar Senha é obrigatório', validate: (value) => value === getValues('password') || 'As senhas não correspondem' })} />

                <div>
                    <label className="flex items-start gap-2 text-sm text-secondary" htmlFor="acceptTerms">
                        <input
                            type="checkbox"
                            id="acceptTerms"
                            className="mt-0.5 h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary/30"
                            {...register('acceptTerms', { required: 'Obrigatório aceitar os termos.' })}
                        />
                        <span>
                            Aceito os{' '}
                            <Link to="/termos-de-uso" className="text-primary hover:underline" target="_blank" rel="noopener noreferrer">Termos de Uso</Link>
                            {' e a '}
                            <Link to="/politica-de-privacidade" className="text-primary hover:underline" target="_blank" rel="noopener noreferrer">Política de Privacidade</Link>
                        </span>
                    </label>
                    {errors.acceptTerms && <p className="mt-1 text-xs text-error">{errors.acceptTerms.message}</p>}
                </div>

                {erroCadastro && (
                    <div role="alert" className="rounded-[5px] border border-error bg-error-bg p-3">
                        <p className="text-sm text-error">{erroCadastro}</p>
                    </div>
                )}

                <Button type="submit" fullWidth loading={isSubmitting}>Cadastrar</Button>
            </form>

            <p className="mt-5 text-[13px] text-secondary">
                Já tem conta? <Link to="/login" className="font-semibold text-primary hover:underline">Faça login</Link>
            </p>
        </div>
    )
}

export default RegisterForm
