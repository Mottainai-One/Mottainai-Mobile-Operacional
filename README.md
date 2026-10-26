# Mottainai Mobile Operacional

Aplicativo Android em Java para apoiar operações de loja, com telas de produtos, scanner, avarias, inventário, fornecedores, alertas, sugestões e chat com IA.

## Estado das integrações

O aplicativo contém clientes Retrofit para produtos, autenticação, alertas, sugestões e chat. A configuração atual também mantém repositórios mock ativados para alguns fluxos: USE_MOCK_REPOSITORY, USE_MOCK_DAMAGE e USE_MOCK_INVENTORY estão definidos como true em Constants.java. Fornecedores e inventário também usam repositórios mock. Portanto, a presença de uma tela ou interface HTTP não significa que o fluxo esteja conectado ao serviço de produção.

O projeto não inclui uma definição de backend que confirme todos os endpoints anteriormente listados. Consulte app/src/main/java/com/mottainai/operacional/network/ApiService.java e confirme o contrato no serviço correspondente antes de integrar um ambiente real.

A documentação funcional do [Mottainai-Hub](https://github.com/Mottainai-One/Mottainai-Hub/blob/main/Docs/mobile/MOTTAINAI-OPERACIONAL-FUNCIONAL-E-TECNICO.md) registra uma branch local de integração, não uma auditoria da branch main deste repositório. Use-a como contexto de arquitetura e confira a implementação da branch que será compilada.

## Tecnologias

- Android nativo, Java e layouts XML com View Binding.
- Android Gradle Plugin 9.0.1, compile/target SDK 36 e minSdk 26.
- Retrofit 2 e OkHttp para chamadas HTTP.
- Firebase Authentication, Firestore, Storage e Cloud Messaging.
- CameraX e ZXing para leitura de códigos de barras.
- AndroidX Navigation, Lifecycle, RecyclerView e ViewModel.

## Executar localmente

1. Abra o projeto no Android Studio e sincronize o Gradle.
2. Use JDK compatível com o Android Gradle Plugin configurado.
3. Para Firebase, forneça um google-services.json válido para o projeto de desenvolvimento.
4. Para apontar a API do build debug para serviços locais, crie local.properties na raiz:

    apiBaseUrl=http://10.0.2.2:8080/
    iaBaseUrl=http://10.0.2.2:8000/

O endereço 10.0.2.2 permite que o emulador Android acesse o loopback da máquina host. Os valores também podem ser passados como propriedades Gradle. O build release usa endpoints HTTPS configuráveis por releaseApiBaseUrl e releaseIaBaseUrl; não publique endereços ou credenciais de desenvolvimento.

Compile no Windows:

    .\gradlew.bat assembleDebug

## Testes

    .\gradlew.bat testDebugUnitTest

Para testes instrumentados, conecte um dispositivo ou emulador e execute .\gradlew.bat connectedDebugAndroidTest.

## Estrutura

- app/src/main/java/com/mottainai/operacional/activities e fragments: telas e fluxos.
- viewmodels e models: estado de apresentação e dados.
- repository: acesso a APIs e implementações mock.
- network: serviços Retrofit para API operacional, IA e Open Food Facts.
- notifications e scanner: notificações e leitura de códigos.

## Documentação relacionada

- [Contrato da API operacional](https://github.com/Mottainai-One/Mottainai-Api-SQL-Operacional)
- [Mottainai IA](https://github.com/Mottainai-One/Mottainai-IA)
