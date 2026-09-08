package com.mottainai.operacional;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.mottainai.operacional.activities.LoginActivity;
import com.mottainai.operacional.notifications.NotificationChannelManager;
import com.mottainai.operacional.notifications.NotificationPermissionManager;
import com.mottainai.operacional.notifications.NotificationRouter;
import com.mottainai.operacional.repository.NotificationRepository;
import com.mottainai.operacional.utils.SessionManager;

public class MainActivity extends AppCompatActivity {

    private SessionManager session;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);

        // Controla a autenticação ANTES de criar a view/NavHostFragment.
        // Centraliza aqui o guard de sessão: valida Firebase Auth + sessão local completa.
        session = new SessionManager(this);

        boolean firebaseAuthed = FirebaseAuth.getInstance().getCurrentUser() != null;
        boolean sessionComplete = session.hasCompleteProfile();

        if (!firebaseAuthed || !sessionComplete) {
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

        navController = navHostFragment.getNavController();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        NavigationUI.setupWithNavController(bottomNav, navController);
        View fabScan = findViewById(R.id.fab_scan);
        fabScan.setOnClickListener(v -> {
            if (navController.getCurrentDestination() == null
                    || navController.getCurrentDestination().getId() != R.id.scannerFragment) {
                navController.navigate(R.id.scannerFragment);
            }
        });
        applyWindowInsets(bottomNav);
        NotificationChannelManager.ensureChannels(this);
        new NotificationRepository(this).captureCurrentDeviceToken();
        NotificationPermissionManager.requestPermissionIfNeeded(this, session);
        handleNotificationIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleNotificationIntent(intent);
    }

    private void applyWindowInsets(View bottomNav) {
        View root = findViewById(android.R.id.content);
        View navHost = findViewById(R.id.nav_host_fragment);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, 0);
            bottomNav.setPadding(bottomNav.getPaddingLeft(), bottomNav.getPaddingTop(),
                    bottomNav.getPaddingRight(), bars.bottom);
            // O conteúdo recebe o inset do teclado; as barras já foram tratadas acima.
            ViewCompat.dispatchApplyWindowInsets(navHost, windowInsets);
            return WindowInsetsCompat.CONSUMED;
        });
        ViewCompat.requestApplyInsets(root);
    }

    private void handleNotificationIntent(Intent intent) {
        if (navController == null) return;

        NotificationRouter.Destination destination = NotificationRouter.destinationFromIntent(intent);
        if (destination == null
                || !NotificationRouter.isDestinationAllowed(destination, session.getRole())) {
            return;
        }

        String entityId = intent.getStringExtra(NotificationRouter.EXTRA_ENTITY_ID);
        switch (destination) {
            case PRODUCT:
                if (entityId == null || entityId.trim().isEmpty()) {
                    navigateIfNeeded(R.id.productsListFragment, null);
                } else {
                    Bundle args = new Bundle();
                    args.putString("product_id", entityId);
                    args.putBoolean("is_new_product", false);
                    navigateIfNeeded(R.id.productDetailActivity, args);
                }
                break;
            case DAMAGE:
                navigateIfNeeded(R.id.scannerFragment, null);
                break;
            case ALERT:
                navigateToIntelligenceTab("alerts");
                break;
            case SUGGESTION:
                navigateToIntelligenceTab("suggestions");
                break;
            case INVENTORY:
                Bundle inventoryArgs = new Bundle();
                inventoryArgs.putInt("initial_tab", 1);
                navigateIfNeeded(R.id.productsListFragment, inventoryArgs);
                break;
            case HOME:
            default:
                navigateIfNeeded(R.id.homeFragment, null);
                break;
        }

        intent.removeExtra(NotificationRouter.EXTRA_DESTINATION);
        intent.removeExtra(NotificationRouter.EXTRA_ENTITY_ID);
        intent.removeExtra(NotificationRouter.EXTRA_NOTIFICATION_ID);
    }

    private void navigateToIntelligenceTab(String tab) {
        Bundle args = new Bundle();
        args.putString("initial_ia_tab", tab);
        navigateIfNeeded(R.id.iaAlertsFragment, args);
    }

    private void navigateIfNeeded(int destinationId, Bundle args) {
        if (navController.getCurrentDestination() != null
                && navController.getCurrentDestination().getId() == destinationId
                && args == null) {
            return;
        }
        navController.navigate(destinationId, args);
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
