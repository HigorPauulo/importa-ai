import type { Pedido } from '@/types/pedidos'

export const user = {
    name: 'Higor Paulo',
    notifications: 10
}

export const pedidos: Pedido[] = [
    {
        codigo: 'CHN100BR',
        status: 'PROCESSANDO',
        etapa: 'NA_CHINA',
        produto: 'Notebook Xiaomi RedmiBook',
        atualizacao: '12:05',
        cidade: 'Pequim',
    },
    {
        codigo: 'ORI200BR',
        status: 'ENVIADO',
        etapa: 'AEROPORTO_ORIGEM',
        produto: 'Smartphone Poco X6',
        atualizacao: '12:31',
        cidade: 'Xangai',
    },
    {
        codigo: 'TRN210BR',
        status: 'ENVIADO',
        etapa: 'EM_TRANSITO',
        produto: 'Caixa de Som JBL',
        atualizacao: '12:44',
        cidade: 'Hong Kong',
    },
    {
        codigo: 'DST220BR',
        status: 'ENVIADO',
        etapa: 'AEROPORTO_DESTINO',
        produto: 'Drone DJI Mini',
        atualizacao: '12:55',
        cidade: 'São Paulo',
    },
    {
        codigo: 'BRA230BR',
        status: 'ENVIADO',
        etapa: 'NO_BRASIL',
        produto: 'Câmera Instax Fuji',
        atualizacao: '13:02',
        cidade: 'Rio de Janeiro',
    },
    {
        codigo: 'CDB240BR',
        status: 'ENVIADO',
        etapa: 'CD_BRASIL',
        produto: 'Relógio Amazfit Bip',
        atualizacao: '13:10',
        cidade: 'Campinas',
    },
    {
        codigo: 'SAE250BR',
        status: 'ENVIADO',
        etapa: 'SAIDA_ENTREGA',
        produto: 'Mini Projetor Wanbo',
        atualizacao: '13:15',
        cidade: 'Curitiba',
    },
    {
        codigo: 'ENT300BR',
        status: 'ENTREGUE',
        etapa: 'ENTREGUE',
        produto: 'Fone de Ouvido Bluetooth',
        atualizacao: '13:20',
        cidade: 'Goiânia',
    },
    {
        codigo: 'CNL400BR',
        status: 'CANCELADO',
        etapa: undefined,
        produto: 'Tablet Lenovo Xiaoxin',
        atualizacao: '14:15',
        cidade: 'São Paulo',
    },
]
