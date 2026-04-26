import Footer from '@/components/Footer'
import { Link } from 'react-router-dom'
import backIcon from '@/assets/icon-back.svg'
import CardMoeda from '@/features/cotacao/components/CardMoeda'
import CardMoedaPrincipal from '@/features/cotacao/components/CardMoedaPrincipal'   
import { moedas } from '@/mocks/moedas'

function CotacaoPage() {
    return (
        <div className="min-h-dvh bg-background flex flex-col px-5"> 
            <header className="w-full max-w-3xl mx-auto flex items-center justify-between my-8 pt-5">
                <Link to="/dashboard">
                    <div className="w-10 lg:w-13 h-10 lg:h-13 bg-white shadow-md rounded-[5px] flex items-center justify-center">
                        <figure>
                            <img src={backIcon} alt="Voltar" className="h-5 lg:h-7" />
                        </figure>
                    </div>
                </Link>
            </header>

            <main className="flex-1 flex justify-center">
                <div className="w-full max-w-3xl">
                    <header className="mb-6">
                        <h2 className="text-2xl font-bold mb-2">Cotação de câmbio</h2>
                        <p className="text-gray-500">Valores atualizados para conversão em BRL.</p>
                    </header>

                    <CardMoedaPrincipal moeda={moedas[0]} />

                    <div className="mt-10">
                        <h3 className="text-1xl lg:text-2xl text-gray-500 font-bold mb-2">Outras moedas</h3>
                        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 mt-2">
                            <CardMoeda moeda={moedas[1]} />
                            <CardMoeda moeda={moedas[2]} />
                        </div>
                    </div>
                </div>
            </main>

            <Footer />
        </div>
    )
}

export default CotacaoPage