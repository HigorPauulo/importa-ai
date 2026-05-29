import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios'

const ACCESS_KEY = 'accessToken'
const REFRESH_KEY = 'refreshToken'

export const api = axios.create({ baseURL: '/api' })

api.interceptors.request.use((config) => {
    const token = localStorage.getItem(ACCESS_KEY)
    if (token) config.headers.Authorization = `Bearer ${token}`
    return config
})

let isRefreshing = false
let fila: Array<(token: string | null) => void> = []

const resolverFila = (token: string | null) => {
    fila.forEach((cb) => cb(token))
    fila = []
}

const limparSessao = () => {
    localStorage.removeItem(ACCESS_KEY)
    localStorage.removeItem(REFRESH_KEY)
    window.location.href = '/login'
}

api.interceptors.response.use(
    (response) => response,
    async (error: AxiosError) => {
        const original = error.config as InternalAxiosRequestConfig & { _retry?: boolean }

        // 401 em /auth/login ou /auth/logout é credencial recusada, não sessão
        // expirada — deixa o erro subir pro componente tratar (ex.: LoginForm).
        const ehRotaDeAuth = original.url?.includes('/auth/')

        if (error.response?.status !== 401 || original._retry || ehRotaDeAuth) {
            return Promise.reject(error)
        }

        const refreshToken = localStorage.getItem(REFRESH_KEY)
        if (!refreshToken) {
            limparSessao()
            return Promise.reject(error)
        }

        original._retry = true

        if (isRefreshing) {
            return new Promise((resolve, reject) => {
                fila.push((token) => {
                    if (!token) return reject(error)
                    original.headers.Authorization = `Bearer ${token}`
                    resolve(api(original))
                })
            })
        }

        isRefreshing = true
        try {
            const { data } = await axios.post('/api/auth/refresh', { refreshToken })
            localStorage.setItem(ACCESS_KEY, data.accessToken)
            resolverFila(data.accessToken)
            original.headers.Authorization = `Bearer ${data.accessToken}`
            return api(original)
        } catch (refreshError) {
            resolverFila(null)
            limparSessao()
            return Promise.reject(refreshError)
        } finally {
            isRefreshing = false
        }
    },
)
