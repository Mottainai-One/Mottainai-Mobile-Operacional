package com.tutu.mobileoperacional;

import android.os.Bundle;
import android.widget.ImageButton;

public class EditMemberActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_member);

        setupBottomNav(Journey.DONO, R.id.nav_config);

        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
    }
}
