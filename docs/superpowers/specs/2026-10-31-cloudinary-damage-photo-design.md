# Cloudinary para fotos de avaria

## Escopo confirmado

Enviar ao Cloudinary apenas as fotos selecionadas na tela de registro de avaria. Login e demais telas permanecem fora desta alteração.

## Upload

Reaproveitar o seletor de câmera/galeria atual e enviar a imagem diretamente para a API de imagem Cloudinary usando o cloud `sffp5gbb` e o preset unsigned `mottainai`. Validar MIME JPG/PNG/WebP e tamanho máximo de 8 MB, executar fora da UI e aceitar somente `secure_url` HTTPS antes de registrar a avaria.

A chave de API não é necessária para upload unsigned e não será embutida no app; segredo de API nunca fica no cliente. O preset `mottainai` deve estar configurado como unsigned.

## Verificação

O endpoint/preset respondeu HTTP 200 a um upload de imagem neutra, retornando `secure_url`. O Gradle local falha antes de compilar com `Unable to establish loopback connection`, então o teste do APK no tablet aguarda uma build bem-sucedida pela CI.
