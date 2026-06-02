import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { PrivateRoute } from '@/components/PrivateRoute'
import { AppLayout } from '@/components/layout/AppLayout'
import { navCliente, navAdmin } from '@/components/layout/navConfig'

import LoginPage from '@/features/auth/pages/LoginPage'
import RegisterPage from '@/features/auth/pages/RegisterPage'

import DashboardPage from '@/features/pedidos/pages/DashboardPage'
import PedidosPage from '@/features/pedidos/pages/PedidosPage'
import DetalhesPedidoPage from '@/features/pedidos/pages/DetalhesPedidoPage'
import CadastrarEncomendas from '@/features/pedidos/pages/CadastrarEncomendasPage'
import CotacaoPage from '@/features/cotacao/pages/CotacaoPage'
import NotificacoesPage from '@/features/notificacoes/pages/NotificacoesPage'

import AdminPainelPage from '@/features/admin/pages/AdminPainelPage'
import AdminPedidosPage from '@/features/admin/pages/AdminPedidosPage'
import AdminDetalhePedidoPage from '@/features/admin/pages/AdminDetalhePedidoPage'
import AdminUsuariosPage from '@/features/admin/pages/AdminUsuariosPage'
import AdminCotacaoManualPage from '@/features/admin/pages/AdminCotacaoManualPage'
import AdminExportarPage from '@/features/admin/pages/AdminExportarPage'

function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<Navigate to="/login" replace />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />

                <Route element={<PrivateRoute><AppLayout nav={navCliente} /></PrivateRoute>}>
                    <Route path="/dashboard" element={<DashboardPage />} />
                    <Route path="/pedidos" element={<PedidosPage />} />
                    <Route path="/pedidos/:id" element={<DetalhesPedidoPage />} />
                    <Route path="/cadastrar-encomendas" element={<CadastrarEncomendas />} />
                    <Route path="/cotacao" element={<CotacaoPage />} />
                    <Route path="/notificacoes" element={<NotificacoesPage />} />
                </Route>

                <Route element={<PrivateRoute adminOnly><AppLayout nav={navAdmin} /></PrivateRoute>}>
                    <Route path="/admin" element={<AdminPainelPage />} />
                    <Route path="/admin/pedidos" element={<AdminPedidosPage />} />
                    <Route path="/admin/pedidos/:id" element={<AdminDetalhePedidoPage />} />
                    <Route path="/admin/usuarios" element={<AdminUsuariosPage />} />
                    <Route path="/admin/cotacao" element={<AdminCotacaoManualPage />} />
                    <Route path="/admin/exportar" element={<AdminExportarPage />} />
                </Route>

                <Route path="*" element={<Navigate to="/login" replace />} />
            </Routes>
        </BrowserRouter>
    )
}

export default App
