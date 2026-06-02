type InputProps = React.InputHTMLAttributes<HTMLInputElement> & {
    label: string
    name: string
    error?: string
    hint?: string
}

export function Input({ label, name, error, hint, className = '', ...props }: InputProps) {
    // borda vermelha quando há erro, deixando o campo problemático evidente
    const borda = error
        ? 'border-error focus:border-error focus:ring-error/20'
        : 'border-secondary focus:border-primary focus:ring-primary/20'

    return (
        <div className="flex flex-col gap-1.5">
            <label className="text-[14px] font-medium leading-[20px] text-secondary" htmlFor={name}>
                {label}
            </label>
            <input
                id={name}
                className={`h-12 rounded-[5px] border bg-white px-3.5 text-[14px] leading-[20px] text-ink placeholder:text-secondary/60 transition-colors focus:outline-none focus:ring-2 ${borda} ${className}`.trim()}
                name={name}
                aria-invalid={!!error}
                {...props}
            />
            {/* dica fica visível enquanto não há erro; some quando o erro aparece */}
            {!error && hint && <p className="text-xs text-secondary">{hint}</p>}
            {error && <p className="text-xs text-error">{error}</p>}
        </div>
    )
}
