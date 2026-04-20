import logo from '../assets/logo.png'

function Header() {
    return (
        <header className="flex justify-center items-center mb-10">
            <img src={logo} alt="Importa Aí" className="h-45" />
        </header>
    )
}

export default Header