# Imagens de produtos e foto local de perfil

## Produtos

As imagens da lista e do detalhe são consultadas no Open Food Facts pelo código
de barras real da API operacional. URLs de outras fontes não substituem essa
consulta. Código ausente/inválido ou produto sem foto resulta em placeholder,
sem inventar correspondência por nome ou trocar dados de preço/estoque.

O cliente público é separado do autenticado. Consultas são deduplicadas e
enfileiradas automaticamente a cada 5 segundos, sem abandonar itens após o
limite. Há cache de fotos por 24 horas, de ausência por 1 hora, e cache Glide.
Views recicladas não recebem respostas de outro produto. Pressione a imagem
para conferir a atribuição Open Food Facts, CC BY-SA.

## Perfil — padrão Cloudinary do Cliente

Toque no avatar do Perfil ou de Minha conta para escolher uma imagem pelo
seletor Android. O app envia ao Cloudinary reutilizando PhotoStorageRepository
e só grava a secure_url depois do sucesso. A referência fica nas preferências
privadas, separada pelo UID Firebase. Outra conta não reutiliza essa foto.
O upload valida MIME/tamanho (8 MB), prepara o arquivo fora da UI e bloqueia
duplo envio. Não altera Firebase nem precisa de Firebase Storage.

Referência correta: C:/Users/isabellagimenes-ieg/StudioProjects/Mottainai-Mobile-Cliente.
CloudinaryProfilePhoto.java implementa upload unsigned com referência por UID.
O documento local docs/API-CLIENTE-VALIDACAO-LOCAL.md (31/10/2026) define foto sem
dependência da API Cliente/SQL. Há código local de avatarUrl no perfil SQL,
mas a decisão documentada não exige essa coluna/rota. Esta adaptação segue a
separação Cloudinary + referência local, sem inventar endpoint operacional.
Ela não garante restauração da URL em outro aparelho após nova instalação.
Não há API secret no app: usa cloud name e preset existentes do Operacional.

## Teste manual

1. Abra produto com código real e sem imageUrl; confira lista e detalhe.
2. Confira produto com imageUrl, código inexistente, SKU alfanumérico e sem rede.
3. Role rapidamente: imagem atrasada não pode aparecer em outro produto.
4. Escolha foto na conta A, reabra o app e confira a persistência.
5. Entre na conta B: não deve exibir foto da A; escolha uma foto distinta.
6. Cancele o seletor e teste arquivo que foi removido: não deve travar.

Essas mudanças não alteram as URLs das APIs Mottainai ou as conexões do banco.
