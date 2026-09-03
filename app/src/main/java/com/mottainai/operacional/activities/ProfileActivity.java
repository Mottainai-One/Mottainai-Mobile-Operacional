package com.mottainai.operacional.activities;

import android.content.Intent;
import android.os.Bundle;
import com.mottainai.operacional.R;

public class ProfileActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        setupBottomNav(Journey.ESTOQUISTA, R.id.profileFragment);

        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
        });
    }
}
