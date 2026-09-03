package com.mottainai.operacional.activities;

import android.content.Intent;
import android.os.Bundle;
import com.mottainai.operacional.R;

public class SuppliersActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suppliers);

        setupBottomNav(Journey.GERENTE, R.id.productsListFragment);

        findViewById(R.id.btn_new_supplier).setOnClickListener(v ->
                startActivity(new Intent(this, NewSupplierActivity.class)));

        // Simulacao de clique em um fornecedor para editar
        findViewById(R.id.cv_supplier_1).setOnClickListener(v ->
                startActivity(new Intent(this, EditSupplierActivity.class)));
    }
}
