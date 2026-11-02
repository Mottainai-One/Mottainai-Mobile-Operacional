# Validação da integração operacional — 02/11/2026

- Login normal: e-mail/senha no Firebase; token validado pela API operacional; papel, loja e empresa vêm do SQL.
- Sem botões de teste, atividade de diagnóstico ou credenciais compiladas no app.
- Produtos, inventário, lotes, alertas, sugestões, fornecedores, contagens, avarias e resumo de vendas consultaram a API real com HTTP 200 para as contas vinculadas de gerente/estoquista. Acesso de estoquista à equipe/PDV e escrita de produto foram recusados com 403.
- Open Food Facts: consulta e download de imagem reais validados com código 3017620422003; código inexistente retornou status 0. Cartões/detalhes usam somente essa fonte por código de barras, com fila/cache e placeholder quando não há imagem.
- Cloudinary: uploads e downloads reais validados. Perfil e Minha conta usam o mesmo componente, com referência separada pelo UID Firebase neste aparelho. Sincronização da referência entre aparelhos não está implementada.
- A seleção visual de fotos e a troca de conta ainda precisam ser conferidas no APK atualizado; testes de serviço não substituem esse teste no aparelho.
- Build completo, testes e lint são executados pelo CI da PR. A CLI Gradle no Windows encontrou erro de loopback; isso não é resultado de build aprovado.

Configuração local e credenciais ficam fora do Git. Esta PR não altera o banco.
