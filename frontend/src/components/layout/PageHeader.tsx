import type { ReactNode } from 'react'
import { HeaderActions } from './HeaderActions'

export { BellButton } from './BellButton'

interface PageHeaderProps {
    titulo: string
    subtitulo?: string
    acao?: ReactNode
}

export function PageHeader({ titulo, subtitulo, acao }: PageHeaderProps) {
    return (
        <header className="mb-6 flex items-start justify-between gap-4 lg:mb-8">
            <div className="min-w-0">
                <h1 className="text-[28px] font-bold leading-[36px] text-ink">{titulo}</h1>
                {subtitulo && <p className="mt-0.5 text-[14px] leading-[20px] text-secondary">{subtitulo}</p>}
            </div>

            <div className="flex shrink-0 items-center gap-3">
                {acao && <div className="hidden lg:flex lg:items-center lg:gap-3">{acao}</div>}
                <div className="lg:hidden">
                    <HeaderActions />
                </div>
            </div>
        </header>
    )
}
