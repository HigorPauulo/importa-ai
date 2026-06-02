import { Outlet } from 'react-router-dom'
import { Sidebar } from './Sidebar'
import { BottomNav } from './BottomNav'
import type { NavItem } from './navConfig'

interface AppLayoutProps {
    nav: NavItem[]
    bottomNav: NavItem[]
}

export function AppLayout({ nav, bottomNav }: AppLayoutProps) {
    return (
        <div className="min-h-dvh bg-background lg:flex">
            <Sidebar nav={nav} />

            <main className="min-w-0 flex-1 px-5 pb-24 pt-6 lg:px-10 lg:py-8 lg:pb-10">
                <Outlet />
            </main>

            <BottomNav items={bottomNav} />
        </div>
    )
}
