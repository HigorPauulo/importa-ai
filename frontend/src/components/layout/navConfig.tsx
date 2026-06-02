import type { ReactNode } from 'react'
import { IconDashboard, IconBox, IconPlus, IconDollar, IconBell, IconUsers, IconDownload } from './icons'

export interface NavItem {
    to: string
    label: string
    icon: ReactNode
    end?: boolean
}

const ic = 'w-[18px] h-[18px]'

export const navCliente: NavItem[] = [
    { to: '/dashboard', label: 'Dashboard', icon: <IconDashboard className={ic} /> },
    { to: '/pedidos', label: 'Encomendas', icon: <IconBox className={ic} /> },
    { to: '/cadastrar-encomendas', label: 'Nova encomenda', icon: <IconPlus className={ic} /> },
    { to: '/cotacao', label: 'Cotação', icon: <IconDollar className={ic} /> },
    { to: '/notificacoes', label: 'Notificações', icon: <IconBell className={ic} /> },
]

export const navAdmin: NavItem[] = [
    { to: '/admin', label: 'Painel', icon: <IconDashboard className={ic} />, end: true },
    { to: '/admin/pedidos', label: 'Pedidos', icon: <IconBox className={ic} /> },
    { to: '/admin/usuarios', label: 'Usuários', icon: <IconUsers className={ic} /> },
    { to: '/admin/cotacao', label: 'Cotação', icon: <IconDollar className={ic} /> },
    { to: '/admin/exportar', label: 'Exportar', icon: <IconDownload className={ic} /> },
]

export const bottomNavCliente: NavItem[] = navCliente.filter((i) => i.to !== '/cadastrar-encomendas')
export const bottomNavAdmin: NavItem[] = navAdmin
