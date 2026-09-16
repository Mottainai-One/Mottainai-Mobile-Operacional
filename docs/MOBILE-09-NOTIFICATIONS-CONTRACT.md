# MOBILE-09 — contrato pendente de notificações

## O que o app já faz

- recebe e renova o token FCM;
- mantém o token localmente marcado como pendente;
- cria canais Android para alertas, sugestões, inventário e atualizações gerais;
- trata a permissão `POST_NOTIFICATIONS` no Android 13 ou superior;
- apresenta mensagens FCM sem duplicidade e encaminha somente destinos autorizados pelo papel do usuário;
- limpa o token local e o histórico de deduplicação no logout.

O app **não envia** o token a nenhuma API enquanto não existir um contrato oficial. Portanto, não há confirmação de sincronização remota simulada.

## Estado da API NoSQL analisada

O repositório `Mottainai-Api-NoSQL`, no branch `main` analisado em 2026-09-08, é uma API MongoDB voltada a conversas, RAG, métricas, execuções e memórias da IA. Ele não possui Redis, FCM, autenticação Firebase nem endpoint de token de dispositivo. Por isso, não é seguro acrescentar uma chamada móvel a ele.

## Contrato a implementar no backend responsável por notificações

### Registro idempotente do token

`POST /api/v1/devices/tokens`

Autenticação: `Authorization: Bearer <Firebase ID token>`.

Corpo mínimo:

```json
{
  "token": "token-fcm-do-dispositivo",
  "platform": "ANDROID"
}
```

O servidor deve obter usuário, empresa e papel a partir do token Firebase; o aplicativo não envia `empresaId` ou `storeId` livremente. A operação deve ser idempotente para o mesmo token e atualizar `lastSeenAt`.

### Desvinculação no logout

`DELETE /api/v1/devices/tokens/current`

Autenticação: o mesmo Firebase ID token. Se a chamada falhar por estar offline, a limpeza local do app continua; o servidor deve expirar tokens inativos.

### Payload FCM de dados

```json
{
  "notificationId": "id-imutavel-para-deduplicacao",
  "notificationType": "alert|suggestion|inventory|product|damage|home",
  "entityId": "id-opcional-da-entidade",
  "title": "Texto seguro para tela bloqueada",
  "body": "Texto seguro para tela bloqueada"
}
```

O backend deve validar empresa e autorização antes de enviar a mensagem. Credenciais Firebase Admin e qualquer chave de envio ficam exclusivamente no backend, nunca no APK.
