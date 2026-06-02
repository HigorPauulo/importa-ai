import type { ReactNode } from 'react'
import { Link } from 'react-router-dom'
import logo from '@/assets/logo.png'

interface LegalLayoutProps {
    titulo: string
    atualizadoEm: string
    children: ReactNode
}

export function LegalLayout({ titulo, atualizadoEm, children }: LegalLayoutProps) {
    return (
        <div className="min-h-dvh bg-background">
            <header className="border-b border-black/5 bg-white">
                <div className="mx-auto flex max-w-3xl items-center justify-between px-6 py-4">
                    <Link to="/login">
                        <img src={logo} alt="Importa Aí" className="h-9 w-auto" />
                    </Link>
                    <Link to="/register" className="text-sm font-semibold text-primary hover:underline">
                        Voltar ao cadastro
                    </Link>
                </div>
            </header>

            <main className="mx-auto max-w-3xl px-6 py-10">
                <h1 className="text-[28px] font-bold leading-[36px] text-ink">{titulo}</h1>
                <p className="mt-1 text-[13px] text-secondary">Última atualização: {atualizadoEm}</p>
                <div className="mt-8 space-y-8 text-[15px] leading-[24px] text-secondary">{children}</div>
            </main>
        </div>
    )
}

interface LegalSectionProps {
    titulo: string
    children: ReactNode
}

export function LegalSection({ titulo, children }: LegalSectionProps) {
    return (
        <section>
            <h2 className="text-[18px] font-semibold leading-[26px] text-ink">{titulo}</h2>
            <div className="mt-2 space-y-3">{children}</div>
        </section>
    )
}
