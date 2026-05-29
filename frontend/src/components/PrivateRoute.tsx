import { Navigate } from 'react-router-dom'
import { useAuth } from '@/context/AuthContext'

type PrivateRouteProps = {
    children: React.ReactNode
    adminOnly?: boolean
}

export function PrivateRoute({ children, adminOnly = false }: PrivateRouteProps) {
    const { isAuthenticated, isAdmin } = useAuth()
    if (!isAuthenticated) {
        return <Navigate to="/login" replace />
    }
    // rota de admin acessada por cliente cai pra home dele
    if (adminOnly && !isAdmin) {
        return <Navigate to="/dashboard" replace />
    }
    return <>{children}</>
}
