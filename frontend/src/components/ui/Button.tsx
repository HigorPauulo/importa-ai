type ButtonProps = {
    children: React.ReactNode
    variant?: 'primary' | 'outline'
    fullWidth?: boolean
    type?: 'button' | 'submit' | 'reset'
    onClick?: () => void
}

export function Button({ children, variant = 'primary', fullWidth = false, type = 'button', onClick }: ButtonProps) {
    return (
        <button
            className={`px-4 py-3 rounded-[5px] font-bold cursor-pointer ${
                variant === 'primary'
                    ? 'bg-primary text-white'
                    : 'bg-white text-primary border border-primary'
            } ${fullWidth ? 'w-full' : ''}`}
            type={type}
            onClick={onClick}
        >
            {children}
        </button>
   
    )
}