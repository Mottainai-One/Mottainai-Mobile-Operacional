package com.mottainai.operacional.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import com.mottainai.operacional.R;

public class RegisterDamageActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_damage);

        setupBottomNav(Journey.ESTOQUISTA, R.id.productsListFragment);

        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        Button btnSubmit = findViewById(R.id.btn_submit_damage);
        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v ->
                    startActivity(new Intent(this, DamageSuccessActivity.class)));
        }
    }
}
