import { api } from './api'

export type PerfilUsuario = 'CLIENTE' | 'ADMINISTRADOR'

export interface UsuarioAdmin {
    id: number
    nome: string
    email: string
    perfil: PerfilUsuario
    ativo: boolean
}

export async function listarUsuarios(): Promise<UsuarioAdmin[]> {
    const { data } = await api.get<UsuarioAdmin[]>('/admin/usuarios')
    return data
}

export async function alterarPerfilUsuario(id: number, perfil: PerfilUsuario): Promise<void> {
    await api.patch(`/admin/usuarios/${id}/perfil`, { perfil })
}

export async function definirStatusUsuario(id: number, ativo: boolean): Promise<void> {
    await api.patch(`/admin/usuarios/${id}/status`, { ativo })
}
