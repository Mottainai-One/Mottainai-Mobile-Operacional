package com.mottainai.operacional.utils;

public class Constants {

    // Duração do resultado e feedback tátil/sonoro do scanner.
    public static final long SCANNER_RESULT_DISMISS_MS = 10_000L;
    public static final int SCANNER_FEEDBACK_MS = 100;
    public static final int SCANNER_TONE_VOLUME = 100;

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

    public static final boolean USE_MOCK_REPOSITORY = false;

    public static final boolean USE_MOCK_INVENTORY = false;
}
