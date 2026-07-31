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
│   (este app)     │     │  (app do lojista)│
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

> 🔒 O app **nunca** acessa o banco diretamente. Tudo passa pela API REST.

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
- 💡 **Sugestões da IA** — aprovadas ou recusadas pelo gerente
- 🤖 **Chat com Agentes IA** — Agente Funcionário (estoque) e Agente Dono (KPIs)

## 🛠️ Stack técnica

| Camada | Tecnologia |
|---|---|
| Linguagem | Java |
| IDE | Android Studio |
| UI | XML + Navigation Component + Bottom Navigation |
| Rede | Retrofit 2 + OkHttp (interceptor de token JWT) |
| Câmera | CameraX + ZXing |
| Push | Firebase Cloud Messaging |
| Localização por CEP | ViaCEP |
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

## 🔌 Endpoints principais

```
POST /auth/login                  → login (retorna token + role + storeId)
GET  /auth/me                     → dados do usuário logado
GET  /products?storeId=           → lista de produtos da loja
POST /products                    → cadastrar produto (Gerente/Dono)
GET  /products/expiring           → produtos próximos do vencimento
POST /damages                     → registrar avaria
GET  /alerts?storeId=             → alertas do Motor Preditivo
GET  /suggestions?storeId=        → sugestões pendentes de aprovação
PUT  /suggestions/{id}/approve    → aprovar sugestão
PUT  /suggestions/{id}/reject     → recusar sugestão
POST /chat                        → chat com agente IA
```

## 🚀 Como rodar

1. Abra o projeto no **Android Studio** (JDK 17+)
2. Synchronize o Gradle (`File → Sync Project with Gradle Files`)
3. Configure a URL da API em `network/ApiClient.java` (ou via `BuildConfig`)
4. Adicione o arquivo `google-services.json` do Firebase (para push)
5. Rode em um emulador ou dispositivo físico

## 🤝 Time

| Membro | Responsabilidade |
|---|---|
| **Mobile** (eu) | Este app + App Cliente |
| **Backend** | API REST + PostgreSQL + JWT |
| **IA** | Agentes LangGraph + motor preditivo |

---

Feito com 💚 no Instituto Germinare — *contra o desperdício, um produto de cada vez.*
