import LoginForm from '@/features/auth/components/LoginForm'
import { AuthLayout } from '@/components/layout/AuthLayout'

function LoginPage() {
    return (
        <AuthLayout
            titulo="Importa Aí"
            subtitulo="Rastreie suas encomendas internacionais do corredor China-Brasil em tempo real."
        >
            <LoginForm />
        </AuthLayout>
    )
}

export default LoginPage
