import { api } from './api'
import type { Pedido, StatusPedido, TipoEtapa, Moeda } from '@/types/pedidos'

interface EtapaResponse {
    tipo: TipoEtapa
    criadoEm: string
    localizacao: string
    descricao: string
}

interface PedidoResponse {
    id: number
    usuarioId: number
    codigoRastreamento: string
    descricao: string
    valorDeclarado: number
    moeda: Moeda
    status: StatusPedido
    cancelado: boolean
    criadoEm: string
    etapas: EtapaResponse[]
}

function formatarData(iso: string): string {
    return new Date(iso).toLocaleDateString('pt-BR', { day: '2-digit', month: 'short', year: 'numeric' })
}

function formatarHora(iso: string): string {
    return new Date(iso).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
}

// Anti-corrupção: traduz o contrato do backend pro modelo que a UI consome.
function toPedidoView(dto: PedidoResponse): Pedido {
    const ultima = dto.etapas.at(-1)
    const instante = ultima?.criadoEm ?? dto.criadoEm

    return {
        id: dto.id,
        codigo: dto.codigoRastreamento,
        status: dto.status,
        etapa: ultima?.tipo,
        produto: dto.descricao,
        atualizacao: formatarHora(instante),
        cidade: ultima?.localizacao ?? '—',
        origem: dto.etapas.at(0)?.localizacao,   // primeira etapa = origem
        valorEstimado: dto.valorDeclarado,
        moeda: dto.moeda,
        historico: [...dto.etapas].reverse().map((e) => ({
            data: formatarData(e.criadoEm),
            hora: formatarHora(e.criadoEm),
            descricao: e.descricao,
            local: e.localizacao,
        })),
    }
}

export async function listarPedidos(): Promise<Pedido[]> {
    const { data } = await api.get<PedidoResponse[]>('/pedidos')
    return data.map(toPedidoView)
}

export interface CriarPedidoInput {
    codigoRastreamento: string
    descricao: string
    valorDeclarado: number
    moeda: 'BRL' | 'USD' | 'CNY'
}

export async function criarPedido(input: CriarPedidoInput): Promise<Pedido> {
    // backend responde 202 com o PedidoResponse criado
    const { data } = await api.post<PedidoResponse>('/pedidos', input)
    return toPedidoView(data)
}

export async function buscarPedido(id: string): Promise<Pedido> {
    const { data } = await api.get<PedidoResponse>(`/pedidos/${id}`)
    return toPedidoView(data)
}
