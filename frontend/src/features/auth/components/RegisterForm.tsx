import { useForm } from 'react-hook-form'
import { Button } from '../../../components/ui/Button'
import { Input } from '../../../components/ui/Input'

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
                <Input label="Nome" name="name" type="text" error={errors.name?.message as string} {...register('name', { required: 'Nome é obrigatório' })} />

                <Input label="Email" name="email" type="email" error={errors.email?.message as string} {...register('email', { required: 'Email é obrigatório' })} />

                <Input label="Senha" name="password" type="password" error={errors.password?.message as string} {...register('password', { required: 'Senha é obrigatório' })} />

                <Input label="Confirmar Senha" name="confirmPassword" type="password" error={errors.confirmPassword?.message as string} {...register('confirmPassword', { required: 'Confirmar Senha é obrigatório' })} />

                <div className="flex items-center gap-2 mb-4">
                    <input
                        className="border border-gray-300 rounded-md p-2 focus:outline-none focus:border-primary"
                        type="checkbox"
                        id="acceptTerms"
                        {...register('acceptTerms', { required: 'Obrigatório aceitar os termos.' })}
                        name="acceptTerms"
                    />
                    <label
                        className="text-sm text-gray-700 flex flex-row flex-wrap items-center gap-1 whitespace-nowrap"
                        htmlFor="acceptTerms"
                    >
                        Aceito os
                        <a href="/termos-de-uso" className="text-primary ml-1" target="_blank" rel="noopener noreferrer">
                            Termos de Uso
                        </a>
                        e a
                        <a href="/politica-de-privacidade" className="text-primary ml-1" target="_blank" rel="noopener noreferrer">
                            Política de privacidade
                        </a>
                    </label>
               
                </div>
                {errors.acceptTerms && (
                    <p className="text-xs text-red-700 mb-4">{errors.acceptTerms.message as string}</p>
                )}

                <Button type="submit" fullWidth>Cadastrar</Button>
            </form>

            <p className="text-center text-sm mt-8">Já tem uma conta? <a href="/login" className="text-primary">Acessar Conta</a></p>
        </div>
    )
}

export default RegisterForm