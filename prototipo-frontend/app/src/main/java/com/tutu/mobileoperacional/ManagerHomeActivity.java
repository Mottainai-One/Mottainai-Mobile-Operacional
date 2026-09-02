package com.tutu.mobileoperacional;

import android.content.Intent;
import android.os.Bundle;

public class ManagerHomeActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_home);

        setupBottomNav(Journey.GERENTE, R.id.nav_home);

        // Ligar botoes internos da Home
        findViewById(R.id.card_vencendo).setOnClickListener(v -> startActivity(new Intent(this, ManagerIntelligenceActivity.class)));
        findViewById(R.id.card_pendente).setOnClickListener(v -> startActivity(new Intent(this, ManagerIntelligenceActivity.class)));
        findViewById(R.id.card_risco_perda).setOnClickListener(v -> startActivity(new Intent(this, ManagerIntelligenceActivity.class)));
        findViewById(R.id.cv_suggestion_promo).setOnClickListener(v -> startActivity(new Intent(this, ApproveSuggestionActivity.class)));
        findViewById(R.id.cv_doacao).setOnClickListener(v -> startActivity(new Intent(this, ManagerIntelligenceActivity.class)));
        findViewById(R.id.btn_ver_sugestoes).setOnClickListener(v -> startActivity(new Intent(this, ManagerIntelligenceActivity.class)));
        findViewById(R.id.fab_scan).setOnClickListener(v -> startActivity(new Intent(this, ScannerActivity.class)));
    }
}
