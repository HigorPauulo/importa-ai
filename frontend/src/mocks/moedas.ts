import type { Moeda } from '@/types/moeda'

export const moedas: Moeda[] = [
    {
        nome: 'Dólar Americano',
        pais: 'Estados Unidos',
        sigla: 'USD',
        tipo: 'USD',
        valor: 5.20,
        atualizacao: '10 min'
    },
    {
        nome: 'Euro',
        pais: 'União Europeia',
        sigla: 'EUR',
        tipo: 'EUR',
        valor: 5.60,
        atualizacao: '10 min'
    },
    {
        nome: 'Yuan Chinês',
        pais: 'China',
        sigla: 'CNY',
        tipo: 'CNY',
        valor: 0.72,
        atualizacao: '10 min'
    }
]