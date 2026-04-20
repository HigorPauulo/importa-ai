import { BrowserRouter, Routes, Route } from 'react-router-dom'
import LoginPage from './features/auth/pages/LoginPage'
import RegisterPage from './features/auth/pages/RegisterPage'
import DashboardPage from './features/pedidos/pages/DashboardPage'

function App() {
  return (
    <>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/dashboard" element={<DashboardPage />} />
        </Routes>
      </BrowserRouter>
    </>
  )
}

export default App
