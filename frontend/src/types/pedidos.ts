export type StatusPedido =
  | 'PROCESSANDO'
  | 'ENVIADO'
  | 'ENTREGUE'
  | 'CANCELADO'

export type TipoEtapa =
  | 'NA_CHINA'
  | 'AEROPORTO_ORIGEM'
  | 'EM_TRANSITO'
  | 'AEROPORTO_DESTINO'
  | 'NO_BRASIL'
  | 'TAXA'
  | 'CD_BRASIL'
  | 'SAIDA_ENTREGA'
  | 'ENTREGUE'

export type Moeda = 'BRL' | 'USD' | 'CNY' | 'EUR'

  export interface EtapaHistorico {
    data: string
    hora: string
    descricao: string
    local: string
  }

  export interface Pedido {
    id: number
    usuarioId?: number
    codigo: string
    status: StatusPedido
    etapa?: TipoEtapa
    produto: string
    atualizacao: string
    cidade: string
    origem?: string
    valorEstimado?: number
    moeda?: Moeda
    rastreioNaoLocalizado?: boolean
    historico?: EtapaHistorico[]
  }