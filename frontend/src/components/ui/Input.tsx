import { useState } from 'react'
import { IconEye, IconEyeOff } from '@/components/layout/icons'

type InputProps = React.InputHTMLAttributes<HTMLInputElement> & {
    label: string
    name: string
    error?: string
    hint?: string
}

export function Input({ label, name, error, hint, className = '', type, ...props }: InputProps) {
    const [mostrarSenha, setMostrarSenha] = useState(false)
    const ehSenha = type === 'password'
    const tipoEfetivo = ehSenha && mostrarSenha ? 'text' : type

    // borda vermelha quando há erro, deixando o campo problemático evidente
    const borda = error
        ? 'border-error focus:border-error focus:ring-error/20'
        : 'border-secondary focus:border-primary focus:ring-primary/20'

    return (
        <div className="flex flex-col gap-1.5">
            <label className="text-[14px] font-medium leading-[20px] text-secondary" htmlFor={name}>
                {label}
            </label>
            <div className="relative">
                <input
                    id={name}
                    type={tipoEfetivo}
                    className={`h-12 w-full rounded-[5px] border bg-white px-3.5 ${ehSenha ? 'pr-11' : ''} text-[14px] leading-[20px] text-ink placeholder:text-secondary/60 transition-colors focus:outline-none focus:ring-2 ${borda} ${className}`.trim()}
                    name={name}
                    aria-invalid={!!error}
                    {...props}
                />
                {ehSenha && (
                    <button
                        type="button"
                        onClick={() => setMostrarSenha((v) => !v)}
                        className="absolute right-3 top-1/2 -translate-y-1/2 text-secondary hover:text-ink"
                        aria-label={mostrarSenha ? 'Ocultar senha' : 'Mostrar senha'}
                    >
                        {mostrarSenha ? <IconEyeOff className="h-5 w-5" /> : <IconEye className="h-5 w-5" />}
                    </button>
                )}
            </div>
            {/* dica fica visível enquanto não há erro; some quando o erro aparece */}
            {!error && hint && <p className="text-xs text-secondary">{hint}</p>}
            {error && <p className="text-xs text-error">{error}</p>}
        </div>
    )
}
