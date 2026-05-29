import { createContext, useCallback, useContext, useRef, useState, type ReactNode } from 'react'

interface Toast {
    id: number
    mensagem: string
}

interface ToastContextType {
    showToast: (mensagem: string) => void
}

const ToastContext = createContext<ToastContextType | undefined>(undefined)

const DURACAO_MS = 5000

export function ToastProvider({ children }: { children: ReactNode }) {
    const [toasts, setToasts] = useState<Toast[]>([])
    // contador estável pra gerar ids únicos sem re-render
    const proximoId = useRef(0)

    const removerToast = useCallback((id: number) => {
        setToasts((atuais) => atuais.filter((t) => t.id !== id))
    }, [])

    const showToast = useCallback((mensagem: string) => {
        const id = proximoId.current++
        setToasts((atuais) => [...atuais, { id, mensagem }])
        // auto-fecha depois de alguns segundos
        setTimeout(() => removerToast(id), DURACAO_MS)
    }, [removerToast])

    return (
        <ToastContext.Provider value={{ showToast }}>
            {children}

            <div className="fixed top-5 right-5 z-50 flex flex-col gap-3 w-80 max-w-[calc(100vw-2.5rem)]">
                {toasts.map((toast) => (
                    <div
                        key={toast.id}
                        role="alert"
                        className="bg-white border-l-4 border-primary shadow-lg rounded-[5px] p-4 flex items-start gap-3"
                        style={{ animation: 'toast-in 0.2s ease-out' }}
                    >
                        <div className="flex-1">
                            <p className="text-sm font-bold mb-0.5">Nova atualização</p>
                            <p className="text-sm text-secondary">{toast.mensagem}</p>
                        </div>
                        <button
                            type="button"
                            onClick={() => removerToast(toast.id)}
                            className="text-secondary hover:text-primary text-lg leading-none"
                            aria-label="Fechar"
                        >
                            ×
                        </button>
                    </div>
                ))}
            </div>
        </ToastContext.Provider>
    )
}

export function useToast() {
    const ctx = useContext(ToastContext)
    if (!ctx) throw new Error('useToast precisa estar dentro de <ToastProvider>')
    return ctx
}
