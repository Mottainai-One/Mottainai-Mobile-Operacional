package com.tutu.mobileoperacional;

import android.content.Intent;
import android.os.Bundle;

public class ProductsActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);

        setupBottomNav(Journey.ESTOQUISTA, R.id.nav_products);

        findViewById(R.id.fab_add).setOnClickListener(v ->
                startActivity(new Intent(this, ScannerActivity.class)));

        // Tab de Inventário
        findViewById(R.id.tv_inventory_tab).setOnClickListener(v ->
                startActivity(new Intent(this, InventoryActivity.class)));
    }
}
