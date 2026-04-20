import React from 'react'

type ButtonProps = {
    children: React.ReactNode
    variant?: 'primary' | 'outline'
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
    fullWidth = false,
    type = 'button',
    onClick,
    disabled = false,
    loading = false,
    className = ''
}: ButtonProps) {
    const baseClasses = 'px-4 py-3 rounded-[5px] font-bold flex items-center justify-center gap-2 transition-all focus:outline-none';
    const variantClasses =
        variant === 'primary'
            ? 'bg-primary text-white'
            : 'bg-white text-primary border border-primary';
    const widthClass = fullWidth ? 'w-full' : '';
    const stateClass = (disabled || loading) ? 'opacity-50 cursor-not-allowed' : '';
    
    const spinnerBorder = variant === 'primary' ? 'border-white' : 'border-primary';

    return (
        <button
            className={`${baseClasses} ${variantClasses} ${widthClass} ${stateClass} ${className}`.trim()}
            type={type}
            onClick={onClick}
            disabled={disabled || loading}
            aria-disabled={disabled || loading}
        >
            {loading && (
                <span
                    className={`animate-spin rounded-full h-5 w-5 border-b-2 ${spinnerBorder} mr-2`}
                    aria-hidden="true"
                />
            )}
            {children}
        </button>
    )
}