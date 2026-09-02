package com.tutu.mobileoperacional;

import android.os.Bundle;

public class DamageSuccessActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_damage_success);

        setupBottomNav(Journey.ESTOQUISTA, R.id.nav_products);
    }
}
