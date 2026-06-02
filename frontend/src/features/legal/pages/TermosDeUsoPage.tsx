import { Link } from 'react-router-dom'
import { LegalLayout, LegalSection } from '@/components/layout/LegalLayout'

function TermosDeUsoPage() {
    return (
        <LegalLayout titulo="Termos de Uso" atualizadoEm="2 de junho de 2026">
            <p>
                Estes Termos de Uso regulam o acesso e a utilização da plataforma Importa Aí, um serviço
                de gestão e acompanhamento de encomendas internacionais. Ao criar uma conta ou utilizar a
                plataforma, você declara ter lido, compreendido e concordado integralmente com estes termos.
            </p>

            <LegalSection titulo="1. Aceitação dos Termos">
                <p>
                    O uso da plataforma está condicionado à aceitação destes Termos de Uso e da{' '}
                    <Link to="/politica-de-privacidade" className="text-primary hover:underline">
                        Política de Privacidade
                    </Link>
                    . Caso não concorde com qualquer disposição aqui prevista, você não deve criar conta
                    nem utilizar o serviço.
                </p>
            </LegalSection>

            <LegalSection titulo="2. Descrição do Serviço">
                <p>
                    O Importa Aí permite que usuários cadastrem encomendas, acompanhem o histórico de
                    rastreamento, consultem cotações de câmbio e recebam notificações sobre mudanças de
                    status de seus pedidos. As informações de rastreamento são obtidas de fontes externas
                    (transportadoras e serviços de rastreio) e refletem os dados por elas disponibilizados.
                </p>
            </LegalSection>

            <LegalSection titulo="3. Cadastro e Conta">
                <p>
                    Para utilizar os recursos da plataforma é necessário criar uma conta com informações
                    verdadeiras, completas e atualizadas. Você é responsável por manter a confidencialidade
                    de suas credenciais e por todas as atividades realizadas em sua conta.
                </p>
                <p>
                    Notifique-nos imediatamente em caso de uso não autorizado ou de qualquer violação de
                    segurança relacionada à sua conta.
                </p>
            </LegalSection>

            <LegalSection titulo="4. Uso Aceitável">
                <p>Ao utilizar a plataforma, você concorda em não:</p>
                <ul className="list-disc space-y-1 pl-5">
                    <li>Fornecer informações falsas ou de terceiros sem autorização;</li>
                    <li>Tentar acessar contas, dados ou áreas restritas que não lhe pertencem;</li>
                    <li>Realizar engenharia reversa, sobrecarregar ou comprometer a infraestrutura do serviço;</li>
                    <li>Utilizar a plataforma para fins ilícitos ou que violem direitos de terceiros.</li>
                </ul>
            </LegalSection>

            <LegalSection titulo="5. Rastreamento e Informações de Terceiros">
                <p>
                    Os dados de rastreamento são fornecidos por transportadoras e serviços externos. O
                    Importa Aí não controla a origem dessas informações e não garante sua exatidão,
                    completude ou disponibilidade em tempo real, atuando apenas como agregador e
                    facilitador da consulta.
                </p>
            </LegalSection>

            <LegalSection titulo="6. Propriedade Intelectual">
                <p>
                    A marca, o layout, os textos, o código e os demais elementos da plataforma são de
                    titularidade do Importa Aí ou de seus licenciadores, sendo vedada a reprodução ou
                    utilização sem autorização prévia e por escrito.
                </p>
            </LegalSection>

            <LegalSection titulo="7. Limitação de Responsabilidade">
                <p>
                    A plataforma é disponibilizada no estado em que se encontra. O Importa Aí não se
                    responsabiliza por atrasos, extravios ou divergências decorrentes da atuação de
                    transportadoras, órgãos aduaneiros ou serviços externos de rastreamento, tampouco por
                    indisponibilidades temporárias causadas por fatores fora de seu controle.
                </p>
            </LegalSection>

            <LegalSection titulo="8. Suspensão e Encerramento">
                <p>
                    Podemos suspender ou encerrar contas que violem estes termos ou a legislação aplicável.
                    Você pode solicitar o encerramento da sua conta a qualquer momento por meio dos canais
                    de contato indicados abaixo.
                </p>
            </LegalSection>

            <LegalSection titulo="9. Alterações dos Termos">
                <p>
                    Estes termos podem ser atualizados periodicamente. Alterações relevantes serão
                    comunicadas pela plataforma, e o uso continuado após a atualização representa a
                    concordância com a nova versão.
                </p>
            </LegalSection>

            <LegalSection titulo="10. Lei Aplicável e Foro">
                <p>
                    Estes termos são regidos pelas leis da República Federativa do Brasil. Fica eleito o
                    foro do domicílio do usuário para dirimir eventuais controvérsias.
                </p>
            </LegalSection>

            <LegalSection titulo="11. Contato">
                <p>
                    Em caso de dúvidas sobre estes Termos de Uso, entre em contato pelo e-mail{' '}
                    <a href="mailto:contato@importaai.com.br" className="text-primary hover:underline">
                        contato@importaai.com.br
                    </a>
                    .
                </p>
            </LegalSection>
        </LegalLayout>
    )
}

export default TermosDeUsoPage
