package com.tutu.mobileoperacional;

import android.content.Intent;
import android.os.Bundle;

public class SuppliersActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suppliers);

        setupBottomNav(Journey.GERENTE, R.id.nav_products);

        findViewById(R.id.btn_new_supplier).setOnClickListener(v ->
                startActivity(new Intent(this, NewSupplierActivity.class)));

        // Simulacao de clique em um fornecedor para editar
        findViewById(R.id.cv_supplier_1).setOnClickListener(v ->
                startActivity(new Intent(this, EditSupplierActivity.class)));
    }
}
