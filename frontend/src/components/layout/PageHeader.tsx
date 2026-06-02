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
        <header className="-mx-5 -mt-6 mb-6 flex items-start justify-between gap-4 bg-white px-5 pb-4 pt-6 shadow-[0px_1px_2px_rgba(0,0,0,0.08)] lg:mx-0 lg:mt-0 lg:mb-8 lg:bg-transparent lg:px-0 lg:pb-0 lg:pt-0 lg:shadow-none">
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
