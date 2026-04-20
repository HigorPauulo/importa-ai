import type { Pedido } from '@/types/pedidos'

export const user = {
    name: 'Higor Paulo',
    notifications: 10
}

export const pedidos: Pedido[] = [
    {
        codigo: 'XZY123BR',
        status: 'PROCESSANDO',
        etapa: 'NA_CHINA',
        produto: 'Teclado Mecanico Machenik',
        atualizacao: '14:45',
        cidade: 'Goiânia',
    },
    {
        codigo: 'ABC123BR',
        status: 'PROCESSANDO',
        etapa: 'AEROPORTO_DESTINO',
        produto: 'Teclado Mecanico Machenik',
        atualizacao: '14:45',
        cidade: 'Goiânia',
    },
    {
        codigo: 'ABC123BR',
        status: 'ENVIADO',
        etapa: 'AEROPORTO_ORIGEM',
        produto: 'Teclado Mecanico Machenik',
        atualizacao: '14:45',
        cidade: 'Goiânia',
    },
    {
        codigo: 'ABC123BR',
        status: 'ENTREGUE',
        etapa: 'SAIDA_ENTREGA',
        produto: 'Teclado Mecanico Machenik',
        atualizacao: '14:45',
        cidade: 'Goiânia',
    },
]
