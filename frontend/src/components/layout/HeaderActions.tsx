import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { BellButton } from './PageHeader'
import { useAuth } from '@/context/AuthContext'
import { nomeExibicao, iniciais } from '@/lib/usuario'
import { buscarMeuPerfil } from '@/services/perfil'
import { EditarPerfilModal } from '@/features/perfil/components/EditarPerfilModal'

export function HeaderActions({ estatico = false }: { estatico?: boolean }) {
    const { email, perfil } = useAuth()
    const { data: meuPerfil } = useQuery({ queryKey: ['me'], queryFn: buscarMeuPerfil })
    const [editando, setEditando] = useState(false)

    const nome = meuPerfil?.nome ?? nomeExibicao(email, perfil)

    return (
        <div className="flex items-center gap-3">
            <BellButton estatico={estatico} />
            <button
                type="button"
                onClick={() => setEditando(true)}
                className="flex h-11 w-11 items-center justify-center rounded-full bg-primary-light text-sm font-bold text-primary-dark lg:hidden"
                aria-label="Perfil e conta"
            >
                {iniciais(nome)}
            </button>
            {meuPerfil && (
                <EditarPerfilModal aberto={editando} onFechar={() => setEditando(false)} perfil={meuPerfil} />
            )}
        </div>
    )
}
