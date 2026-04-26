import { useForm } from 'react-hook-form'
import { Link } from 'react-router-dom'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import type { RegisterFormData } from '@/types/auth'

function RegisterForm() {
    const { register, handleSubmit, getValues, formState: { errors } } = useForm<RegisterFormData>()
    const onSubmit = (data: RegisterFormData) => {
        console.log(data)
    }

    return (
        <div>
            <h2 className="text-2xl font-bold mb-6 text-center">Criar Conta</h2>

            <form onSubmit={handleSubmit(onSubmit)}>
                <Input label="Nome" name="name" type="text" error={errors.name?.message} {...register('name', { required: 'Nome é obrigatório' })} />

                <Input label="Email" name="email" type="email" error={errors.email?.message} {...register('email', { required: 'Email é obrigatório' })} />

                <Input label="Senha" name="password" type="password" error={errors.password?.message} {...register('password', { required: 'Senha é obrigatório' })} />

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

                <Button type="submit" fullWidth>Cadastrar</Button>
            </form>

            <p className="text-center text-sm mt-8">Já tem uma conta? <Link to="/login" className="text-primary">Acessar Conta</Link></p>
        </div>
    )
}

export default RegisterForm