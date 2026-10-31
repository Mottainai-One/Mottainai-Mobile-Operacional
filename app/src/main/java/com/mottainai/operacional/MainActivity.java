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
import com.mottainai.operacional.databinding.ActivityMainBinding;
import com.mottainai.operacional.notifications.NotificationChannelManager;
import com.mottainai.operacional.notifications.NotificationPermissionManager;
import com.mottainai.operacional.notifications.NotificationRouter;
import com.mottainai.operacional.repository.NotificationRepository;
import com.mottainai.operacional.utils.SessionManager;
import com.mottainai.operacional.utils.ThemeSettings;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private SessionManager session;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeSettings.applySavedTheme(this);
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
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment == null) {
            redirectToLogin();
            return;
        }

        navController = navHostFragment.getNavController();
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            boolean home = destination.getId() == R.id.homeFragment;
            boolean profile = destination.getId() == R.id.profileFragment;
            boolean accountFlow = destination.getId() == R.id.accountSettingsFragment
                    || destination.getId() == R.id.changeEmailFragment
                    || destination.getId() == R.id.changePasswordFragment;
            boolean supplierFlow = destination.getId() == R.id.supplierDetailFragment
                    || destination.getId() == R.id.newSupplierFragment
                    || destination.getId() == R.id.editSupplierFragment
                    || destination.getId() == R.id.supplierSavedFragment;
            boolean reportsFlow = destination.getId() == R.id.reportsFragment
                    || destination.getId() == R.id.teamFragment
                    || destination.getId() == R.id.managerConferencesFragment
                    || destination.getId() == R.id.stockTasksFragment
                    || destination.getId() == R.id.replenishmentFragment
                    || destination.getId() == R.id.receivingWorkflowFragment
                    || destination.getId() == R.id.ownerCountReviewFragment
                    || destination.getId() == R.id.managerReceivingReviewFragment
                    || destination.getId() == R.id.managerDamageReviewFragment;
            binding.opLogo.setVisibility(home ? View.VISIBLE : View.GONE);
            binding.opHeaderTitle.setVisibility(home ? View.GONE : View.VISIBLE);
            binding.btnTheme.setVisibility(home || profile ? View.VISIBLE : View.GONE);
            binding.btnBack.setVisibility(accountFlow || supplierFlow || reportsFlow ? View.VISIBLE : View.GONE);
            binding.btnAssistant.setVisibility(accountFlow || supplierFlow || reportsFlow ? View.GONE : View.VISIBLE);
            if (destination.getId() == R.id.productsListFragment) {
                binding.opHeaderTitle.setText(R.string.op_nav_products);
            } else if (profile) {
                binding.opHeaderTitle.setText(R.string.op_my_profile);
            } else {
                binding.opHeaderTitle.setText(destination.getLabel());
            }
        });

        BottomNavigationView bottomNav = binding.bottomNav;
        NavigationUI.setupWithNavController(bottomNav, navController);
        binding.btnTheme.setOnClickListener(v -> ThemeSettings.toggle(this));
        binding.btnBack.setOnClickListener(v -> {
            if (navController.getCurrentDestination() != null
                    && navController.getCurrentDestination().getId() == R.id.supplierSavedFragment) {
                navController.popBackStack(R.id.productsListFragment, false);
            } else {
                navController.navigateUp();
            }
        });
        binding.btnAssistant.setOnClickListener(v -> navigateToIntelligenceTab("chat"));
        View fabScan = binding.fabScan;
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
        View navHost = binding.navHostFragment;
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
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
