import { createContext, useContext, useEffect, type ReactNode } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useAuth } from '@/context/AuthContext'
import { useToast } from '@/context/ToastContext'
import { criarClienteStomp } from '@/services/stomp'
import { listarNotificacoes, marcarTodasComoLidas } from '@/services/notificacoes'
import type { Notificacao } from '@/types/notificacao'

const QUERY_KEY = ['notificacoes'] as const

interface NotificacaoContextType {
    notificacoes: Notificacao[]
    naoLidas: number
    carregando: boolean
    erro: boolean
    marcarTodasComoLidas: () => void
}

const NotificacaoContext = createContext<NotificacaoContextType | undefined>(undefined)

export function NotificacaoProvider({ children }: { children: ReactNode }) {
    const { isAuthenticated } = useAuth()
    const { showToast } = useToast()
    const queryClient = useQueryClient()

    // Histórico persistido é a fonte de verdade; só busca com sessão ativa.
    const { data: notificacoes = [], isLoading, isError } = useQuery({
        queryKey: QUERY_KEY,
        queryFn: listarNotificacoes,
        enabled: isAuthenticated,
    })

    // O WebSocket empurra as notificações novas pro MESMO cache do React Query,
    // mantendo lista e badge do sino em sincronia (uma única fonte de verdade).
    useEffect(() => {
        if (!isAuthenticated) {
            queryClient.removeQueries({ queryKey: QUERY_KEY })
            return
        }

        const client = criarClienteStomp((nova) => {
            queryClient.setQueryData<Notificacao[]>(QUERY_KEY, (anteriores = []) => {
                // idempotência no front: ignora reentrega do mesmo id (espelha RN04)
                if (anteriores.some((n) => n.id === nova.id)) return anteriores
                return [nova, ...anteriores]
            })
            showToast(nova.mensagem)
        })
        client.activate()

        return () => {
            void client.deactivate()
        }
    }, [isAuthenticated, showToast, queryClient])

    // Marca todas como lidas de forma otimista — a UI reage na hora; rollback se falhar.
    const { mutate: marcarLidas } = useMutation({
        mutationFn: marcarTodasComoLidas,
        onMutate: () => {
            const anteriores = queryClient.getQueryData<Notificacao[]>(QUERY_KEY)
            queryClient.setQueryData<Notificacao[]>(QUERY_KEY, (atual = []) =>
                atual.map((n) => (n.lida ? n : { ...n, lida: true })),
            )
            return { anteriores }
        },
        onError: (_erro, _vars, contexto) => {
            if (contexto?.anteriores) queryClient.setQueryData(QUERY_KEY, contexto.anteriores)
        },
    })

    const naoLidas = notificacoes.filter((n) => !n.lida).length

    return (
        <NotificacaoContext.Provider
            value={{
                notificacoes,
                naoLidas,
                carregando: isLoading,
                erro: isError,
                marcarTodasComoLidas: marcarLidas,
            }}
        >
            {children}
        </NotificacaoContext.Provider>
    )
}

export function useNotificacoes() {
    const ctx = useContext(NotificacaoContext)
    if (!ctx) throw new Error('useNotificacoes precisa estar dentro de <NotificacaoProvider>')
    return ctx
}
