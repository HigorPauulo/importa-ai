type InputProps = React.InputHTMLAttributes<HTMLInputElement> & {
    label: string
    name: string
    error?: string
    hint?: string
}

export function Input({ label, name, error, hint, ...props }: InputProps) {
    // borda vermelha quando há erro, deixando o campo problemático evidente
    const borda = error ? 'border-error focus:border-error' : 'border-gray-300 focus:border-primary'
    return (
        <div className="flex flex-col gap-1 mb-4">
            <label className="text-sm" htmlFor={name}>{label}</label>
            <input
                id={name}
                className={`border ${borda} rounded-md p-2 focus:outline-none shadow-md`}
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