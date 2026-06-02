import type { ReactNode } from 'react'
import { Link } from 'react-router-dom'
import { useNotificacoes } from '@/context/NotificacaoContext'
import { IconBell } from './icons'

interface PageHeaderProps {
    titulo: string
    subtitulo?: string
    acao?: ReactNode
}

export function PageHeader({ titulo, subtitulo, acao }: PageHeaderProps) {
    return (
        <header className="mb-6 flex items-start justify-between gap-4 lg:mb-8">
            <div>
                <h1 className="text-[28px] font-bold leading-[36px] text-ink">{titulo}</h1>
                {subtitulo && <p className="mt-0.5 text-[14px] leading-[20px] text-secondary">{subtitulo}</p>}
            </div>
            {acao && <div className="shrink-0">{acao}</div>}
        </header>
    )
}

export function BellButton({ estatico = false }: { estatico?: boolean }) {
    const { naoLidas } = useNotificacoes()

    const conteudo = (
        <>
            <IconBell className="h-5 w-5 text-ink" />
            {naoLidas > 0 && (
                <span className="absolute -right-1.5 -top-1.5 flex h-5 min-w-[20px] items-center justify-center rounded-full bg-error px-1 text-xs font-bold text-white">
                    {naoLidas > 99 ? '99+' : naoLidas}
                </span>
            )}
        </>
    )

    const classe =
        'relative flex h-12 w-12 items-center justify-center rounded-[10px] bg-white shadow-[0px_1px_2px_rgba(0,0,0,0.08)] transition-colors hover:bg-gray-50'
    const rotulo = `Notificações${naoLidas > 0 ? `, ${naoLidas} não lidas` : ''}`

    if (estatico) {
        return (
            <span className={classe} aria-label={rotulo} role="status">
                {conteudo}
            </span>
        )
    }

    return (
        <Link to="/notificacoes" aria-label={rotulo} className={classe}>
            {conteudo}
        </Link>
    )
}
