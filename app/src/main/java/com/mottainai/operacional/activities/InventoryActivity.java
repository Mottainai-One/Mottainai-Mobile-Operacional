package com.mottainai.operacional.activities;

import android.os.Bundle;
import com.mottainai.operacional.R;

public class InventoryActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        setupBottomNav(Journey.ESTOQUISTA, R.id.productsListFragment);
    }
}
