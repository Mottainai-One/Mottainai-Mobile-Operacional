package com.mottainai.operacional.activities;

import android.content.Intent;
import android.os.Bundle;
import com.mottainai.operacional.R;

public class ScannerActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        setupBottomNav(Journey.ESTOQUISTA, R.id.productsListFragment);

        findViewById(R.id.btn_register_damage).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterDamageActivity.class)));

        findViewById(R.id.btn_update_quantity).setOnClickListener(v -> finish());

        findViewById(R.id.btn_not_this_product).setOnClickListener(v ->
                startActivity(new Intent(this, ScannerNotFoundActivity.class)));
    }
}
