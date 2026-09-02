package com.tutu.mobileoperacional;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText etEmail = findViewById(R.id.et_email);
        Button btnLogin = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().toLowerCase();

            if (email.contains("pedro")) {
                startActivity(new Intent(this, OwnerHomeActivity.class));
            } else if (email.contains("carlos")) {
                startActivity(new Intent(this, ManagerHomeActivity.class));
            } else {
                startActivity(new Intent(this, HomeActivity.class));
            }
            finish();
        });
    }
}
