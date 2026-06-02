import type { ReactNode } from 'react'
import { IconClose } from '@/components/layout/icons'

interface ModalProps {
    aberto: boolean
    onFechar: () => void
    titulo: string
    children: ReactNode
}

export function Modal({ aberto, onFechar, titulo, children }: ModalProps) {
    if (!aberto) return null

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
            <div className="absolute inset-0 bg-black/40" onClick={onFechar} aria-hidden="true" />
            <div className="relative z-10 w-full max-w-md rounded-2xl bg-white p-6 shadow-xl">
                <div className="mb-5 flex items-center justify-between">
                    <h2 className="text-xl font-bold text-ink">{titulo}</h2>
                    <button type="button" onClick={onFechar} className="text-secondary hover:text-ink" aria-label="Fechar">
                        <IconClose className="h-5 w-5" />
                    </button>
                </div>
                {children}
            </div>
        </div>
    )
}
