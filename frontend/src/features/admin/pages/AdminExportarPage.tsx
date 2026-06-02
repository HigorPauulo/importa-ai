import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { PageHeader, BellButton } from '@/components/layout/PageHeader'
import { Card } from '@/components/ui/Card'
import { Input } from '@/components/ui/Input'
import { Select } from '@/components/ui/Select'
import { Button } from '@/components/ui/Button'
import { useToast } from '@/context/ToastContext'
import { listarTodosPedidos } from '@/services/pedidos'
import type { Pedido } from '@/types/pedidos'

function gerarCsv(pedidos: Pedido[]): string {
    const cabecalho = ['Código', 'Produto', 'Status', 'Cliente', 'Atualização']
    const escapar = (campo: string) => `"${campo.replace(/"/g, '""')}"`
    const linhas = pedidos.map((p) =>
        [p.codigo, p.produto, p.status, p.usuarioId != null ? `Cliente #${p.usuarioId}` : '', p.atualizacao].map(escapar).join(','),
    )
    return [cabecalho.map(escapar).join(','), ...linhas].join('\n')
}

function baixarArquivo(conteudo: string, nome: string) {
    const blob = new Blob([conteudo], { type: 'text/csv;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = nome
    link.click()
    URL.revokeObjectURL(url)
}

function AdminExportarPage() {
    const { showToast } = useToast()
    const [status, setStatus] = useState('TODOS')
    const [de, setDe] = useState('')
    const [ate, setAte] = useState('')
    const [cliente, setCliente] = useState('')
    const [formato, setFormato] = useState<'CSV' | 'XLSX'>('CSV')

    const { data: pedidos = [] } = useQuery({ queryKey: ['admin', 'pedidos'], queryFn: listarTodosPedidos })

    const gerar = () => {
        const termo = cliente.trim().toLowerCase()
        const filtrados = pedidos
            .filter((p) => status === 'TODOS' || p.status === status)
            .filter((p) => termo === '' || (p.usuarioId != null && `cliente #${p.usuarioId}`.includes(termo)))

        if (filtrados.length === 0) {
            showToast('Nenhum pedido corresponde aos filtros.')
            return
        }

        baixarArquivo(gerarCsv(filtrados), `pedidos-${new Date().toISOString().slice(0, 10)}.csv`)
        showToast(
            formato === 'XLSX'
                ? `Exportação XLSX chega na v2 — gerado CSV com ${filtrados.length} pedido(s).`
                : `Arquivo gerado com ${filtrados.length} pedido(s).`,
        )
    }

    const contagem = pedidos
        .filter((p) => status === 'TODOS' || p.status === status)
        .filter((p) => {
            const termo = cliente.trim().toLowerCase()
            return termo === '' || (p.usuarioId != null && `cliente #${p.usuarioId}`.includes(termo))
        }).length

    return (
        <>
            <PageHeader titulo="Exportar pedidos" subtitulo="Gere um arquivo dos pedidos com os filtros aplicados" acao={<BellButton estatico />} />

            <Card className="p-6">
                <div className="mb-[18px]">
                    <h2 className="text-lg font-semibold text-ink">Filtros de exportação</h2>
                    <p className="mt-1 text-sm text-secondary">
                        Selecione os filtros e o formato. Toda exportação é auditada (quem, quando, filtros).
                    </p>
                </div>

                <div className="space-y-[18px]">
                    <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
                        <Select label="Status" name="status" value={status} onChange={(e) => setStatus(e.target.value)}>
                            <option value="TODOS">Todos</option>
                            <option value="PROCESSANDO">Processando</option>
                            <option value="ENVIADO">Enviado</option>
                            <option value="ENTREGUE">Entregue</option>
                            <option value="CANCELADO">Cancelado</option>
                        </Select>

                        <Input label="Período — de" name="de" type="date" value={de} onChange={(e) => setDe(e.target.value)} />
                        <Input label="Período — até" name="ate" type="date" value={ate} onChange={(e) => setAte(e.target.value)} />
                    </div>

                    <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
                        <Input label="Valor mínimo (R$)" name="valorMin" type="number" placeholder="0,00" />
                        <Input label="Valor máximo (R$)" name="valorMax" type="number" placeholder="10.000,00" />
                        <Select label="Cliente" name="clienteFiltro" value={cliente} onChange={(e) => setCliente(e.target.value)}>
                            <option value="">Todos</option>
                        </Select>
                    </div>

                    <div className="flex flex-col gap-1.5">
                        <span className="text-[12px] text-secondary">Formato</span>
                        <div className="flex gap-2">
                            <button
                                type="button"
                                onClick={() => setFormato('CSV')}
                                className={`rounded-[8px] px-[22px] py-[10px] text-[14px] font-medium transition-colors ${formato === 'CSV' ? 'bg-primary text-white' : 'bg-background text-secondary'}`}
                            >
                                CSV
                            </button>
                            <button
                                type="button"
                                onClick={() => setFormato('XLSX')}
                                className={`rounded-[8px] px-[22px] py-[10px] text-[14px] font-medium transition-colors ${formato === 'XLSX' ? 'bg-primary text-white' : 'bg-background text-secondary'}`}
                            >
                                XLSX
                            </button>
                        </div>
                    </div>

                    <div className="rounded-[8px] bg-primary-light px-4 py-3 text-[14px] font-medium text-primary-dark">
                        ≈ {contagem.toLocaleString('pt-BR')} pedidos correspondem aos filtros atuais.
                    </div>

                    <div>
                        <Button type="button" onClick={gerar}>Exportar arquivo</Button>
                    </div>
                </div>
            </Card>
        </>
    )
}

export default AdminExportarPage
