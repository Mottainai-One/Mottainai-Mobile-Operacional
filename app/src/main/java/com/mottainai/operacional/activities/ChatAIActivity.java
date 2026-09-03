package com.mottainai.operacional.activities;

import android.os.Bundle;
import com.mottainai.operacional.R;

public class ChatAIActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_ai);

        setupBottomNav(Journey.ESTOQUISTA, R.id.iaAlertsFragment);
    }
}
