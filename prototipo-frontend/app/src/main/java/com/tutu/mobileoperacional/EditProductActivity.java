package com.tutu.mobileoperacional;

import android.os.Bundle;
import android.widget.ImageButton;

public class EditProductActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        setupBottomNav(Journey.GERENTE, R.id.nav_products);

        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
    }
}
