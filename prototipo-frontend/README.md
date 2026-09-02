# Protótipo de telas — Mottainai Operacional

Protótipo navegável das telas do app operacional. **Não é o app de produção** — o app
real fica em [`../app`](../app), no pacote `com.mottainai.operacional`, com Retrofit,
Firebase e arquitetura de Fragments.

Aqui as telas são estáticas: servem para validar fluxo, layout e navegação antes de
ligar os dados.

## Como abrir

Esta pasta é um projeto Gradle **independente**. O `settings.gradle.kts` da raiz do
repositório não a inclui, então ela não interfere no build do app de produção.

Abra `prototipo-frontend/` como projeto separado no Android Studio (File → Open →
selecione esta pasta), ou pela linha de comando:

```bash
cd prototipo-frontend
./gradlew assembleDebug
```

O pacote é `com.tutu.mobileoperacional` de propósito: como o `applicationId` é
diferente do app de produção, dá para instalar os dois no mesmo aparelho ao mesmo
tempo e comparar lado a lado.

## Estrutura

- 27 Activities (uma por tela), todas herdando de `BaseActivity`
- 32 layouts em `app/src/main/res/layout/`, sendo 27 com a barra inferior
- 3 jornadas: Estoquista, Gerente e Dono — declaradas no enum `BaseActivity.Journey`

Login de teste (`LoginActivity`): o e-mail define a jornada — qualquer coisa com
`pedro` entra como Dono, `carlos` como Gerente, e o resto como Estoquista.

## Barra inferior e responsividade

O `targetSdk` é 36, e nessa versão o Android desenha o app de ponta a ponta sem opção
de desligar. Sem tratar os *insets*, a barra inferior fica embaixo da barra de gestos
e o cabeçalho embaixo da barra de status.

`BaseActivity` resolve isso num lugar só:

- **Insets** — topo e laterais vão para a raiz; o rodapé vira *padding da própria
  barra*, então o fundo branco continua desenhado atrás da barra de gestos em vez de
  sobrar uma faixa vazia. O teclado também é tratado.
- **Navegação** — `setupBottomNav(Journey, abaAtual)` liga a barra nas 25 telas que a
  exibem (todas menos o Login e a `MainActivity` de redirecionamento) e usa
  `SINGLE_TOP | CLEAR_TOP` para não empilhar telas repetidas.

Do lado dos recursos:

- A barra é `wrap_content` + `minHeight`, então cresce com a fonte do sistema e com o
  inset do aparelho, em vez de cortar ícone e rótulo.
- O FAB central é ancorado na borda superior da barra — acompanha qualquer altura.
- Espaçamentos vêm de `@dimen/screen_padding`, com variantes em `values-land` e
  `values-sw600dp` (tablet).
- Botões e campos usam `wrap_content` + `minHeight` no lugar de altura fixa em `dp`,
  para o texto quebrar em vez de ser cortado com fonte grande.
- Aparência da barra e do FAB centralizada em `values/styles.xml`
  (`Widget.App.BottomNav` e `Widget.App.Fab`).

Validado no emulador (Android 16, navegação por gestos) em retrato, paisagem e com a
fonte do sistema a 150%. `assembleDebug` e `lintDebug` passam com 0 erros.
