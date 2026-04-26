import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { PrivateRoute } from '@/components/PrivateRoute'
import LoginPage from '@/features/auth/pages/LoginPage'
import RegisterPage from '@/features/auth/pages/RegisterPage'
import DashboardPage from '@/features/pedidos/pages/DashboardPage'
import CadastrarEncomendas from '@/features/pedidos/pages/CadastrarEncomendasPage'
import CatacaoPage from '@/features/cotacao/pages/CotacaoPage'

function App() {
  return (
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Navigate to="/login" />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/dashboard" element={<PrivateRoute><DashboardPage /></PrivateRoute>} />
          <Route path="/cadastrar-encomendas" element={<PrivateRoute><CadastrarEncomendas /></PrivateRoute  >} />
          <Route path="/cotacao" element={<PrivateRoute><CatacaoPage /></PrivateRoute>} />
        </Routes>
      </BrowserRouter>
  )
}

export default App
