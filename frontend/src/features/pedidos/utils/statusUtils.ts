export function getStatusColor(status: string) {
    switch (status) {
        case 'Na China':
            return 'bg-blue-50 text-blue-700 border border-blue-200'
        case 'Aeroporto Origem':
            return 'bg-cyan-100 text-cyan-800 border border-cyan-200'
        case 'Em Trânsito':
            return 'bg-amber-50 text-amber-700 border border-amber-200'
        case 'Aeroporto Destino':
            return 'bg-violet-50 text-violet-700 border border-violet-200'
        case 'No Brasil':
            return 'bg-green-50 text-green-700 border border-green-200'
        case 'CD_BRASIL':
            return 'bg-lime-50 text-lime-700 border border-lime-200'
        case 'Saida Entrega':
            return 'bg-yellow-50 text-yellow-700 border border-yellow-200'
        case 'Entregue':
            return 'bg-primary/10 text-primary border border-primary/30'
        case 'Taxa pendente':
            return 'bg-orange-100 text-orange-900 border border-orange-200'
        case 'Cancelado':
            return 'bg-red-50 text-red-700 border border-red-200 line-through'
        default:
            return 'bg-neutral-50 text-neutral-700 border border-neutral-200'
    }
}