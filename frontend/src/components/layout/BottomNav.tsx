import { cloneElement, isValidElement, type ReactElement } from 'react'
import { NavLink } from 'react-router-dom'
import type { NavItem } from './navConfig'

export function BottomNav({ items }: { items: NavItem[] }) {
    return (
        <nav className="fixed inset-x-0 bottom-0 z-30 flex h-[68px] items-stretch border-t border-gray-200 bg-white lg:hidden">
            {items.map((item) => (
                <NavLink
                    key={item.to}
                    to={item.to}
                    end={item.end}
                    className={({ isActive }) =>
                        `flex flex-1 flex-col items-center justify-center gap-1 text-[11px] font-medium leading-[14px] transition-colors ${
                            isActive ? 'text-primary' : 'text-secondary'
                        }`
                    }
                >
                    {isValidElement(item.icon)
                        ? cloneElement(item.icon as ReactElement<{ className?: string }>, { className: 'h-[22px] w-[22px]' })
                        : item.icon}
                    {item.label}
                </NavLink>
            ))}
        </nav>
    )
}
