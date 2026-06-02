import { Link } from 'react-router-dom'
import { useNotificacoes } from '@/context/NotificacaoContext'
import { IconBell } from './icons'

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
