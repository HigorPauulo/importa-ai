import { api } from './api'
import type { StatusPedido } from '@/types/pedidos'

export interface StatusKpi {
    status: StatusPedido
    quantidade: number
    percentual: number
}

export interface PontoEvolucao {
    dia: string
    total: number
}

export interface Dashboard {
    totalAtivos: number
    taxaPendente: number
    entreguesNoMes: number
    total: number
    porStatus: StatusKpi[]
    evolucao: PontoEvolucao[]
}

export async function buscarDashboard(): Promise<Dashboard> {
    const { data } = await api.get<Dashboard>('/admin/dashboard')
    return data
}
