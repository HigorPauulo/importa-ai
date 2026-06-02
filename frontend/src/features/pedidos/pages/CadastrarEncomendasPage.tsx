import { PageHeader } from '@/components/layout/PageHeader'
import { Card } from '@/components/ui/Card'
import CadastrarEncomendaForm from '@/features/pedidos/components/CadastrarEncomendaForm'

function CadastrarEncomendas() {
    return (
        <>
            <PageHeader titulo="Cadastrar Encomenda" subtitulo="Inicie o rastreio de sua nova importação." />

            <Card className="max-w-2xl p-6 lg:p-8">
                <CadastrarEncomendaForm />
            </Card>
        </>
    )
}

export default CadastrarEncomendas
