package com.tutu.mobileoperacional;

import android.os.Bundle;

public class InventoryActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        setupBottomNav(Journey.ESTOQUISTA, R.id.nav_products);
    }
}
