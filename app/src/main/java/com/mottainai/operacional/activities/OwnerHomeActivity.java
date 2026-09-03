package com.mottainai.operacional.activities;

import android.content.Intent;
import android.os.Bundle;
import com.mottainai.operacional.R;

public class OwnerHomeActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_home);

        setupBottomNav(Journey.DONO, R.id.homeFragment);

        // Ligar botoes da Home do Dono
        findViewById(R.id.card_risco_perda).setOnClickListener(v -> startActivity(new Intent(this, ManagerIntelligenceActivity.class)));
        findViewById(R.id.card_salvo).setOnClickListener(v -> startActivity(new Intent(this, ManagerIntelligenceActivity.class)));
        findViewById(R.id.btn_config_geral).setOnClickListener(v -> startActivity(new Intent(this, ConfigActivity.class)));
        findViewById(R.id.fab_add).setOnClickListener(v -> startActivity(new Intent(this, ScannerActivity.class)));
    }
}
