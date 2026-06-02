import type { PontoEvolucao } from '@/services/dashboard'

export function EvolucaoChart({ dados }: { dados: PontoEvolucao[] }) {
    const max = Math.max(1, ...dados.map((d) => d.total))

    return (
        <div className="flex h-[170px] items-end gap-3 overflow-hidden">
            {dados.map((barra) => (
                <div
                    key={barra.dia}
                    className="min-w-0 flex-1 rounded-t-[4px] bg-primary transition-all"
                    style={{ height: `${(barra.total / max) * 100}%` }}
                    title={`${barra.dia}: ${barra.total} pedidos`}
                />
            ))}
        </div>
    )
}
