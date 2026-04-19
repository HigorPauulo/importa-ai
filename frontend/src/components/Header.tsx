import logo from '../assets/logo.png'

function Header() {
    return (
        <header className="pb-4 flex justify-center items-center">
            <img src={logo} alt="Importa Aí" className="h-45" />
        </header>
    )
}

export default Header