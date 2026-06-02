import type { EtapaHistorico } from '@/types/pedidos'

export function Timeline({ historico }: { historico?: EtapaHistorico[] }) {
    if (!historico || historico.length === 0) {
        return <p className="text-sm text-secondary">Ainda não há eventos de rastreio.</p>
    }

    return (
        <ol className="relative space-y-4">
            {historico.map((etapa, idx) => {
                const atual = idx === 0

                return (
                    <li key={`${etapa.data}-${etapa.hora}-${idx}`} className="flex items-start gap-3 py-1">
                        <span
                            aria-hidden
                            className={`relative z-10 mt-1 shrink-0 rounded-full ${
                                atual ? 'h-4 w-4 bg-primary' : 'h-3.5 w-3.5 bg-success'
                            }`}
                        />

                        <div className="flex-1">
                            <p className={`text-[12px] font-bold leading-[16px] ${atual ? 'text-primary-dark' : 'text-primary-dark'}`}>
                                {etapa.data} - {etapa.hora}
                            </p>
                            <p className="mt-0.5 text-[14px] leading-[20px] text-ink">{etapa.descricao}</p>
                            {etapa.local && <p className="mt-0.5 text-[12px] leading-[16px] text-secondary">{etapa.local}</p>}
                        </div>
                    </li>
                )
            })}
        </ol>
    )
}
