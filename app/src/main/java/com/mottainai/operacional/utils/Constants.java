package com.mottainai.operacional.utils;

public class Constants {

    // Perfis
    public static final String ROLE_ESTOQUISTA = "estoquista";
    public static final String ROLE_GERENTE = "gerente";
    public static final String ROLE_DONO = "dono";

    // Severidades de alerta
    public static final String SEVERIDADE_CRITICO = "CRITICO";
    public static final String SEVERIDADE_ATENCAO = "ATENCAO";
    public static final String SEVERIDADE_MONITOR = "MONITOR";

    // Status de sugestão
    public static final String SUGESTAO_PENDENTE = "pending";
    public static final String SUGESTAO_APROVADA = "approved";
    public static final String SUGESTAO_RECUSADA = "rejected";

    // Modo mock do repositório de produtos
    // true = usa MockProductRepository (desenvolvimento sem backend)
    // false = usa ProductRepository (API real via Retrofit)
    // TODO: alterar para false quando API Spring estiver pronta
    public static final boolean USE_MOCK_REPOSITORY = true;

    // Mock para avarias (MOBILE-06) até POST /api/v1/damages existir
    public static final boolean USE_MOCK_DAMAGE = true;
}
