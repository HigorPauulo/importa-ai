import logo from '../assets/logo.png'

function Header() {
    return (
        <header className="flex justify-center items-center mb-3 lg:mb-10">
            <img src={logo} alt="Importa Aí" className="h-25 lg:h-35" />
        </header>
    )
}

export default Header