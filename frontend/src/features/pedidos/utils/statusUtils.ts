import type { TipoEtapa } from '@/types/pedidos'

export function getEtapaColor(etapa: TipoEtapa) {
    switch (etapa) {
        case 'NA_CHINA':
            return 'bg-blue-50 text-blue-700 border border-blue-200'
        case 'AEROPORTO_ORIGEM':
            return 'bg-cyan-100 text-cyan-800 border border-cyan-200'
        case 'EM_TRANSITO':
            return 'bg-amber-50 text-amber-700 border border-amber-200'
        case 'AEROPORTO_DESTINO':
            return 'bg-violet-50 text-violet-700 border border-violet-200'
        case 'NO_BRASIL':
            return 'bg-green-50 text-green-700 border border-green-200'
        case 'CD_BRASIL':
            return 'bg-lime-50 text-lime-700 border border-lime-200'
        case 'SAIDA_ENTREGA':
            return 'bg-yellow-50 text-yellow-700 border border-yellow-200'
        case 'ENTREGUE':
            return 'bg-primary/10 text-primary border border-primary/30'
        default:
            return 'bg-neutral-50 text-neutral-700 border border-neutral-200'
    }
}

export function getEtapaLabel(etapa: TipoEtapa) {
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
            return 'Saida Entrega'
        case 'ENTREGUE':
            return 'Entregue'
        default:
            return 'Não definido'
    }
}