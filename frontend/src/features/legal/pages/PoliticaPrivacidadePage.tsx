import { Link } from 'react-router-dom'
import { LegalLayout, LegalSection } from '@/components/layout/LegalLayout'

function PoliticaPrivacidadePage() {
    return (
        <LegalLayout titulo="Política de Privacidade" atualizadoEm="2 de junho de 2026">
            <p>
                Esta Política de Privacidade descreve como o Importa Aí coleta, utiliza, armazena e protege
                os dados pessoais dos seus usuários, em conformidade com a Lei Geral de Proteção de Dados
                (Lei nº 13.709/2018 – LGPD). Ao utilizar a plataforma, você concorda com as práticas aqui
                descritas.
            </p>

            <LegalSection titulo="1. Dados que Coletamos">
                <ul className="list-disc space-y-1 pl-5">
                    <li>
                        <strong className="text-ink">Dados de cadastro:</strong> nome, e-mail e senha (armazenada
                        de forma criptografada).
                    </li>
                    <li>
                        <strong className="text-ink">Dados de uso:</strong> encomendas cadastradas, códigos de
                        rastreamento, descrições, valores declarados e histórico de etapas.
                    </li>
                    <li>
                        <strong className="text-ink">Dados técnicos:</strong> informações necessárias à autenticação
                        e ao funcionamento da sessão, como tokens de acesso.
                    </li>
                </ul>
            </LegalSection>

            <LegalSection titulo="2. Como Usamos seus Dados">
                <ul className="list-disc space-y-1 pl-5">
                    <li>Prestar o serviço de gestão e acompanhamento de encomendas;</li>
                    <li>Consultar e atualizar o rastreamento junto a serviços externos;</li>
                    <li>Enviar notificações sobre mudanças de status dos seus pedidos;</li>
                    <li>Garantir a segurança da conta e prevenir fraudes;</li>
                    <li>Aprimorar a experiência e o funcionamento da plataforma.</li>
                </ul>
            </LegalSection>

            <LegalSection titulo="3. Base Legal">
                <p>
                    O tratamento dos seus dados fundamenta-se na execução do contrato de prestação de
                    serviço, no cumprimento de obrigações legais, no legítimo interesse e, quando
                    aplicável, no seu consentimento, conforme previsto na LGPD.
                </p>
            </LegalSection>

            <LegalSection titulo="4. Compartilhamento de Dados">
                <p>
                    Não vendemos seus dados pessoais. O compartilhamento ocorre apenas com serviços
                    estritamente necessários à operação — como provedores de rastreamento e de
                    infraestrutura — e somente na medida indispensável à prestação do serviço, sempre
                    observando a confidencialidade e a segurança das informações.
                </p>
            </LegalSection>

            <LegalSection titulo="5. Armazenamento e Segurança">
                <p>
                    Adotamos medidas técnicas e organizacionais para proteger seus dados, incluindo
                    criptografia de senhas, autenticação por tokens e tráfego protegido por HTTPS. Apesar
                    dos esforços, nenhum sistema é totalmente imune a riscos, e nos comprometemos a tratar
                    eventuais incidentes com a devida diligência.
                </p>
            </LegalSection>

            <LegalSection titulo="6. Cookies e Armazenamento Local">
                <p>
                    Utilizamos o armazenamento local do navegador para manter sua sessão autenticada e
                    permitir o funcionamento da plataforma. Esses dados ficam no seu dispositivo e podem ser
                    removidos ao encerrar a sessão ou limpar os dados do navegador.
                </p>
            </LegalSection>

            <LegalSection titulo="7. Seus Direitos">
                <p>Nos termos da LGPD, você pode, a qualquer momento, solicitar:</p>
                <ul className="list-disc space-y-1 pl-5">
                    <li>Confirmação da existência de tratamento e acesso aos seus dados;</li>
                    <li>Correção de dados incompletos, inexatos ou desatualizados;</li>
                    <li>Anonimização, bloqueio ou eliminação de dados desnecessários;</li>
                    <li>Eliminação dos dados tratados com base no seu consentimento;</li>
                    <li>Informações sobre o compartilhamento dos seus dados.</li>
                </ul>
            </LegalSection>

            <LegalSection titulo="8. Retenção de Dados">
                <p>
                    Mantemos seus dados pelo tempo necessário à prestação do serviço e ao cumprimento de
                    obrigações legais. Encerrada a conta, os dados são eliminados ou anonimizados, salvo
                    quando a sua retenção for exigida por lei.
                </p>
            </LegalSection>

            <LegalSection titulo="9. Alterações desta Política">
                <p>
                    Esta política pode ser atualizada para refletir mudanças na plataforma ou na
                    legislação. Alterações relevantes serão comunicadas, e recomendamos a revisão periódica
                    deste documento.
                </p>
            </LegalSection>

            <LegalSection titulo="10. Contato">
                <p>
                    Para exercer seus direitos ou esclarecer dúvidas sobre o tratamento dos seus dados,
                    entre em contato pelo e-mail{' '}
                    <a href="mailto:contato@importaai.com.br" className="text-primary hover:underline">
                        contato@importaai.com.br
                    </a>
                    . Consulte também os nossos{' '}
                    <Link to="/termos-de-uso" className="text-primary hover:underline">
                        Termos de Uso
                    </Link>
                    .
                </p>
            </LegalSection>
        </LegalLayout>
    )
}

export default PoliticaPrivacidadePage
