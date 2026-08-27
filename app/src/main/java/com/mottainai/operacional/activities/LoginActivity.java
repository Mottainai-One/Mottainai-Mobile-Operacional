package com.mottainai.operacional.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.mottainai.operacional.MainActivity;
import com.mottainai.operacional.R;
import com.mottainai.operacional.utils.SessionManager;
import com.mottainai.operacional.models.User;
import com.mottainai.operacional.repository.AuthRepository;
import com.mottainai.operacional.repository.UserRepository;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private AuthRepository authRepository;
    private UserRepository userRepository;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);

        session = new SessionManager(this);
        authRepository = new AuthRepository();
        userRepository = new UserRepository();

        // Verifica se o usuário já está logado COM SESSÃO COMPLETA, se estiver leva para home
        if (session.isLoggedIn() 
                && session.getStoreId() != null && !session.getStoreId().isEmpty()
                && session.getRole() != null && !session.getRole().isEmpty()) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

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
        userRepository.getUserProfile(uid, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                if (user.getStoreId() == null || user.getStoreId().isEmpty()
                        || user.getRole() == null || user.getRole().isEmpty()) {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Entrar");
                    Toast.makeText(LoginActivity.this,
                            "Usuário sem loja ou perfil configurado. Contate o administrador.",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                refreshTokenAndProceed(user);
            }

            @Override
            public void onError(Exception e) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Entrar");
                Toast.makeText(LoginActivity.this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNotFound() {
                btnLogin.setEnabled(true);
                btnLogin.setText("Entrar");
                Toast.makeText(LoginActivity.this, "Perfil não encontrado no Firestore", Toast.LENGTH_LONG).show();
            }
        });
    }

    // Renova o ID token para que as custom claims (storeID, role) entrem no token
    // antes do Firestore validar as security rules. Só abre a MainActivity após o
    // refresh terminar. Se falhar, mostra erro e mantém o usuário no login.
    private void refreshTokenAndProceed(User user) {
        FirebaseAuth.getInstance().getCurrentUser()
                .getIdToken(true)
                .addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
                    @Override
                    public void onSuccess(@NonNull GetTokenResult result) {
                        // DEBUG temporário — inspecionar custom claims do token
                        Object storeID = result.getClaims().get("storeID");
                        Object role = result.getClaims().get("role");
                        Log.d("AUTH_CLAIMS", "storeID=" + storeID + ", role=" + role);

                        if (storeID == null || role == null) {
                            btnLogin.setEnabled(true);
                            btnLogin.setText("Entrar");
                            Toast.makeText(LoginActivity.this,
                                    "Token sem storeID/role. Logout e login novamente.",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        // Só salva a sessão após a renovação do token com as claims confirmadas
                        session.saveSession(user);
                        // Salva o Firebase ID token para usar como Authorization: Bearer nas chamadas de API
                        session.saveToken(result.getToken());
                        goToMain();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("AUTH_CLAIMS", "Falha ao renovar token", e);
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Entrar");
                        Toast.makeText(LoginActivity.this,
                                "Erro ao renovar sessão: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void goToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
