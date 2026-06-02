import type { PontoEvolucao } from '@/services/dashboard'

export function EvolucaoChart({ dados }: { dados: PontoEvolucao[] }) {
    const max = Math.max(1, ...dados.map((d) => d.total))

    return (
        <div className="flex h-[170px] items-end gap-1 sm:gap-2">
            {dados.map((barra) => (
                <div key={barra.dia} className="flex h-full min-w-0 flex-1 items-end">
                    <div
                        className="w-full rounded-t-[4px] bg-primary"
                        style={{ height: `${barra.total > 0 ? Math.max(6, (barra.total / max) * 100) : 0}%` }}
                        title={`${barra.dia}: ${barra.total} pedidos`}
                    />
                </div>
            ))}
        </div>
    )
}
