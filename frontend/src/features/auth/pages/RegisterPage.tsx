import RegisterForm from '@/features/auth/components/RegisterForm'
import Header from '@/components/Header'
import Footer from '@/components/Footer'

function RegisterPage() {
    return (
        <div className="min-h-dvh bg-background flex flex-col px-5">
            <Header />

            <main className="flex-1 flex items-center justify-center">
                <div className="bg-white p-8 rounded-lg shadow-md w-full max-w-md">
                    <RegisterForm />
                </div>
            </main>
            
            <Footer />
        </div>
    )
}

export default RegisterPage