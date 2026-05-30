import { api } from './api'
import type { Moeda, TipoMoeda } from '@/types/moeda'

// Espelha o CotacaoResponse do backend (GET /api/cotacoes/{moeda}).
interface CotacaoResponse {
    moedaOrigem: string
    moedaDestino: string
    taxa: number
    manual: boolean
    desatualizada: boolean
    atualizadoEm: string
}

// Nome e país são apresentação — o backend só devolve o código da moeda.
const META: Record<string, { nome: string; pais: string }> = {
    USD: { nome: 'Dólar Americano', pais: 'Estados Unidos' },
    EUR: { nome: 'Euro', pais: 'União Europeia' },
    CNY: { nome: 'Yuan Chinês', pais: 'China' },
}

// "há X min" a partir do timestamp de atualização do backend.
function minutosDesde(iso: string): string {
    const min = Math.max(0, Math.round((Date.now() - new Date(iso).getTime()) / 60000))
    return `${min} min`
}

function toMoeda(c: CotacaoResponse): Moeda {
    const meta = META[c.moedaOrigem] ?? { nome: c.moedaOrigem, pais: '' }
    return {
        nome: meta.nome,
        pais: meta.pais,
        sigla: c.moedaOrigem,
        tipo: c.moedaOrigem as TipoMoeda,
        valor: c.taxa,
        atualizacao: minutosDesde(c.atualizadoEm),
    }
}

export async function buscarCotacao(code: string): Promise<Moeda> {
    const { data } = await api.get<CotacaoResponse>(`/cotacoes/${code}`)
    return toMoeda(data)
}

// Busca as 3 moedas em paralelo; uma que falhar é omitida (degrada sem quebrar a tela).
export async function buscarCotacoes(): Promise<Moeda[]> {
    const codes = ['USD', 'EUR', 'CNY']
    const resultados = await Promise.allSettled(codes.map(buscarCotacao))
    return resultados
        .filter((r): r is PromiseFulfilledResult<Moeda> => r.status === 'fulfilled')
        .map((r) => r.value)
}
