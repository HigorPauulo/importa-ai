import { useState } from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { useAuth } from '@/context/AuthContext'
import { nomeExibicao, iniciais } from '@/lib/usuario'
import { buscarMeuPerfil } from '@/services/perfil'
import { EditarPerfilModal } from '@/features/perfil/components/EditarPerfilModal'
import type { NavItem } from './navConfig'
import logo from '@/assets/logo.png'

interface SidebarProps {
    nav: NavItem[]
}

export function Sidebar({ nav }: SidebarProps) {
    const { email, perfil, logout } = useAuth()
    const navigate = useNavigate()
    const { data: meuPerfil } = useQuery({ queryKey: ['me'], queryFn: buscarMeuPerfil })
    const [editando, setEditando] = useState(false)

    const nome = meuPerfil?.nome ?? nomeExibicao(email, perfil)

    const sair = async () => {
        await logout()
        navigate('/login')
    }

    return (
        <>
            <aside className="hidden w-[260px] flex-col bg-white lg:flex">
                <div className="flex items-center justify-center px-6 pb-3 pt-0">
                    <img src={logo} alt="Importa Aí" className="w-[150px]" />
                </div>

                <nav className="flex-1 space-y-1 px-4 py-2">
                    {nav.map((item) => (
                        <NavLink
                            key={item.to}
                            to={item.to}
                            end={item.end}
                            className={({ isActive }) =>
                                `flex items-center gap-3 rounded-[8px] px-3.5 py-3 text-[14px] font-medium leading-[20px] transition-colors ${
                                    isActive ? 'bg-primary text-white' : 'text-muted hover:bg-gray-100'
                                }`
                            }
                        >
                            {({ isActive }) => (
                                <>
                                    <span className={isActive ? 'text-white' : 'text-secondary'}>{item.icon}</span>
                                    {item.label}
                                </>
                            )}
                        </NavLink>
                    ))}
                </nav>

                <div className="p-4 pt-3">
                    <div className="flex items-center gap-2.5 px-2">
                        <button
                            type="button"
                            onClick={() => setEditando(true)}
                            className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-primary-light text-sm font-bold text-primary-dark"
                            aria-label="Editar perfil"
                        >
                            {iniciais(nome)}
                        </button>
                        <div className="min-w-0 leading-tight">
                            <p className="truncate text-[13px] font-semibold leading-[18px] text-ink">{nome}</p>
                            <div className="mt-0.5 flex flex-col items-start gap-0.5">
                                <button type="button" onClick={() => setEditando(true)} className="text-[11px] font-semibold leading-[14px] text-primary hover:underline">
                                    Editar perfil
                                </button>
                                <button type="button" onClick={sair} className="text-[11px] font-bold leading-[14px] text-error hover:underline">
                                    Sair da conta
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </aside>

            {meuPerfil && (
                <EditarPerfilModal aberto={editando} onFechar={() => setEditando(false)} perfil={meuPerfil} />
            )}
        </>
    )
}
