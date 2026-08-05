package com.mottainai.operacional.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.mottainai.operacional.MainActivity;
import com.mottainai.operacional.R;
import com.mottainai.operacional.utils.SessionManager;
import com.mottainai.operacional.models.User;
import com.mottainai.operacional.repository.AuthRepository;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private AuthRepository authRepository;
    private FirebaseFirestore db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);

        db = FirebaseFirestore.getInstance();
        session = new SessionManager(this);
        authRepository = new AuthRepository();

        btnLogin.setOnClickListener(v -> login());
    }

    // Realiza o login na activity
    private void login() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Preencha email e senha", Toast.LENGTH_SHORT).show();
            return;
        }
        btnLogin.setEnabled(false);
        btnLogin.setText("Entrando...");

        authRepository.login(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(String uid) {
                loadUserProfile(uid);
            }

            @Override
            public void onError(Exception e) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Entrar");
                Toast.makeText(LoginActivity.this,
                        "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Carrega o perfil do usuário
    private void loadUserProfile(String uid) {
        db.collection("users").document(uid)
                .get()
                .addOnCompleteListener(this, new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                            DocumentSnapshot doc = task.getResult();

                            String nome = doc.getString("name");
                            String role = doc.getString("role");
                            String storeId = doc.getString("storeId");

                            User user = new User(uid, nome, role, storeId);

                            session.saveSession(user);

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            btnLogin.setEnabled(true);
                            btnLogin.setText("Entrar");
                            Toast.makeText(LoginActivity.this,
                                    "Perfil não encontrado no Firestore",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
