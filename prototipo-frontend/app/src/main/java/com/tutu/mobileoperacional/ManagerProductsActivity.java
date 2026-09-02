package com.tutu.mobileoperacional;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class ManagerProductsActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_products);

        setupBottomNav(Journey.GERENTE, R.id.nav_products);

        // Logica de Tabs
        TextView tabSuppliers = findViewById(R.id.tv_tab_suppliers);
        tabSuppliers.setOnClickListener(v -> startActivity(new Intent(this, SuppliersActivity.class)));

        Button btnRegister = findViewById(R.id.btn_register_product);
        btnRegister.setOnClickListener(v -> startActivity(new Intent(this, NewProductActivity.class)));
    }
}
