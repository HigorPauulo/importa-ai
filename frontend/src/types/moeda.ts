export type TipoMoeda = 
    | 'BRL' 
    | 'USD' 
    | 'CNY'
    | 'EUR'

export interface Moeda {
    nome: string
    pais: string
    sigla: string
    tipo: TipoMoeda
    valor: number
    atualizacao: string
    manual?: boolean
}