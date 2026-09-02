package com.tutu.mobileoperacional;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class IntelligenceActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intelligence);

        setupBottomNav(Journey.ESTOQUISTA, R.id.nav_ai);

        findViewById(R.id.fab_add).setOnClickListener(v ->
                startActivity(new Intent(this, ScannerActivity.class)));

        View tabChat = findViewById(R.id.tv_tab_chat);
        if (tabChat != null) {
            tabChat.setOnClickListener(v -> startActivity(new Intent(this, ChatAIActivity.class)));
        }
    }
}
