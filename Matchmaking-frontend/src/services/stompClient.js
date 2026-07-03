import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

const WS_URL = 'http://localhost:8080/ws'

export function createChatStompClient({
  token,
  conversationId,
  onMessage,
  onConnect,
  onError,
}) {
  const client = new Client({
    webSocketFactory: () => new SockJS(WS_URL),

    connectHeaders: {
      Authorization: `Bearer ${token}`,
    },

    reconnectDelay: 5000,

    onConnect: () => {
      console.log('STOMP connected')

      client.subscribe(
        `/topic/conversations/${conversationId}`,
        (frame) => {
          const message = JSON.parse(frame.body)
          onMessage(message)
        }
      )

      onConnect?.()
    },

    onStompError: (frame) => {
      console.error('STOMP error:', frame)
      onError?.(frame)
    },

    onWebSocketError: (error) => {
      console.error('WebSocket error:', error)
      onError?.(error)
    },
  })

  return client
}