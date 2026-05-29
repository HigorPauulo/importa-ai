import { createContext, useContext, useEffect, useState, type ReactNode } from 'react'
import { useAuth } from '@/context/AuthContext'
import { useToast } from '@/context/ToastContext'
import { criarClienteStomp } from '@/services/stomp'
import type { Notificacao } from '@/types/notificacao'

interface NotificacaoContextType {
    notificacoes: Notificacao[]
    naoLidas: number
}

const NotificacaoContext = createContext<NotificacaoContextType | undefined>(undefined)

export function NotificacaoProvider({ children }: { children: ReactNode }) {
    const { isAuthenticated } = useAuth()
    const { showToast } = useToast()
    const [notificacoes, setNotificacoes] = useState<Notificacao[]>([])

    useEffect(() => {
        // só conecta o WebSocket quando há usuário logado
        if (!isAuthenticated) {
            setNotificacoes([])
            return
        }

        const client = criarClienteStomp((nova) => {
            // mais recente no topo
            setNotificacoes((anteriores) => [nova, ...anteriores])
            showToast(nova.mensagem)
        })
        client.activate()

        // ao deslogar/desmontar, encerra a conexão STOMP
        return () => {
            void client.deactivate()
        }
    }, [isAuthenticated, showToast])

    const naoLidas = notificacoes.filter((n) => !n.lida).length

    return (
        <NotificacaoContext.Provider value={{ notificacoes, naoLidas }}>
            {children}
        </NotificacaoContext.Provider>
    )
}

export function useNotificacoes() {
    const ctx = useContext(NotificacaoContext)
    if (!ctx) throw new Error('useNotificacoes precisa estar dentro de <NotificacaoProvider>')
    return ctx
}
