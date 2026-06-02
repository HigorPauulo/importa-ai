import type { ReactNode } from 'react'

type Tom = 'default' | 'primary' | 'warning' | 'success' | 'error'

const corDoValor: Record<Tom, string> = {
    default: 'text-primary-dark',
    primary: 'text-primary',
    warning: 'text-warning',
    success: 'text-success',
    error: 'text-error',
}

interface KpiCardProps {
    titulo: string
    valor: ReactNode
    tom?: Tom
}

export function KpiCard({ titulo, valor, tom = 'default' }: KpiCardProps) {
    return (
        <div className="rounded-[10px] bg-white p-5 shadow-[0px_1px_2px_rgba(0,0,0,0.08)]">
            <p className="text-[13px] font-semibold leading-[18px] text-secondary">{titulo}</p>
            <p className={`mt-2 text-[28px] font-bold leading-[36px] ${corDoValor[tom]}`}>{valor}</p>
        </div>
    )
}
