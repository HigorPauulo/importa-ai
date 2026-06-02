import RegisterForm from '@/features/auth/components/RegisterForm'
import { AuthLayout } from '@/components/layout/AuthLayout'

function RegisterPage() {
    return (
        <AuthLayout
            titulo="Crie sua conta"
            subtitulo="Junte-se ao Importa Aí e acompanhe todas as suas importações em um só lugar."
        >
            <RegisterForm />
        </AuthLayout>
    )
}

export default RegisterPage
