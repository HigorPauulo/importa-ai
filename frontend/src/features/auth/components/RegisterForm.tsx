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
                const { status, data } = erro.response
                if (status === 422) {
                    setErroCadastro('Este email já está cadastrado.')
                } else if (status === 400 && Array.isArray(data?.detalhes) && data.detalhes.length > 0) {
                    // surfaca o motivo real da validação do backend (ex.: senha curta)
                    setErroCadastro(data.detalhes.join('. '))
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
            <h2 className="text-2xl font-bold mb-6 text-center">Criar Conta</h2>

            <form onSubmit={handleSubmit(onSubmit)}>
                <Input label="Nome" name="name" type="text" error={errors.name?.message} {...register('name', { required: 'Nome é obrigatório' })} />

                <Input label="Email" name="email" type="email" error={errors.email?.message} {...register('email', { required: 'Email é obrigatório', pattern: { value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/, message: 'Digite um email válido' } })} />

                <Input label="Senha" name="password" type="password" hint="Mínimo de 8 caracteres" error={errors.password?.message} {...register('password', { required: 'Senha é obrigatória', minLength: { value: 8, message: 'A senha deve ter no mínimo 8 caracteres' } })} />

                <Input label="Confirmar Senha" name="confirmPassword" type="password" error={errors.confirmPassword?.message} {...register('confirmPassword', { required: 'Confirmar Senha é obrigatório', validate: (value) => value === getValues('password') || 'As senhas não correspondem' })} />

                <div className="flex items-center gap-2 mb-4">
                    <input
                        className="border border-gray-300 rounded-md p-2 focus:outline-none focus:border-primary"
                        type="checkbox"
                        id="acceptTerms"
                        {...register('acceptTerms', { required: 'Obrigatório aceitar os termos.' })}
                        name="acceptTerms"
                    />
                    <label
                        className="text-sm text-secondary flex flex-row flex-wrap items-center gap-1 whitespace-nowrap"
                        htmlFor="acceptTerms"
                    >
                        Aceito os
                        <Link to="/termos-de-uso" className="text-primary ml-1" target="_blank" rel="noopener noreferrer">
                            Termos de Uso
                        </Link>
                        {' e a '}
                        <Link to="/politica-de-privacidade" className="text-primary ml-1" target="_blank" rel="noopener noreferrer">
                            Política de Privacidade
                        </Link>
                   
                    </label>
               
                </div>
                {errors.acceptTerms && (
                    <p className="text-xs text-error mb-4">{errors.acceptTerms.message}</p>
                )}

                {erroCadastro && (
                    <div role="alert" className="bg-error-bg border border-error rounded-md p-3 mb-4">
                        <p className="text-sm text-error">{erroCadastro}</p>
                    </div>
                )}

                <Button type="submit" fullWidth loading={isSubmitting}>Cadastrar</Button>
            </form>

            <p className="text-center text-sm mt-8">Já tem uma conta? <Link to="/login" className="text-primary">Acessar Conta</Link></p>
        </div>
    )
}

export default RegisterForm