package com.mottainai.operacional.activities;

import android.os.Bundle;
import android.widget.ImageButton;
import com.mottainai.operacional.R;

public class NewSupplierActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_supplier);

        setupBottomNav(Journey.GERENTE, R.id.productsListFragment);

        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
    }
}
