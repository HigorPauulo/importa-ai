// Aviso exibido quando a transportadora não reconhece o código (flag vinda do backend).
export function AvisoRastreioNaoLocalizado() {
    return (
        <div className="flex items-start gap-3 rounded-[8px] border-l-4 border-warning bg-warning-bg p-4">
            <span
                className="mt-0.5 flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-warning text-[12px] font-bold text-white"
                aria-hidden
            >
                !
            </span>
            <div>
                <p className="text-[14px] font-semibold leading-[20px] text-ink">
                    Código não localizado na transportadora
                </p>
                <p className="mt-0.5 text-[13px] leading-[18px] text-secondary">
                    Confira se o código de rastreio está correto. Assim que a transportadora reconhecer o objeto,
                    o histórico aparece aqui automaticamente.
                </p>
            </div>
        </div>
    )
}
