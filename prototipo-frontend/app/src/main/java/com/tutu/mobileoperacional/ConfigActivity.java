package com.tutu.mobileoperacional;

import android.content.Intent;
import android.os.Bundle;

public class ConfigActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        setupBottomNav(Journey.DONO, R.id.nav_config);

        // Tab switching logic (simplified for prototype)
        findViewById(R.id.tab_team).setOnClickListener(v -> {
            // In a real app, this would swap fragments or visibility
        });

        findViewById(R.id.btn_view_profile).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }
}
