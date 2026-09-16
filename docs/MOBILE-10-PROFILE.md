# MOBILE-10 — Perfil e sessão

## Entrega

- A tela de perfil apresenta nome, e-mail, iniciais, papel e loja da sessão autenticada.
- A identidade vem de `FirebaseAuth`; papel e loja são exibidos a partir da sessão criada pelas claims.
- O fragmento observa um `ProfileViewModel` com estados de carregamento, sessão incompleta e saída.
- O logout exige confirmação, limpa a sessão local e remove a pilha de navegação antes de abrir o login.

## Limites de contrato atuais

- Não há contrato de upload/atualização de avatar confirmado para Firebase Storage ou API. Por isso, a tela usa iniciais e não simula um upload concluído.
- A loja é exibida pelo identificador recebido na sessão. O nome comercial da empresa deve ser resolvido pela API relacional quando o endpoint estiver disponível.
- A autorização permanece baseada nas claims do Firebase e nas validações das camadas de operação; os dados mostrados nesta tela não concedem permissões.

## Dependência de notificações

Quando a infraestrutura de notificações da MOBILE-09 estiver integrada à `main`, o logout deve também desvincular/limpar o token FCM conforme o endpoint oficial de dispositivos. Essa chamada não foi adicionada aqui porque a API de sincronização do token ainda não possui contrato confirmado.
