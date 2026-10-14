package com.mottainai.operacional.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Preferências de notificação/regras salvas localmente no dispositivo.
 * Não existe endpoint de configuração da loja ainda, então isto é o que
 * realmente controla o comportamento do app hoje (gate de push em
 * NotificationRouter); a Tela de Configurações lê/grava por aqui.
 */
public class NotificationPreferences {

    private static final String PREFS_NAME = "mottainai_notification_prefs";
    private static final String KEY_PUSH_ALERTS = "push_alerts";
    private static final String KEY_PUSH_SUGGESTIONS = "push_suggestions";
    private static final String KEY_PUSH_INVENTORY = "push_inventory";
    private static final String KEY_AUTO_PROMO = "auto_promo";
    private static final String KEY_REQUIRE_APPROVAL = "require_approval";

    private final SharedPreferences prefs;

    public NotificationPreferences(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isPushAlertsEnabled() {
        return prefs.getBoolean(KEY_PUSH_ALERTS, true);
    }

    public boolean isPushSuggestionsEnabled() {
        return prefs.getBoolean(KEY_PUSH_SUGGESTIONS, true);
    }

    public boolean isPushInventoryEnabled() {
        return prefs.getBoolean(KEY_PUSH_INVENTORY, false);
    }

    public boolean isAutoPromoEnabled() {
        return prefs.getBoolean(KEY_AUTO_PROMO, true);
    }

    public boolean isApprovalRequired() {
        return prefs.getBoolean(KEY_REQUIRE_APPROVAL, true);
    }

    public void saveNotificationPrefs(boolean pushAlerts, boolean pushSuggestions, boolean pushInventory) {
        prefs.edit()
                .putBoolean(KEY_PUSH_ALERTS, pushAlerts)
                .putBoolean(KEY_PUSH_SUGGESTIONS, pushSuggestions)
                .putBoolean(KEY_PUSH_INVENTORY, pushInventory)
                .apply();
    }

    public void saveRulesPrefs(boolean autoPromo, boolean requireApproval) {
        prefs.edit()
                .putBoolean(KEY_AUTO_PROMO, autoPromo)
                .putBoolean(KEY_REQUIRE_APPROVAL, requireApproval)
                .apply();
    }
}
