import type { TipoEtapa } from '@/types/pedidos'

export function getEtapaColor(etapa: TipoEtapa | undefined) {
    switch (etapa) {
        case 'NA_CHINA':
            return 'bg-etapa-na-china-bg text-etapa-na-china-text border border-etapa-na-china-bg'
        case 'AEROPORTO_ORIGEM':
            return 'bg-etapa-aeroporto-origem-bg text-etapa-aeroporto-origem-text border border-etapa-aeroporto-origem-bg'
        case 'EM_TRANSITO':
            return 'bg-etapa-em-transito-bg text-etapa-em-transito-text border border-etapa-em-transito-bg'
        case 'AEROPORTO_DESTINO':
            return 'bg-etapa-aeroporto-destino-bg text-etapa-aeroporto-destino-text border border-etapa-aeroporto-destino-bg'
        case 'NO_BRASIL':
            return 'bg-etapa-no-brasil-bg text-etapa-no-brasil-text border border-etapa-no-brasil-bg'
        case 'CD_BRASIL':
            return 'bg-etapa-cd-brasil-bg text-etapa-cd-brasil-text border border-etapa-cd-brasil-bg'
        case 'SAIDA_ENTREGA':
            return 'bg-etapa-saida-entrega-bg text-etapa-saida-entrega-text border border-etapa-saida-entrega-bg'
        case 'ENTREGUE':
            return 'bg-etapa-entregue-bg text-etapa-entregue-text border border-etapa-entregue-bg'
        default:
            return 'bg-neutral-50 text-neutral-700 border border-neutral-200'
    }
}

export function getEtapaLabel(etapa: TipoEtapa | undefined) {
    switch (etapa) {
        case 'NA_CHINA':
            return 'Na China'
        case 'AEROPORTO_ORIGEM':
            return 'Aeroporto Origem'
        case 'EM_TRANSITO':
            return 'Em Trânsito'
        case 'AEROPORTO_DESTINO':
            return 'Aeroporto Destino'
        case 'NO_BRASIL':
            return 'No Brasil'
        case 'CD_BRASIL':
            return 'CD Brasil'
        case 'SAIDA_ENTREGA':
            return 'Saída Entrega'
        case 'ENTREGUE':
            return 'Entregue'
        default:
            return 'Não definido'
    }
}