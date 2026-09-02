package com.tutu.mobileoperacional;

import android.os.Bundle;
import android.widget.ImageButton;

public class ApproveSuggestionActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approve_suggestion);

        setupBottomNav(Journey.GERENTE, R.id.nav_ai);

        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
    }
}
