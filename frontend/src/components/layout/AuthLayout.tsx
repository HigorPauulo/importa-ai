import type { ReactNode } from 'react'
import logo from '@/assets/logo.png'

interface AuthLayoutProps {
    titulo: string
    subtitulo: string
    children: ReactNode
}

export function AuthLayout({ titulo, subtitulo, children }: AuthLayoutProps) {
    return (
        <div className="min-h-dvh lg:flex">
            <div className="relative hidden flex-col items-center justify-center bg-primary px-16 text-center text-white lg:flex lg:w-[43%]">
                <img src={logo} alt="Importa Aí" className="absolute left-1/2 top-0 w-72 -translate-x-1/2" />
                <h1 className="mb-4 text-[34px] font-bold leading-[42px]">{titulo}</h1>
                <p className="max-w-xs text-base text-white/85">{subtitulo}</p>
            </div>

            <div className="flex flex-1 items-center justify-center bg-background p-6">
                <div className="w-full max-w-[480px]">
                    <div className="mb-6 text-center lg:hidden">
                        <img src={logo} alt="Importa Aí" className="mx-auto w-40" />
                        <p className="mt-2 px-4 text-[14px] leading-[20px] text-secondary">{subtitulo}</p>
                    </div>
                    <div className="rounded-[16px] bg-white p-6 shadow-[0px_4px_4px_rgba(0,0,0,0.1)] sm:p-10">{children}</div>
                </div>
            </div>
        </div>
    )
}
