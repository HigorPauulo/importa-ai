import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import LoginPage from './features/auth/pages/LoginPage'
import RegisterPage from './features/auth/pages/RegisterPage'
import DashboardPage from './features/pedidos/pages/DashboardPage'
import CadastrarEncomendas from './features/pedidos/pages/CadastrarEncomendasPage'

function App() {
  return (
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Navigate to="/login" />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/cadastrar-encomendas" element={<CadastrarEncomendas />} />
        </Routes>
      </BrowserRouter>
  )
}

export default App
