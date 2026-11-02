# Sessão mobile operacional

O app autentica e-mail/senha pelo Firebase Authentication e envia Firebase ID Token como Bearer para a API operacional. A API valida assinatura, emissor, audiência e validade; o vínculo SQL ativo define usuário, funcionário, cargo, loja e empresa. O app não autoriza por claims de cargo/loja do Firebase nem consulta Firestore.

`GET /api/v1/auth/mobile-profile` carrega o perfil operacional. A restauração revalida esse perfil antes da navegação. Tokens atuais são obtidos pelo SDK e há uma renovação forçada após 401; logout remove a sessão local e sai do Firebase. Nome/e-mail de exibição podem vir da identidade Firebase, sem definir permissões.

A ponte `POST /api/v1/auth/firebase` fornece JWT operacional para as integrações que exigem esse contrato, como IA e Redis. A senha Firebase não é encaminhada à API SQL. Notificações são consultadas na caixa interna Redis; não há Cloud Messaging no fluxo.

Níveis suportados pelo contrato atual: 100 dono, 80 gerente e 60/40 estoquista. Contas sem vínculo ou com cargo incompatível são recusadas; não há criação automática nem conversão por nome do cargo. A API deve estar configurada e o vínculo no banco deve ser válido.

O login padrão tem somente os campos e-mail/senha e o botão de entrada. Não existem atalhos de diagnóstico ou credenciais de teste compiladas. Configurações locais usam local.properties, ignorado pelo Git.

Contrato da API: https://github.com/Mottainai-One/Mottainai-Api-SQL-Operacional/pull/73
