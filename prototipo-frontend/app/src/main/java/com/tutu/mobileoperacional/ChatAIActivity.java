package com.tutu.mobileoperacional;

import android.os.Bundle;

public class ChatAIActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_ai);

        setupBottomNav(Journey.ESTOQUISTA, R.id.nav_ai);
    }
}
