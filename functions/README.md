# Ponte de aprovação do PDV

`posApprovalBridge` mantém as decisões no Firestore e entrega cada solicitação pelos dois canais: FCM para o aviso imediato e API Redis para a caixa de notificações do app.

## Configuração

1. Configure os segredos no projeto Firebase:

   ```bash
   firebase functions:secrets:set POS_APPROVAL_API_SECRET
   firebase functions:secrets:set REDIS_NOTIFICATION_API_SECRET
   ```

   `REDIS_NOTIFICATION_API_SECRET` deve ser igual a `NOTIFICATION_PUBLISHER_SECRET` configurado no processo da API Redis. Use um valor aleatório forte e não o coloque no app.

2. Defina `REDIS_NOTIFICATION_API_URL` no arquivo `functions/.env.<project-id>` como a URL HTTPS completa do endpoint `POST /internal/v1/notifications`. Esse arquivo não deve ser versionado. A URL precisa ser acessível pela Cloud Function; a porta Redis e a porta local do Docker não devem ser expostas diretamente. Use um proxy HTTPS com controle de acesso ou uma conexão privada apropriada.

3. Faça o deploy das Functions, das regras e dos índices Firestore:

   ```bash
   firebase deploy --only functions,firestore:rules,firestore:indexes
   ```

O PDV continua configurado com `Firebase__PosApprovalFunctionUrl` e o segredo `POS_APPROVAL_API_SECRET`. O mesmo pedido pode ser reenviado com segurança: a notificação Redis usa um identificador estável por solicitação e gerente.
