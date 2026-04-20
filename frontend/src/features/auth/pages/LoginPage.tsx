import LoginForm from '../components/LoginForm'
import Header from '../../../components/Header'
import Footer from '../../../components/Footer'

function LoginPage() {
  return (
    <div className="min-h-dvh bg-background flex flex-col">
        <Header />

        <main className="flex-1 flex items-center justify-center">
            <div className="bg-white p-8 rounded-lg shadow-md w-full max-w-md">
            <LoginForm />
            </div>
        </main>

        <Footer />
    </div>
  )
}

export default LoginPage