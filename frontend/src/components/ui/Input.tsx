type InputProps = React.InputHTMLAttributes<HTMLInputElement> & {
    label: string
    name: string
    error?: string
}

export function Input({ label, name, error, ...props }: InputProps) {
    return (
        <div className="flex flex-col gap-1 mb-4">
            <label className="text-sm font-bold text-gray-700" htmlFor={name}>{label}</label>   
            <input
                className="border border-gray-300 rounded-md p-2 focus:outline-none focus:border-primary"
                name={name}
                {...props}
            />
            {error && <p className="text-xs text-red-700">{error}</p>}
        </div>
    )
}