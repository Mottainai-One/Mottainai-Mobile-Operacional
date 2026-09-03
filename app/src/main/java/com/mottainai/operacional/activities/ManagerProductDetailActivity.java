package com.mottainai.operacional.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import com.mottainai.operacional.R;

public class ManagerProductDetailActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_product_detail);

        setupBottomNav(Journey.GERENTE, R.id.productsListFragment);

        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        Button btnEdit = findViewById(R.id.btn_edit_product);
        btnEdit.setOnClickListener(v -> startActivity(new Intent(this, EditProductActivity.class)));

        Button btnDamage = findViewById(R.id.btn_damage);
        btnDamage.setOnClickListener(v -> startActivity(new Intent(this, RegisterDamageActivity.class)));
    }
}
