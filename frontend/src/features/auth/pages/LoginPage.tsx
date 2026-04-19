import LoginForm from '../components/LoginForm'

function LoginPage() {
  return (
    <div className="min-h-screen bg-background flex flex-col">
        <header className="p-4">
            <h1 className="text-2xl font-bold">Login</h1>
        </header>

        <main className="flex-1 flex items-center justify-center">
            <div className="bg-white p-8 rounded-lg shadow-md w-full max-w-md">
            <LoginForm />
            </div>
        </main>

        <footer className="p-4">
            <p className="text-sm text-gray-500">Copyright 2026 Importa Aí</p>
        </footer>
    </div>
  )
}

export default LoginPage