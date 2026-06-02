import { Link } from 'react-router-dom'
import type { Notificacao } from '@/types/notificacao'
import { categoriaDe, tempoRelativo } from '@/features/notificacoes/utils/notificacaoView'

interface CardNotificacaoProps {
    notificacao: Notificacao
}

function CardNotificacao({ notificacao }: CardNotificacaoProps) {
    const naoLida = !notificacao.lida

    // Não-lida: barra lateral + fundo azul claro + ponto (espelha o Figma).
    const conteudo = (
        <article
            className={`relative overflow-hidden rounded-[8px] border-l-4 p-4 pl-5 shadow-[0px_1px_2px_rgba(0,0,0,0.08)] transition-colors ${
                naoLida ? 'bg-primary/10 border-primary' : 'bg-white border-transparent'
            }`}
        >
            {naoLida && (
                <span className="absolute top-4 right-4 h-2.5 w-2.5 rounded-full bg-primary" aria-hidden />
            )}

            <p className={`mb-1 text-[11px] font-bold uppercase leading-[14px] tracking-wide ${naoLida ? 'text-primary' : 'text-secondary'}`}>
                {categoriaDe(notificacao)}
            </p>
            <p className="pr-6 text-[14px] font-semibold leading-[20px] text-ink">{notificacao.mensagem}</p>
            <p className="mt-3 text-[11px] font-bold leading-[14px] text-secondary">{tempoRelativo(notificacao.criadoEm)}</p>
        </article>
    )

    // Ligada a um pedido => clicável, leva ao detalhe + linha do tempo.
    if (notificacao.pedidoId != null) {
        return (
            <Link to={`/pedidos/${notificacao.pedidoId}`} className="block hover:opacity-90 transition-opacity">
                {conteudo}
            </Link>
        )
    }

    return conteudo
}

export default CardNotificacao
