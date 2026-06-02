import { api } from './api'
import type { Perfil } from '@/context/AuthContext'

export interface MeuPerfil {
    id: number
    nome: string
    email: string
    perfil: Perfil
}

export async function buscarMeuPerfil(): Promise<MeuPerfil> {
    const { data } = await api.get<MeuPerfil>('/me')
    return data
}

export interface AtualizarPerfilInput {
    nome: string
    email: string
    senha?: string
}

export async function atualizarMeuPerfil(input: AtualizarPerfilInput): Promise<MeuPerfil> {
    const { data } = await api.patch<MeuPerfil>('/me', input)
    return data
}
