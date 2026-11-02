<h1 align="center">
  🍱 Mottainai Operacional
</h1>

<p align="center">
  <b>Gestão de estoque anti-desperdício para o time interno da loja</b><br>
  Projeto interdisciplinar · Instituto Germinare · 2026
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java">
  <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android">
  <img src="https://img.shields.io/badge/Retrofit-2-blue?style=for-the-badge&logo=square&logoColor=white" alt="Retrofit 2">
  <img src="https://img.shields.io/badge/Firebase%20Cloud%20Messaging-FFCA28?style=for-the-badge&logo=firebase&logoColor=black" alt="FCM">
</p>

---

## 🌱 O que é o Mottainai?

> *"Mottainai" (もったいない) é uma expressão japonesa que significa "que desperdício!".*

O Mottainai é um ecossistema anti-desperdício de alimentos. Este repositório contém o app **Operacional**, usado pelo time interno das lojas para gerenciar estoque, registrar avarias, fazer inventário e agir contra a perda de produtos — com ajuda de inteligência artificial.

## 🏗️ O ecossistema completo

```
┌─────────────────┐     ┌─────────────────┐
│   🏪 Operacional │     │   🛍️ Cliente    │
│   (este app)     │     │  (app do cliente)│
└────────┬────────┘     └────────┬────────┘
         │            Retrofit            │
         └──────────────┬─────────────────┘
                        ▼
              ┌──────────────────┐
              │    API REST 🌐   │
              │   (PostgreSQL)   │
              └────────┬─────────┘
                       ▼
             ┌──────────────────┐
             │ 🤖 Agentes IA    │
             │    (LangGraph)   │
             └──────────────────┘
```

- **App Operacional** → time interno (estoquista, gerente, dono)
- **App Cliente** → consumidor final
- **API REST** → comunicação com o banco (PostgreSQL)
- **Agentes IA** → motor preditivo, alertas e sugestões

> O app utiliza APIs HTTP e serviços Firebase para autenticação, dados e notificações.

## 👥 Perfis de acesso

| Perfil | Responsabilidades |
|---|---|
| **🧹 Estoquista** | Buscar produtos, escanear código de barras, registrar avarias, inventário, ver alertas e chat com o Agente Funcionário |
| **👔 Gerente** | Tudo do estoquista + cadastro/edição de produtos, fornecedores, aprovar/recusar sugestões da IA, indicadores operacionais |
| **👑 Dono** | Tudo do gerente + regras automáticas, gestão de equipe, visão multi-loja, indicadores financeiros e chat com o Agente Dono |

O perfil é detectado **automaticamente após o login** — não existe tela de seleção.

## 🧩 Funcionalidades

- 📦 **Produtos** — listagem, busca por nome/SKU e cadastro
- 📷 **Scanner** — leitura de código de barras via CameraX + ZXing (escaneamento contínuo em loop)
- 💔 **Avaria** — registro com motivo, quantidade e foto
- 📋 **Inventário** — contagem física vs. sistema, com cálculo automático de divergência
- ⚠️ **Alertas da IA** — níveis CRÍTICO / ATENÇÃO / MONITOR
- 💡 **Sugestões da IA** — análise e decisão pelo gerente
- 🤖 **Chat com IA** — envio de mensagens e histórico de conversas

## 🛠️ Stack técnica

| Camada | Tecnologia |
|---|---|
| Linguagem | Java |
| IDE | Android Studio |
| UI | XML + Navigation Component + Bottom Navigation |
| Rede | Retrofit 2 + OkHttp (interceptor de token JWT) |
| Câmera | CameraX + ZXing |
| Push | Firebase Cloud Messaging |
| Catálogo por código de barras | Open Food Facts API |

## 🗂️ Estrutura de pastas

```
app/src/main/java/com/mottainai/operacional/
├── activities/     → telas que abrem "por cima" (login, formulários, detalhe)
├── fragments/      → telas do bottom nav
├── models/         → classes de dados (Product, User, Damage...)
├── viewmodels/     → lógica separada da tela (ViewModel + LiveData)
├── adapters/       → listas RecyclerView
├── repository/     → chamadas à API REST (Retrofit)
├── network/        → ApiClient + ApiService
└── utils/          → Constants, SessionManager...
```

## 🔌 Integrações e contratos

Os contratos HTTP consumidos pelo aplicativo estão em `network/ApiService.java` e `network/AiApiService.java`. As rotas incluem:

| Área | Chamadas declaradas |
|---|---|
| Acesso e perfil | `POST api/v1/auth/login`, `POST api/v1/auth/refresh`, `GET api/v1/auth/profile` |
| Produtos | `GET api/v1/products`, `GET api/v1/products/{id}`, `GET api/v1/products/barcode/{barcode}`, `POST api/v1/products`, `PUT api/v1/products/{id}` |
| Avarias e estoque | `GET/POST api/v1/damages`, `GET api/v1/inventory`, `GET api/v1/batches` |
| Inventário | `GET/POST api/v1/inventory-counts`, operações de itens e finalização em `api/v1/inventory-counts/{id}` |
| Alertas e sugestões | `GET api/v1/alerts`, `GET api/v1/suggested-actions`, `POST api/v1/suggested-actions/{id}/decision` |
| Indicadores | `GET api/v1/sales/summary` |
| Assistente | `POST chat`, `GET chat/history/{sessionId}` na API da IA |

O app também usa Firebase e integrações para notificações e imagens. Os serviços de destino precisam estar configurados no ambiente; consulte os arquivos de rede, os repositórios e a documentação de cada serviço para os contratos completos.

## 🚀 Como rodar

1. Abra o projeto no **Android Studio** (JDK 17+)
2. Sincronize o Gradle (`File → Sync Project with Gradle Files`)
3. Para usar a API local no emulador, crie/edite `local.properties` (arquivo ignorado pelo Git):
   ```properties
   apiBaseUrl=http://10.0.2.2:8080/
   iaBaseUrl=http://10.0.2.2:8000/
   ```
   Os endpoints HTTP locais só são aceitos no build `debug`; o `release` usa HTTPS e
   pode receber outros endpoints seguros por `-PreleaseApiBaseUrl=https://.../` e
   `-PreleaseIaBaseUrl=https://.../` no CI.
4. Adicione o arquivo `google-services.json` do Firebase (para push)
5. Rode em um emulador ou dispositivo físico

## ✅ Testes e integração contínua

Os testes locais estão em `app/src/test` e os instrumentados em `app/src/androidTest`. O workflow [CI](.github/workflows/ci.yml) descreve as verificações automatizadas. Para desenvolvimento, use as tarefas Gradle apropriadas ao ambiente e ao dispositivo ou emulador disponível.

## 🤝 Time

| Membro | Responsabilidade |
|---|---|
| **Mobile** (eu) | Este app + App Cliente |
| **Backend** | API REST + PostgreSQL + JWT |
| **IA** | Agentes LangGraph + motor preditivo |

---

Feito com 💚 no Instituto Germinare — *contra o desperdício, um produto de cada vez.*
