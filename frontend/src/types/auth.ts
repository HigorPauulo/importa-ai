export interface User {
    id: number
    name: string
    email: string
}

export interface LoginFormData {
    email: string
    password: string
}

export interface RegisterFormData {
    name: string
    email: string
    password: string
    confirmPassword: string
    acceptTerms: boolean
}