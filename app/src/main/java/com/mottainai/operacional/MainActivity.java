package com.mottainai.operacional;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import com.mottainai.operacional.activities.LoginActivity;
import com.mottainai.operacional.utils.SessionManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Controla a autenticação ANTES de criar a view/NavHostFragment.
        // Centraliza aqui o guard de sessão: valida Firebase Auth + sessão local completa.
        SessionManager session = new SessionManager(this);

        boolean firebaseAuthed = FirebaseAuth.getInstance().getCurrentUser() != null;
        boolean sessionComplete = session.hasCompleteProfile();

        if (!firebaseAuthed || !sessionComplete) {
            // Limpa a sessão local se o Firebase não tiver usuário (sessão inválida/expirada)
            if (!firebaseAuthed) {
                session.clearSession();
            }
            redirectToLogin();
            return;
        }

        // Só monta a UI depois de validar a sessão
        setContentView(R.layout.activity_main);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment == null) {
            redirectToLogin();
            return;
        }

        NavController navController = navHostFragment.getNavController();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        NavigationUI.setupWithNavController(bottomNav, navController);
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}