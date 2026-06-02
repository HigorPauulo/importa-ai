import React from 'react'

type ButtonProps = {
    children: React.ReactNode
    variant?: 'primary' | 'outline' | 'ghost'
    size?: 'md' | 'sm'
    fullWidth?: boolean
    type?: 'button' | 'submit' | 'reset'
    onClick?: React.MouseEventHandler<HTMLButtonElement>
    disabled?: boolean
    loading?: boolean
    className?: string
}

export function Button({
    children,
    variant = 'primary',
    size = 'md',
    fullWidth = false,
    type = 'button',
    onClick,
    disabled = false,
    loading = false,
    className = '',
}: ButtonProps) {
    const base =
        'inline-flex items-center justify-center gap-2 rounded-[5px] font-semibold transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-primary/40'
    const sizes = size === 'sm' ? 'h-10 px-4 text-sm' : 'h-12 px-5 text-sm'
    const variants = {
        primary: 'bg-primary text-white hover:bg-primary-dark',
        outline: 'bg-white text-primary border border-primary hover:bg-primary-light/40',
        ghost: 'bg-transparent text-primary hover:bg-primary-light/40',
    }[variant]
    const widthClass = fullWidth ? 'w-full' : ''
    const stateClass = disabled || loading ? 'opacity-50 cursor-not-allowed' : ''
    const spinnerBorder = variant === 'primary' ? 'border-white' : 'border-primary'

    return (
        <button
            className={`${base} ${sizes} ${variants} ${widthClass} ${stateClass} ${className}`.trim()}
            type={type}
            onClick={onClick}
            disabled={disabled || loading}
            aria-disabled={disabled || loading}
        >
            {loading && (
                <span
                    className={`mr-1 h-4 w-4 animate-spin rounded-full border-b-2 ${spinnerBorder}`}
                    aria-hidden="true"
                />
            )}
            {children}
        </button>
    )
}
