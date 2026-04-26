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
  | 'CD_BRASIL'
  | 'SAIDA_ENTREGA'
  | 'ENTREGUE'

  export interface EtapaHistorico {
    data: string
    hora: string
    descricao: string
    local: string
  }

  export interface Pedido {
    codigo: string
    status: StatusPedido
    etapa?: TipoEtapa
    produto: string
    atualizacao: string
    cidade: string
    origem?: string
    valorEstimado?: number
    historico?: EtapaHistorico[]
  }