import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { PageHeader, BellButton } from '@/components/layout/PageHeader'
import { Card } from '@/components/ui/Card'
import { StatusBadge } from '@/components/ui/StatusBadge'
import { filtrarPorSituacao, type FiltroPedido } from '@/features/pedidos/utils/filtros'
import { IconSearch } from '@/components/layout/icons'
import { listarTodosPedidos } from '@/services/pedidos'
import { listarUsuarios } from '@/services/admin'
import type { Pedido } from '@/types/pedidos'

const TABS: { label: string; valor: FiltroPedido }[] = [
    { label: 'Todos', valor: 'TODOS' },
    { label: 'Em Trânsito', valor: 'EM_TRANSITO' },
    { label: 'Taxados', valor: 'TAXADOS' },
    { label: 'Entregues', valor: 'ENTREGUES' },
]

function AdminPedidosPage() {
    const [busca, setBusca] = useState('')
    const [filtro, setFiltro] = useState<FiltroPedido>('TODOS')

    const { data: pedidos, isLoading, isError } = useQuery({ queryKey: ['admin', 'pedidos'], queryFn: listarTodosPedidos })
    const { data: usuarios } = useQuery({ queryKey: ['admin', 'usuarios'], queryFn: listarUsuarios })

    const nomePorId = new Map((usuarios ?? []).map((u) => [u.id, u.nome]))
    const nomeCliente = (pedido: Pedido): string =>
        pedido.usuarioId != null ? (nomePorId.get(pedido.usuarioId) ?? `Cliente #${pedido.usuarioId}`) : '—'

    const lista = pedidos ?? []
    const termo = busca.trim().toLowerCase()
    const filtrados = lista
        .filter((p) => filtrarPorSituacao(p, filtro))
        .filter((p) =>
            termo === '' ||
            p.codigo.toLowerCase().includes(termo) ||
            p.produto.toLowerCase().includes(termo) ||
            nomeCliente(p).toLowerCase().includes(termo),
        )

    const vazio = !isLoading && !isError && filtrados.length === 0

    return (
        <>
            <PageHeader titulo="Pedidos" subtitulo="Todos os pedidos do sistema" acao={<BellButton estatico />} />

            <div className="lg:hidden">
                <div className="relative mb-4">
                    <IconSearch className="pointer-events-none absolute left-[14px] top-1/2 h-4 w-4 -translate-y-1/2 text-secondary" />
                    <input
                        value={busca}
                        onChange={(e) => setBusca(e.target.value)}
                        placeholder="Buscar por código, produto ou cliente"
                        className="h-11 w-full rounded-[8px] border border-gray-200 bg-white pl-[38px] pr-3 text-sm text-ink placeholder:text-secondary focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20"
                    />
                </div>

                <div className="scrollbar-hide mb-5 flex gap-2 overflow-x-auto">
                    {TABS.map((tab) => (
                        <button
                            key={tab.valor}
                            type="button"
                            onClick={() => setFiltro(tab.valor)}
                            className={`shrink-0 rounded-[8px] px-[14px] py-2 text-[13px] font-semibold transition-colors ${
                                filtro === tab.valor ? 'bg-primary text-white' : 'border border-gray-200 bg-white text-secondary'
                            }`}
                        >
                            {tab.label}
                        </button>
                    ))}
                </div>

                <div className="space-y-3">
                    {filtrados.map((pedido) => (
                        <Link
                            key={pedido.id}
                            to={`/admin/pedidos/${pedido.id}`}
                            className="block rounded-[8px] bg-white p-4 shadow-[0px_1px_2px_rgba(0,0,0,0.08)]"
                        >
                            <div className="flex items-start justify-between gap-2">
                                <span className="text-[15px] font-semibold text-primary-dark">{pedido.codigo}</span>
                                <StatusBadge status={pedido.status} />
                            </div>
                            <h3 className="mt-2 text-[16px] font-medium leading-[22px] text-primary-dark">{pedido.produto}</h3>
                            <p className="mt-1.5 text-[13px] leading-[18px] text-secondary">Cliente: {nomeCliente(pedido)}</p>
                            <p className="mt-1 text-[12px] leading-[16px] text-secondary">{pedido.atualizacao} · {pedido.cidade}</p>
                            <div className="mt-3 rounded-[8px] bg-primary-light py-2 text-center text-[13px] font-medium text-primary-dark">
                                Ver detalhes
                            </div>
                        </Link>
                    ))}
                </div>
            </div>

            <Card className="hidden overflow-hidden lg:block">
                <div className="flex items-center justify-between gap-4 p-5">
                    <div className="relative w-[360px]">
                        <IconSearch className="pointer-events-none absolute left-[14px] top-1/2 h-4 w-4 -translate-y-1/2 text-secondary" />
                        <input
                            value={busca}
                            onChange={(e) => setBusca(e.target.value)}
                            placeholder="Buscar por código, produto ou cliente"
                            className="h-[44px] w-full rounded-[8px] border border-secondary bg-white pl-[38px] pr-3 text-sm text-secondary focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20"
                        />
                    </div>
                    <div className="flex gap-2">
                        {TABS.map((tab) => (
                            <button
                                key={tab.valor}
                                type="button"
                                onClick={() => setFiltro(tab.valor)}
                                className={`rounded-[8px] px-[14px] py-2 text-[13px] font-semibold transition-colors ${
                                    filtro === tab.valor ? 'bg-primary text-white' : 'bg-background text-secondary'
                                }`}
                            >
                                {tab.label}
                            </button>
                        ))}
                    </div>
                </div>

                {filtrados.length > 0 && (
                    <div className="overflow-x-auto">
                        <table className="w-full text-left text-sm">
                            <thead>
                                <tr className="border-b border-gray-200 text-[11px] font-bold uppercase text-secondary">
                                    <th className="px-5 py-3 font-bold">Código</th>
                                    <th className="px-5 py-3 font-bold">Produto</th>
                                    <th className="px-5 py-3 font-bold">Cliente</th>
                                    <th className="px-5 py-3 font-bold">Status</th>
                                    <th className="px-5 py-3 font-bold">Atualização</th>
                                    <th className="px-5 py-3 font-bold">Ações</th>
                                </tr>
                            </thead>
                            <tbody>
                                {filtrados.map((pedido) => (
                                    <tr key={pedido.id} className="border-b border-gray-100 last:border-0 hover:bg-gray-50/60">
                                        <td className="px-5 py-[14px]">
                                            <span className="text-[13px] font-semibold text-primary-dark">{pedido.codigo}</span>
                                        </td>
                                        <td className="px-5 py-[14px]">
                                            <span className="text-[14px] font-medium text-primary-dark">{pedido.produto}</span>
                                        </td>
                                        <td className="px-5 py-[14px] text-[14px] text-secondary">{nomeCliente(pedido)}</td>
                                        <td className="px-5 py-[14px]"><StatusBadge status={pedido.status} /></td>
                                        <td className="px-5 py-[14px] text-[13px] text-secondary">{pedido.atualizacao}</td>
                                        <td className="px-5 py-[14px]">
                                            <Link
                                                to={`/admin/pedidos/${pedido.id}`}
                                                className="inline-flex items-center rounded-[8px] bg-primary-light px-3 py-1.5 text-[12px] text-primary-dark hover:opacity-80"
                                            >
                                                Ver detalhes
                                            </Link>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </Card>

            {isLoading && <p className="mt-4 text-secondary">Carregando pedidos...</p>}
            {isError && <p className="mt-4 text-error">Não foi possível carregar os pedidos.</p>}
            {vazio && (
                <p className="mt-4 rounded-[8px] bg-white p-6 text-center text-secondary shadow-[0px_1px_2px_rgba(0,0,0,0.08)] lg:bg-transparent lg:shadow-none">
                    Nenhum pedido encontrado.
                </p>
            )}
        </>
    )
}

export default AdminPedidosPage
