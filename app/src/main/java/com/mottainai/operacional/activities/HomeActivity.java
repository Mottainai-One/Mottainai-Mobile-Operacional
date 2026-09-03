package com.mottainai.operacional.activities;

import android.content.Intent;
import android.os.Bundle;
import com.mottainai.operacional.R;

public class HomeActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        setupBottomNav(Journey.ESTOQUISTA, R.id.homeFragment);

        findViewById(R.id.card_vencendo).setOnClickListener(v -> startActivity(new Intent(this, IntelligenceActivity.class)));
        findViewById(R.id.card_estoque_baixo).setOnClickListener(v -> startActivity(new Intent(this, IntelligenceActivity.class)));
        findViewById(R.id.btn_abrir_camera).setOnClickListener(v -> startActivity(new Intent(this, ScannerActivity.class)));
        findViewById(R.id.fab_add).setOnClickListener(v -> startActivity(new Intent(this, ScannerActivity.class)));
    }
}
