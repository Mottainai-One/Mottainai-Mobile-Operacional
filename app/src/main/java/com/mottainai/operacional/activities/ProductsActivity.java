package com.mottainai.operacional.activities;

import android.content.Intent;
import android.os.Bundle;
import com.mottainai.operacional.R;

public class ProductsActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);

        setupBottomNav(Journey.ESTOQUISTA, R.id.productsListFragment);

        findViewById(R.id.fab_add).setOnClickListener(v ->
                startActivity(new Intent(this, ScannerActivity.class)));

        // Tab de Inventário
        findViewById(R.id.tv_inventory_tab).setOnClickListener(v ->
                startActivity(new Intent(this, InventoryActivity.class)));
    }
}
