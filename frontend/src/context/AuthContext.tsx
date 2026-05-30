import { createContext, useContext, useState, type ReactNode } from 'react'
import * as authService from '@/services/auth'

export type Perfil = 'CLIENTE' | 'ADMINISTRADOR'

interface Sessao {
    perfil: Perfil
    email: string
}

interface AuthContextType {
    isAuthenticated: boolean
    perfil: Perfil | null
    email: string | null
    isAdmin: boolean
    login: (email: string, senha: string) => Promise<Perfil | null>
    logout: () => Promise<void>
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

const ACCESS_KEY = 'accessToken'
const REFRESH_KEY = 'refreshToken'

// Lê os claims 'perfil' e 'email' do payload do JWT sem validar a assinatura — a
// validação real é do backend; aqui é só pra decidir o que mostrar na UI.
function lerSessao(token: string | null): Sessao | null {
    if (!token) return null
    try {
        const payload = JSON.parse(atob(token.split('.')[1]))
        const perfil: Perfil = payload.perfil === 'ADMINISTRADOR' ? 'ADMINISTRADOR' : 'CLIENTE'
        return { perfil, email: payload.email ?? '' }
    } catch {
        return null
    }
}

export function AuthProvider({ children }: { children: ReactNode }) {
    const [sessao, setSessao] = useState<Sessao | null>(() => lerSessao(localStorage.getItem(ACCESS_KEY)))

    const login = async (email: string, senha: string) => {
        const { accessToken, refreshToken } = await authService.login(email, senha)
        localStorage.setItem(ACCESS_KEY, accessToken)
        localStorage.setItem(REFRESH_KEY, refreshToken)
        const nova = lerSessao(accessToken)
        setSessao(nova)
        return nova?.perfil ?? null
    }

    const logout = async () => {
        const refreshToken = localStorage.getItem(REFRESH_KEY)
        try {
            if (refreshToken) await authService.logout(refreshToken)
        } finally {
            localStorage.removeItem(ACCESS_KEY)
            localStorage.removeItem(REFRESH_KEY)
            setSessao(null)
        }
    }

    return (
        <AuthContext.Provider value={{
            isAuthenticated: sessao !== null,
            perfil: sessao?.perfil ?? null,
            email: sessao?.email ?? null,
            isAdmin: sessao?.perfil === 'ADMINISTRADOR',
            login,
            logout,
        }}>
            {children}
        </AuthContext.Provider>
    )
}

export function useAuth() {
    const ctx = useContext(AuthContext)
    if (!ctx) throw new Error('useAuth precisa estar dentro de <AuthProvider>')
    return ctx
}
