import { useState } from 'react'
import { Outlet } from 'react-router-dom'
import { Sidebar } from './Sidebar'
import { IconMenu } from './icons'
import type { NavItem } from './navConfig'
import logo from '@/assets/logo.png'

interface AppLayoutProps {
    nav: NavItem[]
}

export function AppLayout({ nav }: AppLayoutProps) {
    const [menuAberto, setMenuAberto] = useState(false)

    return (
        <div className="min-h-dvh bg-background lg:flex">
            <Sidebar nav={nav} aberto={menuAberto} onFechar={() => setMenuAberto(false)} />

            <div className="flex h-16 items-center justify-between border-b border-gray-200 bg-white px-4 lg:hidden">
                <img src={logo} alt="Importa Aí" className="h-10" />
                <button
                    type="button"
                    onClick={() => setMenuAberto(true)}
                    className="text-ink"
                    aria-label="Abrir menu"
                >
                    <IconMenu className="h-6 w-6" />
                </button>
            </div>

            <main className="min-w-0 flex-1 px-5 py-6 lg:px-10 lg:py-8 lg:pb-10">
                <Outlet />
            </main>
        </div>
    )
}
