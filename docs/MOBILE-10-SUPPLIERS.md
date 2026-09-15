# MOBILE-10 — Fornecedores

## Estado atual

O aplicativo mantém fornecedores localmente, isolados por loja, enquanto o consumo da API relacional não estiver habilitado. A lista inicial contém dois fornecedores demonstrativos; criar, editar e remover são persistidos em `SharedPreferences` neste dispositivo.

## Regras no mobile

- Gerente e Dono podem criar, editar e remover.
- Estoquista pode visualizar a lista, sem acesso às ações de manutenção.
- Nome da empresa e CNPJ com 14 dígitos são obrigatórios.
- Contato é opcional no modo local.

## Contrato já existente na API relacional

`/api/v1/suppliers` expõe `GET`, `POST`, `GET /{id}`, `PUT /{id}` e `DELETE /{id}`.

O cadastro remoto requer `addressId`, `tradeName`, `cnpj`, `email` e `phone`. A tela operacional atual ainda não coleta nem gerencia um endereço remoto; por isso o repositório local não envia dados parciais para a API. A migração deve substituir `MockSupplierRepository` por um repositório Retrofit quando o fluxo de endereço estiver definido.
