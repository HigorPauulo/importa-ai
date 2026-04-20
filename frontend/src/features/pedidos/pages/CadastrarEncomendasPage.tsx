import { Link } from 'react-router-dom'
import Footer from '../../../components/Footer'
import backIcon from '../../../assets/icon-back.svg'
import CadastrarEncomendaForm from '../components/CadastrarEncomendaForm'

function CadastrarEncomendas() {
    return (
        <div className="min-h-dvh bg-background flex flex-col px-5">

            <div className="w-full max-w-3xl mx-auto">
                <header className="flex items-center justify-between my-8 pt-5">
                        <Link to="/dashboard">
                            <div className="w-10 lg:w-13 h-10 lg:h-13 bg-white shadow-md rounded-[5px] flex items-center justify-center">
                                <figure>
                                    <img src={backIcon} alt="Voltar" className="h-5 lg:h-7" />
                                </figure>
                            </div>
                        </Link>
                </header>
            </div>

            <main className="flex-1 flex items-center justify-center">
                <div className="w-full max-w-3xl mx-auto">
                    <div className="bg-white p-8 rounded-lg shadow-md w-full">
                        <CadastrarEncomendaForm />
                    </div>
                </div>
            </main>

            <Footer />
        </div>
    )
}

export default CadastrarEncomendas