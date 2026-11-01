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
import com.mottainai.operacional.utils.FirebaseSessionUser;
import com.mottainai.operacional.utils.RoleHelper;
import com.mottainai.operacional.models.User;
import com.mottainai.operacional.repository.AuthRepository;
import com.mottainai.operacional.repository.NotificationRepository;
import com.google.firebase.auth.FirebaseUser;
import com.mottainai.operacional.network.ApiService;
import com.mottainai.operacional.network.RetrofitClient;
import com.mottainai.operacional.models.ApiLoginRequest;
import com.mottainai.operacional.models.ApiTokenResponse;
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
    private String loginPassword;

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

        if (session.isLoggedIn() && session.getApiAccessToken() != null
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
        loginPassword = password;

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
                        User user = FirebaseSessionUser.fromClaims(
                                uid, firebaseUser.getEmail(), result.getClaims());
                        if (user == null) {
                            resetLoginButton();
                            showLoginStatus("Conta sem cargo ou loja válidos. Contate o administrador.");
                            return;
                        }
                        session.saveFirebaseToken(result.getToken());
                        session.saveStoreKey(user.getStoreId());
                        exchangeOperationalToken(user, result.getToken());
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

    private void exchangeOperationalToken(User firebaseUser, String firebaseToken) {
        ApiService api = RetrofitClient.getClient(getApplicationContext()).create(ApiService.class);
        api.exchangeFirebaseToken(new ApiLoginRequest(firebaseToken, loginPassword))
                .enqueue(new Callback<ApiTokenResponse>() {
                    @Override public void onResponse(Call<ApiTokenResponse> call, Response<ApiTokenResponse> response) {
                        if (!response.isSuccessful() || response.body() == null
                                || response.body().getAccessToken() == null
                                || response.body().getRefreshToken() == null) {
                            failOperationalLogin("A API operacional recusou a sessão (HTTP " + response.code() + ").");
                            return;
                        }
                        session.saveApiAccessToken(response.body().getAccessToken());
                        session.saveApiRefreshToken(response.body().getRefreshToken());
                        api.getOperationalProfile().enqueue(new Callback<ApiUserProfile>() {
                            @Override public void onResponse(Call<ApiUserProfile> call, Response<ApiUserProfile> profileResponse) {
                                ApiUserProfile profile = profileResponse.body();
                                if (!profileResponse.isSuccessful() || profile == null || !Boolean.TRUE.equals(profile.getActive())
                                        || profile.getStoreId() == null || profile.getRole() == null
                                        || !RoleHelper.normalize(profile.getRole()).equals(firebaseUser.getRole())) {
                                    failOperationalLogin("O perfil da API está incompleto ou diverge do cargo autenticado.");
                                    return;
                                }
                                session.saveSession(new User(firebaseUser.getUid(),
                                        profile.getName() == null ? firebaseUser.getName() : profile.getName(),
                                        RoleHelper.normalize(profile.getRole()), String.valueOf(profile.getStoreId())));
                                new NotificationRepository(getApplicationContext()).captureCurrentDeviceToken();
                                goToMain();
                            }
                            @Override public void onFailure(Call<ApiUserProfile> call, Throwable error) {
                                failOperationalLogin("Não foi possível carregar o perfil operacional: " + error.getMessage());
                            }
                        });
                    }
                    @Override public void onFailure(Call<ApiTokenResponse> call, Throwable error) {
                        failOperationalLogin("Não foi possível validar a conta na API operacional: " + error.getMessage());
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
