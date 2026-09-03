package com.mottainai.operacional.activities;

import android.os.Bundle;
import android.widget.ImageButton;
import com.mottainai.operacional.R;

public class InviteMemberActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_member);

        setupBottomNav(Journey.DONO, R.id.configFragment);

        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
    }
}
