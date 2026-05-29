import { createContext, useContext, useState, type ReactNode } from 'react'
import * as authService from '@/services/auth'

export type Perfil = 'CLIENTE' | 'ADMINISTRADOR'

interface AuthContextType {
    isAuthenticated: boolean
    perfil: Perfil | null
    isAdmin: boolean
    login: (email: string, senha: string) => Promise<Perfil | null>
    logout: () => Promise<void>
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

const ACCESS_KEY = 'accessToken'
const REFRESH_KEY = 'refreshToken'

// Lê o claim 'perfil' do payload do JWT sem validar a assinatura — a validação
// real é responsabilidade do backend; aqui é só pra decidir o que mostrar na UI.
function lerPerfil(token: string | null): Perfil | null {
    if (!token) return null
    try {
        const payload = JSON.parse(atob(token.split('.')[1]))
        return payload.perfil === 'ADMINISTRADOR' ? 'ADMINISTRADOR' : 'CLIENTE'
    } catch {
        return null
    }
}

export function AuthProvider({ children }: { children: ReactNode }) {
    const [perfil, setPerfil] = useState<Perfil | null>(() => lerPerfil(localStorage.getItem(ACCESS_KEY)))

    const isAuthenticated = perfil !== null

    const login = async (email: string, senha: string) => {
        const { accessToken, refreshToken } = await authService.login(email, senha)
        localStorage.setItem(ACCESS_KEY, accessToken)
        localStorage.setItem(REFRESH_KEY, refreshToken)
        const novoPerfil = lerPerfil(accessToken)
        setPerfil(novoPerfil)
        return novoPerfil
    }

    const logout = async () => {
        const refreshToken = localStorage.getItem(REFRESH_KEY)
        try {
            if (refreshToken) await authService.logout(refreshToken)
        } finally {
            localStorage.removeItem(ACCESS_KEY)
            localStorage.removeItem(REFRESH_KEY)
            setPerfil(null)
        }
    }

    return (
        <AuthContext.Provider value={{ isAuthenticated, perfil, isAdmin: perfil === 'ADMINISTRADOR', login, logout }}>
            {children}
        </AuthContext.Provider>
    )
}

export function useAuth() {
    const ctx = useContext(AuthContext)
    if (!ctx) throw new Error('useAuth precisa estar dentro de <AuthProvider>')
    return ctx
}
