import { Client, type IMessage } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import type { Notificacao } from '@/types/notificacao'

const ACCESS_KEY = 'accessToken'

// Cria um cliente STOMP sobre SockJS apontando pro endpoint /ws do backend.
// O JWT vai no header Authorization do frame CONNECT — é o que o
// StompAuthChannelInterceptor do backend lê pra estabelecer o Principal e
// rotear /user/queue/notificacoes pro usuário certo.
export function criarClienteStomp(onNotificacao: (n: Notificacao) => void): Client {
    const client = new Client({
        // SockJS usa http(s), não ws(s); o proxy do Vite encaminha /ws pro backend
        webSocketFactory: () => new SockJS('/ws'),
        connectHeaders: {
            Authorization: `Bearer ${localStorage.getItem(ACCESS_KEY) ?? ''}`,
        },
        reconnectDelay: 5000, // tenta reconectar a cada 5s se a conexão cair
        onConnect: () => {
            client.subscribe('/user/queue/notificacoes', (message: IMessage) => {
                onNotificacao(JSON.parse(message.body) as Notificacao)
            })
        },
    })

    return client
}
