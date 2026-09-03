package com.mottainai.operacional.activities;

import android.os.Bundle;
import com.mottainai.operacional.R;

public class DamageSuccessActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_damage_success);

        setupBottomNav(Journey.ESTOQUISTA, R.id.productsListFragment);
    }
}
