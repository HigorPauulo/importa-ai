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
    cotadoEm: string
}

// Nome e país são apresentação — o backend só devolve o código da moeda.
const META: Record<string, { nome: string; pais: string }> = {
    USD: { nome: 'Dólar Comercial', pais: 'Estados Unidos' },
    EUR: { nome: 'Euro', pais: 'União Europeia' },
    CNY: { nome: 'Yuan Chinês', pais: 'China' },
}

function tempoDesde(iso: string): string {
    const min = Math.round((Date.now() - new Date(iso).getTime()) / 60000)
    if (min <= 0) return 'agora mesmo'
    if (min < 60) return `há ${min} min`
    const horas = Math.round(min / 60)
    if (horas < 24) return `há ${horas} h`
    return `há ${Math.round(horas / 24)} d`
}

function toMoeda(c: CotacaoResponse): Moeda {
    const meta = META[c.moedaOrigem] ?? { nome: c.moedaOrigem, pais: '' }
    return {
        nome: meta.nome,
        pais: meta.pais,
        sigla: c.moedaOrigem,
        tipo: c.moedaOrigem as TipoMoeda,
        valor: c.taxa,
        atualizacao: tempoDesde(c.cotadoEm),
        manual: c.manual,
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

export interface CotacaoManualInput {
    moedaOrigem: TipoMoeda
    taxa: number
    validoAte?: string
}

export async function definirCotacaoManual(input: CotacaoManualInput): Promise<void> {
    await api.post('/admin/cotacoes', {
        moedaOrigem: input.moedaOrigem,
        moedaDestino: 'BRL',
        taxa: input.taxa,
        validoAte: input.validoAte ?? null,
    })
}
