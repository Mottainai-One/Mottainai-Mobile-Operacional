package com.mottainai.operacional.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.FirebaseUser;
import com.mottainai.operacional.MainActivity;
import com.mottainai.operacional.R;
import com.mottainai.operacional.databinding.ActivityLoginBinding;
import com.mottainai.operacional.utils.AuthErrorTranslator;
import com.mottainai.operacional.utils.RoleHelper;
import com.mottainai.operacional.utils.SessionManager;
import com.mottainai.operacional.utils.ThemeSettings;
import com.mottainai.operacional.utils.ValidationUtils;
import com.mottainai.operacional.models.User;
import com.mottainai.operacional.repository.AuthRepository;
import com.mottainai.operacional.repository.NotificationRepository;

public class LoginActivity extends AppCompatActivity {

    public static final String EXTRA_SIGNED_OUT = "signed_out";

    private ActivityLoginBinding binding;

    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private AuthRepository authRepository;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeSettings.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        etEmail = binding.etEmail;
        etPassword = binding.etPassword;
        btnLogin = binding.btnLogin;
        binding.loginStatus.setVisibility(getIntent().getBooleanExtra(EXTRA_SIGNED_OUT, false)
                ? android.view.View.VISIBLE : android.view.View.GONE);

        session = new SessionManager(this);
        authRepository = new AuthRepository();

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

    private void login() {
        binding.loginStatus.setVisibility(android.view.View.GONE);
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showLoginStatus("Preencha e-mail e senha.");
            Toast.makeText(this, "Preencha email e senha", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            showLoginStatus("Informe um e-mail válido.");
            Toast.makeText(this, "Informe um e-mail válido", Toast.LENGTH_SHORT).show();
            return;
        }
        btnLogin.setEnabled(false);
        btnLogin.setText(R.string.op_logging_in);

        authRepository.login(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(String uid) {
                loadSessionFromToken(uid);
            }

            @Override
            public void onError(Exception e) {
                btnLogin.setEnabled(true);
                btnLogin.setText(R.string.op_login_button);
                showLoginStatus(AuthErrorTranslator.toUserMessage(e));
                Toast.makeText(LoginActivity.this,
                        AuthErrorTranslator.toUserMessage(e), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoginStatus(String message) {
        binding.loginStatus.setText(message);
        binding.loginStatus.setVisibility(android.view.View.VISIBLE);
    }

    private void loadSessionFromToken(String uid) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            resetLoginButton("Sessão Firebase indisponível. Tente entrar novamente.");
            return;
        }

        // The just-authenticated Firebase ID token already carries the server-assigned
        // operational claims. Using it avoids blocking login on a Firestore read.
        firebaseUser.getIdToken(false)
                .addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
                    @Override
                    public void onSuccess(@NonNull GetTokenResult result) {
                        Object storeID = result.getClaims().get("storeID");
                        Object role = result.getClaims().get("role");
                        if (storeID == null || role == null
                                || storeID.toString().trim().isEmpty()
                                || role.toString().trim().isEmpty()) {
                            resetLoginButton("Usuário sem loja ou perfil operacional. Contate o administrador.");
                            return;
                        }

                        String name = firebaseUser.getDisplayName();
                        if (name == null || name.trim().isEmpty()) {
                            String email = firebaseUser.getEmail();
                            name = email == null ? "Usuário" : email.split("@", 2)[0];
                        }
                        User user = new User(uid, name, RoleHelper.normalize(role.toString()), storeID.toString());
                        session.saveSession(user);
                        session.saveToken(result.getToken());
                        new NotificationRepository(getApplicationContext()).captureCurrentDeviceToken();
                        goToMain();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        resetLoginButton("Erro ao validar sessão: " + AuthErrorTranslator.toUserMessage(e));
                    }
                });
    }

    private void resetLoginButton(String message) {
        btnLogin.setEnabled(true);
        btnLogin.setText(R.string.op_login_button);
        showLoginStatus(message);
    }

    private void goToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
