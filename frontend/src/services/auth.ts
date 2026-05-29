import { api } from './api'

export interface LoginResponse {
    accessToken: string
    refreshToken: string
}

export async function login(email: string, senha: string): Promise<LoginResponse> {
    const { data } = await api.post<LoginResponse>('/auth/login', { email, senha })
    return data
}

export async function register(nome: string, email: string, senha: string): Promise<void> {
    // backend responde 201 sem corpo
    await api.post('/auth/register', { nome, email, senha })
}

export async function logout(refreshToken: string): Promise<void> {
    await api.post('/auth/logout', { refreshToken })
}
