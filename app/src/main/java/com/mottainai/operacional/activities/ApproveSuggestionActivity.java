package com.mottainai.operacional.activities;

import android.os.Bundle;
import android.widget.ImageButton;
import com.mottainai.operacional.R;

public class ApproveSuggestionActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approve_suggestion);

        setupBottomNav(Journey.GERENTE, R.id.iaAlertsFragment);

        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
    }
}
