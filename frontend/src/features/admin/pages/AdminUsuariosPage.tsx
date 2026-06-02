import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { PageHeader } from '@/components/layout/PageHeader'
import { Card } from '@/components/ui/Card'
import { IconSearch } from '@/components/layout/icons'
import { iniciais } from '@/lib/usuario'
import {
    listarUsuarios,
    alterarPerfilUsuario,
    definirStatusUsuario,
    type PerfilUsuario,
    type UsuarioAdmin,
} from '@/services/admin'

const CHAVE = ['admin', 'usuarios'] as const

function AdminUsuariosPage() {
    const queryClient = useQueryClient()
    const [busca, setBusca] = useState('')
    const [perfil, setPerfil] = useState('TODOS')
    const [status, setStatus] = useState('TODOS')

    const { data: usuarios = [], isLoading } = useQuery({ queryKey: CHAVE, queryFn: listarUsuarios })

    const invalidar = () => queryClient.invalidateQueries({ queryKey: CHAVE })
    const { mutate: mudarPerfil } = useMutation({
        mutationFn: ({ id, perfil }: { id: number; perfil: PerfilUsuario }) => alterarPerfilUsuario(id, perfil),
        onSuccess: invalidar,
    })
    const { mutate: mudarStatus } = useMutation({
        mutationFn: ({ id, ativo }: { id: number; ativo: boolean }) => definirStatusUsuario(id, ativo),
        onSuccess: invalidar,
    })

    const termo = busca.trim().toLowerCase()
    const filtrados = usuarios
        .filter((u) => termo === '' || u.nome.toLowerCase().includes(termo) || u.email.toLowerCase().includes(termo))
        .filter((u) => perfil === 'TODOS' || u.perfil === perfil)
        .filter((u) => status === 'TODOS' || (status === 'ATIVO' ? u.ativo : !u.ativo))

    return (
        <>
            <PageHeader titulo="Gestão de usuários" subtitulo="Gerencie perfis, status e acessos" />

            <Card className="overflow-hidden">
                <div className="flex items-center justify-between gap-4 p-5">
                    <div className="relative w-[340px]">
                        <IconSearch className="pointer-events-none absolute left-[14px] top-1/2 h-4 w-4 -translate-y-1/2 text-secondary" />
                        <input
                            value={busca}
                            onChange={(e) => setBusca(e.target.value)}
                            placeholder="Buscar por nome ou e-mail"
                            className="h-[44px] w-full rounded-[8px] border border-secondary bg-white pl-[38px] pr-3 text-sm text-secondary focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20"
                        />
                    </div>
                    <div className="flex gap-[10px]">
                        <select
                            value={perfil}
                            onChange={(e) => setPerfil(e.target.value)}
                            className="rounded-[8px] bg-background px-[14px] py-[9px] text-[13px] font-semibold text-secondary focus:outline-none"
                        >
                            <option value="TODOS">Perfil: Todos</option>
                            <option value="CLIENTE">Cliente</option>
                            <option value="ADMINISTRADOR">Administrador</option>
                        </select>
                        <select
                            value={status}
                            onChange={(e) => setStatus(e.target.value)}
                            className="rounded-[8px] bg-background px-[14px] py-[9px] text-[13px] font-semibold text-secondary focus:outline-none"
                        >
                            <option value="TODOS">Status: Todos</option>
                            <option value="ATIVO">Ativo</option>
                            <option value="INATIVO">Inativo</option>
                        </select>
                    </div>
                </div>

                {isLoading && <p className="p-6 text-secondary">Carregando usuários...</p>}
                {!isLoading && filtrados.length === 0 && (
                    <p className="p-6 text-center text-secondary">Nenhum usuário encontrado.</p>
                )}

                {filtrados.length > 0 && (
                    <div className="overflow-x-auto">
                        <table className="w-full text-left text-sm">
                            <thead>
                                <tr className="border-b border-gray-200 text-[11px] font-bold uppercase text-secondary">
                                    <th className="px-5 py-3 font-bold">Usuário</th>
                                    <th className="px-5 py-3 font-bold">E-mail</th>
                                    <th className="px-5 py-3 font-bold">Perfil</th>
                                    <th className="px-5 py-3 font-bold">Status</th>
                                    <th className="px-5 py-3 font-bold">Ações</th>
                                </tr>
                            </thead>
                            <tbody>
                                {filtrados.map((usuario) => (
                                    <LinhaUsuario
                                        key={usuario.id}
                                        usuario={usuario}
                                        onPerfil={() => mudarPerfil({ id: usuario.id, perfil: usuario.perfil === 'ADMINISTRADOR' ? 'CLIENTE' : 'ADMINISTRADOR' })}
                                        onStatus={() => mudarStatus({ id: usuario.id, ativo: !usuario.ativo })}
                                    />
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </Card>
        </>
    )
}

function LinhaUsuario({ usuario, onPerfil, onStatus }: { usuario: UsuarioAdmin; onPerfil: () => void; onStatus: () => void }) {
    const ehAdmin = usuario.perfil === 'ADMINISTRADOR'
    return (
        <tr className="border-b border-gray-100 last:border-0 hover:bg-gray-50/60">
            <td className="px-5 py-[14px]">
                <div className="flex items-center gap-[10px]">
                    <span className="flex h-[34px] w-[34px] items-center justify-center rounded-full bg-primary-light text-xs font-bold text-primary-dark">
                        {iniciais(usuario.nome)}
                    </span>
                    <span className="text-[14px] font-medium text-primary-dark">{usuario.nome}</span>
                </div>
            </td>
            <td className="px-5 py-[14px] text-[14px] text-secondary">{usuario.email}</td>
            <td className="px-5 py-[14px]">
                <span className={`inline-flex rounded-full px-[10px] py-1 text-[11px] font-bold ${ehAdmin ? 'bg-primary-light text-primary-dark' : 'bg-background text-secondary'}`}>
                    {ehAdmin ? 'Admin' : 'Cliente'}
                </span>
            </td>
            <td className="px-5 py-[14px]">
                <span className={`inline-flex rounded-full px-[10px] py-1 text-[11px] font-bold ${usuario.ativo ? 'bg-success-bg text-success-dark' : 'bg-background text-secondary'}`}>
                    {usuario.ativo ? 'Ativo' : 'Inativo'}
                </span>
            </td>
            <td className="px-5 py-[14px]">
                <div className="flex items-center gap-2">
                    <button
                        type="button"
                        onClick={onPerfil}
                        className="rounded-[8px] bg-primary-light px-3 py-1.5 text-[12px] text-primary-dark hover:opacity-80"
                    >
                        {ehAdmin ? 'Rebaixar' : 'Promover'}
                    </button>
                    <button
                        type="button"
                        onClick={onStatus}
                        className={`rounded-[8px] px-3 py-1.5 text-[12px] hover:opacity-80 ${usuario.ativo ? 'bg-error-bg text-error' : 'bg-success-bg text-success-dark'}`}
                    >
                        {usuario.ativo ? 'Desativar' : 'Ativar'}
                    </button>
                    <button
                        type="button"
                        className="rounded-[8px] bg-background px-3 py-1.5 text-[12px] text-secondary hover:opacity-80"
                    >
                        Redefinir
                    </button>
                </div>
            </td>
        </tr>
    )
}

export default AdminUsuariosPage
