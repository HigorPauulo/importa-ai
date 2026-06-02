import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { isAxiosError } from 'axios'
import { Modal } from '@/components/ui/Modal'
import { Input } from '@/components/ui/Input'
import { Button } from '@/components/ui/Button'
import { useAuth } from '@/context/AuthContext'
import { useToast } from '@/context/ToastContext'
import { atualizarMeuPerfil, type MeuPerfil } from '@/services/perfil'

interface EditarPerfilModalProps {
    aberto: boolean
    onFechar: () => void
    perfil: MeuPerfil
}

interface FormData {
    nome: string
    email: string
    senha?: string
}

export function EditarPerfilModal({ aberto, onFechar, perfil }: EditarPerfilModalProps) {
    const { showToast } = useToast()
    const { logout } = useAuth()
    const navigate = useNavigate()
    const queryClient = useQueryClient()
    const [erro, setErro] = useState<string | null>(null)

    const sair = async () => {
        await logout()
        navigate('/login')
    }
    const { register, handleSubmit, formState: { errors } } = useForm<FormData>({
        values: { nome: perfil.nome, email: perfil.email, senha: '' },
    })

    const { mutate, isPending } = useMutation({
        mutationFn: atualizarMeuPerfil,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['me'] })
            showToast('Perfil atualizado com sucesso.')
            onFechar()
        },
        onError: (e) => {
            if (isAxiosError(e) && e.response?.status === 422) setErro('Este e-mail já está em uso.')
            else setErro('Não foi possível salvar. Tente novamente.')
        },
    })

    const onSubmit = (data: FormData) => {
        setErro(null)
        mutate({ nome: data.nome, email: data.email, senha: data.senha?.trim() ? data.senha : undefined })
    }

    return (
        <Modal aberto={aberto} onFechar={onFechar} titulo="Editar perfil">
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                <Input label="Nome completo" error={errors.nome?.message}
                       {...register('nome', { required: 'Nome é obrigatório' })} />

                <Input label="E-mail" type="email" error={errors.email?.message}
                       {...register('email', { required: 'E-mail é obrigatório' })} />

                <Input label="Nova senha (opcional)" type="password" hint="Deixe em branco para manter a atual"
                       error={errors.senha?.message}
                       {...register('senha', { validate: (v) => !v || v.length >= 8 || 'Mínimo de 8 caracteres' })} />

                {erro && <p className="text-sm text-error">{erro}</p>}

                <div className="flex items-center gap-2 pt-2">
                    <button type="button" onClick={sair} className="text-sm font-bold text-error hover:underline lg:hidden">
                        Sair da conta
                    </button>
                    <div className="ml-auto flex gap-2">
                        <Button type="button" variant="outline" onClick={onFechar}>Cancelar</Button>
                        <Button type="submit" loading={isPending}>Salvar</Button>
                    </div>
                </div>
            </form>
        </Modal>
    )
}
