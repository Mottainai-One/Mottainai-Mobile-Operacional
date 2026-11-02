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
import com.mottainai.operacional.MainActivity;
import com.mottainai.operacional.R;
import com.mottainai.operacional.databinding.ActivityLoginBinding;
import com.mottainai.operacional.utils.AuthErrorTranslator;
import com.mottainai.operacional.utils.SessionManager;
import com.mottainai.operacional.utils.ThemeSettings;
import com.mottainai.operacional.utils.ValidationUtils;
import com.mottainai.operacional.utils.OperationalSessionUser;
import com.mottainai.operacional.utils.RoleHelper;
import com.mottainai.operacional.models.User;
import com.mottainai.operacional.repository.AuthRepository;
import com.mottainai.operacional.repository.NotificationRepository;
import com.google.firebase.auth.FirebaseUser;
import com.mottainai.operacional.network.ApiService;
import com.mottainai.operacional.network.RetrofitClient;
import com.mottainai.operacional.models.ApiUserProfile;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

        // Revalidate SQL assignment on restored Firebase sessions before opening the app.
        FirebaseUser restored = FirebaseAuth.getInstance().getCurrentUser();
        if (session.hasCompleteProfile() && restored != null && restored.getUid().equals(session.getUid())) {
            btnLogin.setEnabled(false);
            refreshTokenAndProceed(restored.getUid());
        }

        btnLogin.setOnClickListener(v -> login());

    }

    private void login() {
        binding.loginStatus.setVisibility(android.view.View.GONE);
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

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
                refreshTokenAndProceed(uid);
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

    private void refreshTokenAndProceed(String uid) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null || !uid.equals(firebaseUser.getUid())) {
            resetLoginButton();
            showLoginStatus("Não foi possível validar a sessão. Tente entrar novamente.");
            return;
        }
        // signInWithEmailAndPassword acabou de obter um token atual; não force
        // uma segunda renovação de rede antes de abrir a sessão.
        firebaseUser.getIdToken(false)
                .addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
                    @Override
                    public void onSuccess(@NonNull GetTokenResult result) {
                        if (!isCurrentFirebaseUser(uid)) return;
                        if (result.getToken() == null || result.getToken().trim().isEmpty()) {
                            resetLoginButton();
                            showLoginStatus("Não foi possível obter o token de autenticação.");
                            return;
                        }
                        if (session.getUid() != null && !uid.equals(session.getUid())) session.clearSession();
                        session.saveFirebaseToken(result.getToken());
                        loadOperationalProfile(uid);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        resetLoginButton();
                        showLoginStatus("Erro ao validar a sessão: " + AuthErrorTranslator.toUserMessage(e));
                    }
                });
    }

    private boolean isCurrentFirebaseUser(String uid) {
        FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
        return !isFinishing() && !isDestroyed() && current != null && uid.equals(current.getUid());
    }

    private void loadOperationalProfile(String uid) {
        ApiService api = RetrofitClient.getClient(getApplicationContext()).create(ApiService.class);
        api.getOperationalProfile().enqueue(new Callback<ApiUserProfile>() {
            @Override public void onResponse(Call<ApiUserProfile> call, Response<ApiUserProfile> response) {
                if (!isCurrentFirebaseUser(uid)) return;
                User user = OperationalSessionUser.fromProfile(uid, response.body());
                if (!response.isSuccessful() || user == null) {
                    failOperationalLogin(response.code() == 401 || response.code() == 403
                            ? "A API não autorizou a conta. Confira o vínculo ativo do funcionário, cargo, loja e empresa no banco."
                            : "Não foi possível carregar o perfil operacional (HTTP " + response.code() + ").");
                    return;
                }
                FirebaseUser identity = FirebaseAuth.getInstance().getCurrentUser();
                String name = identity.getDisplayName();
                if (name == null || name.trim().isEmpty()) name = identity.getEmail();
                if (name != null && !name.trim().isEmpty()) {
                    user = new User(uid, name.trim(), user.getRole(), user.getStoreId());
                }
                // Old operational tokens are integration credentials only, never the SQL login.
                session.saveApiAccessToken(null);
                session.saveApiRefreshToken(null);
                session.saveSession(user);
                session.saveStoreKey(user.getStoreId());
                goToMain();
            }
            @Override public void onFailure(Call<ApiUserProfile> call, Throwable error) {
                if (!isCurrentFirebaseUser(uid)) return;
                failOperationalLogin("Não foi possível conectar à API operacional. Confira a conexão e tente novamente.");
            }
        });
    }

    private void failOperationalLogin(String message) {
        session.clearSession();
        resetLoginButton();
        showLoginStatus(message);
    }

    private void resetLoginButton() {
        btnLogin.setEnabled(true);
        btnLogin.setText(R.string.op_login_button);
    }

    private void goToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
