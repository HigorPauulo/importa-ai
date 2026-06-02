import type { Perfil } from '@/context/AuthContext'

export function nomeExibicao(email: string | null, perfil: Perfil | null): string {
    if (perfil === 'ADMINISTRADOR') return 'Administrador'
    if (!email) return 'Cliente'
    const local = email.split('@')[0]
    return local
        .split(/[._-]+/)
        .filter(Boolean)
        .map((parte) => parte.charAt(0).toUpperCase() + parte.slice(1))
        .join(' ')
}

export function iniciais(nome: string): string {
    const partes = nome.trim().split(/\s+/)
    const primeira = partes[0]?.charAt(0) ?? ''
    const ultima = partes.length > 1 ? partes[partes.length - 1].charAt(0) : ''
    return (primeira + ultima).toUpperCase()
}
