type SelectProps = React.SelectHTMLAttributes<HTMLSelectElement> & {
    label?: string
    name: string
    error?: string
    hint?: string
}

export function Select({ label, name, error, hint, className = '', children, ...props }: SelectProps) {
    const borda = error
        ? 'border-error focus:border-error focus:ring-error/20'
        : 'border-secondary focus:border-primary focus:ring-primary/20'

    return (
        <div className="flex flex-col gap-1.5">
            {label && (
                <label className="text-[14px] font-medium leading-[20px] text-secondary" htmlFor={name}>
                    {label}
                </label>
            )}
            <select
                id={name}
                name={name}
                aria-invalid={!!error}
                className={`h-12 rounded-[5px] border bg-white px-3.5 text-[14px] leading-[20px] text-ink transition-colors focus:outline-none focus:ring-2 ${borda} ${className}`.trim()}
                {...props}
            >
                {children}
            </select>
            {!error && hint && <p className="text-xs text-secondary">{hint}</p>}
            {error && <p className="text-xs text-error">{error}</p>}
        </div>
    )
}
