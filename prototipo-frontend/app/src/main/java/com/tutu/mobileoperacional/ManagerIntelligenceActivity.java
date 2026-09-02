package com.tutu.mobileoperacional;

import android.content.Intent;
import android.os.Bundle;

public class ManagerIntelligenceActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intelligence_manager);

        setupBottomNav(Journey.GERENTE, R.id.nav_ai);

        // Ligar botoes de acao nas sugestoes
        findViewById(R.id.btn_approve_1).setOnClickListener(v ->
                startActivity(new Intent(this, ApproveSuggestionActivity.class)));

        findViewById(R.id.btn_edit_1).setOnClickListener(v ->
                startActivity(new Intent(this, ApproveSuggestionActivity.class)));

        // Alternar abas (exemplo visual)
        findViewById(R.id.tv_tab_alertas).setOnClickListener(v -> {
            // Em um app real trocaria o fragmento, aqui mantemos a fidelidade visual
        });
    }
}
