import type { HTMLAttributes } from 'react'

export function Card({ className = '', children, ...props }: HTMLAttributes<HTMLDivElement>) {
    return (
        <div className={`rounded-[10px] bg-white shadow-[0px_1px_2px_rgba(0,0,0,0.08)] ${className}`.trim()} {...props}>
            {children}
        </div>
    )
}
