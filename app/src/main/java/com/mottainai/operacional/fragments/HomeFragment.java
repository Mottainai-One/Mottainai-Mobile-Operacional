package com.mottainai.operacional.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mottainai.operacional.R;
import com.mottainai.operacional.adapters.AlertAdapter;
import com.mottainai.operacional.adapters.SuggestionAdapter;
import com.mottainai.operacional.utils.RoleHelper;
import com.mottainai.operacional.utils.SessionManager;
import com.mottainai.operacional.utils.Constants;
import com.mottainai.operacional.viewmodels.HomeViewModel;

public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;
    private SessionManager sessionManager;
    private NavController navController;

    // Views
    private ProgressBar progressBar;
    private View containerError;
    private TextView tvError;
    private Button btnRetry;
    private View containerEmpty;
    private TextView tvEmpty;
    private TextView tvWelcome;
    private TextView tvStoreInfo;
    private View headerContainer;

    // Cards Estoquista
    private View layoutCardsEstoque;
    private TextView tvExpiringCount;
    private TextView tvLowStockCount;
    private TextView tvPromotionsCount;

    // Cards Gerente
    private View layoutCardsGerente;
    private TextView tvPendingApprovals;
    private TextView tvFinancialRisk;

    // Cards Dono
    private View layoutCardsDono;
    private TextView tvLossRisk;
    private TextView tvValueSaved;
    private TextView tvActiveStores;

    // Atalhos
    private View layoutShortcuts;
    private View layoutShortcutsGestao;
    private View layoutShortcutsDono;
    private View btnShortcutProducts;
    private View btnShortcutScan;
    private View btnShortcutDamage;
    private View btnShortcutInventario;
    private View btnShortcutAvarias;
    private View btnShortcutFornecedores;
    private View btnShortcutEquipe;
    private View btnShortcutConfig;

    // Listas
    private RecyclerView rvAlerts;
    private RecyclerView rvSuggestions;
    private TextView tvAlertsTitle;
    private TextView tvSuggestionsTitle;

    private AlertAdapter alertAdapter;
    private SuggestionAdapter suggestionAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        initViews(view);
        setupRecyclerViews();
        setupViewModel();
        setupRetryButton();
        setupShortcuts();
        setupHeader();
    }

    private void initViews(View view) {
        progressBar = view.findViewById(R.id.progress_home);
        containerError = view.findViewById(R.id.container_error);
        tvError = view.findViewById(R.id.tv_error);
        btnRetry = view.findViewById(R.id.btn_retry);
        containerEmpty = view.findViewById(R.id.container_empty);
        tvEmpty = view.findViewById(R.id.tv_empty);
        tvWelcome = view.findViewById(R.id.tv_welcome);
        tvStoreInfo = view.findViewById(R.id.tv_store_info);
        headerContainer = view.findViewById(R.id.header_container);

        // Cards Estoquista
        layoutCardsEstoque = view.findViewById(R.id.layout_cards_estoque);
        tvExpiringCount = view.findViewById(R.id.tv_expiring_count);
        tvLowStockCount = view.findViewById(R.id.tv_low_stock_count);
        tvPromotionsCount = view.findViewById(R.id.tv_promotions_count);

        // Cards Gerente
        layoutCardsGerente = view.findViewById(R.id.layout_cards_gerente);
        tvPendingApprovals = view.findViewById(R.id.tv_pending_approvals);
        tvFinancialRisk = view.findViewById(R.id.tv_financial_risk);

        // Cards Dono
        layoutCardsDono = view.findViewById(R.id.layout_cards_dono);
        tvLossRisk = view.findViewById(R.id.tv_loss_risk);
        tvValueSaved = view.findViewById(R.id.tv_value_saved);
        tvActiveStores = view.findViewById(R.id.tv_active_stores);

        // Atalhos
        layoutShortcuts = view.findViewById(R.id.layout_shortcuts);
        layoutShortcutsGestao = view.findViewById(R.id.layout_shortcuts_gestao);
        layoutShortcutsDono = view.findViewById(R.id.layout_shortcuts_dono);
        btnShortcutProducts = view.findViewById(R.id.btn_shortcut_products);
        btnShortcutScan = view.findViewById(R.id.btn_shortcut_scan);
        btnShortcutDamage = view.findViewById(R.id.btn_shortcut_damage);
        btnShortcutInventario = view.findViewById(R.id.btn_shortcut_inventario);
        btnShortcutAvarias = view.findViewById(R.id.btn_shortcut_avarias);
        btnShortcutFornecedores = view.findViewById(R.id.btn_shortcut_fornecedores);
        btnShortcutEquipe = view.findViewById(R.id.btn_shortcut_equipe);
        btnShortcutConfig = view.findViewById(R.id.btn_shortcut_config);

        // Listas
        rvAlerts = view.findViewById(R.id.rv_alerts);
        rvSuggestions = view.findViewById(R.id.rv_suggestions);
        tvAlertsTitle = view.findViewById(R.id.tv_alerts_title);
        tvSuggestionsTitle = view.findViewById(R.id.tv_suggestions_title);
    }

    private void setupRecyclerViews() {
        alertAdapter = new AlertAdapter();
        rvAlerts.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvAlerts.setAdapter(alertAdapter);

        suggestionAdapter = new SuggestionAdapter();
        rvSuggestions.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSuggestions.setAdapter(suggestionAdapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        sessionManager = new SessionManager(requireContext());

        // Verifica se a sessão está completa antes de carregar
        if (!sessionManager.hasCompleteProfile()) {
            showSessionError();
            return;
        }

        // Observa loading
        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            if (isLoading) {
                hideError();
                hideEmpty();
            }
        });

        // Observa erros de rede/dados
        viewModel.getError().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null) {
                showError(errorMsg);
            }
        });

        // Observa erro de sessão
        viewModel.getSessionError().observe(getViewLifecycleOwner(), sessionError -> {
            if (sessionError != null) {
                showSessionError();
            }
        });

        // Observa alertas
        viewModel.getAlerts().observe(getViewLifecycleOwner(), alerts -> {
            alertAdapter.setAlerts(alerts);
            updateEmptyState();
            String role = sessionManager.getRole();
            if (role != null) {
                applyRoleRules(role);
            }
        });

        // Observa sugestões
        viewModel.getSuggestions().observe(getViewLifecycleOwner(), suggestions -> {
            suggestionAdapter.setSuggestions(suggestions);
            updateEmptyState();
        });

        // Carrega dados
        String storeId = sessionManager.getStoreId();
        if (storeId != null) {
            viewModel.loadData(storeId);
        } else {
            showSessionError();
        }
    }

    private void setupHeader() {
        String name = sessionManager.getName();
        String storeId = sessionManager.getStoreId();
        String role = sessionManager.getRole();

        if (name != null && !name.isEmpty()) {
            tvWelcome.setText("Bem-vindo, " + name);
        } else {
            tvWelcome.setText("Bem-vindo");
        }

        String roleLabel = RoleHelper.roleToLabel(role);
        tvStoreInfo.setText("Loja " + storeId + " — " + roleLabel);
    }

    private void setupRetryButton() {
        btnRetry.setOnClickListener(v -> {
            String storeId = sessionManager.getStoreId();
            if (storeId != null) {
                viewModel.retry(storeId);
            }
        });
    }

    private void setupShortcuts() {
        // Estoquista
        if (btnShortcutProducts != null) {
            btnShortcutProducts.setOnClickListener(v -> navigateTo(R.id.productsListFragment));
        }
        if (btnShortcutScan != null) {
            btnShortcutScan.setOnClickListener(v -> navigateTo(R.id.scannerFragment));
        }
        if (btnShortcutDamage != null) {
            btnShortcutDamage.setOnClickListener(v -> navigateTo(R.id.registerDamageFragment));
        }

        // Gerente
        if (btnShortcutInventario != null) {
            btnShortcutInventario.setOnClickListener(v -> navigateTo(R.id.inventoryFragment));
        }
        if (btnShortcutAvarias != null) {
            btnShortcutAvarias.setOnClickListener(v -> navigateTo(R.id.damageListFragment));
        }
        if (btnShortcutFornecedores != null) {
            btnShortcutFornecedores.setOnClickListener(v -> navigateTo(R.id.suppliersFragment));
        }

        // Dono
        if (btnShortcutEquipe != null) {
            btnShortcutEquipe.setOnClickListener(v -> navigateTo(R.id.teamFragment));
        }
        if (btnShortcutConfig != null) {
            btnShortcutConfig.setOnClickListener(v -> navigateTo(R.id.configFragment));
        }
    }

    private void navigateTo(int destinationId) {
        try {
            navController.navigate(destinationId);
        } catch (IllegalArgumentException e) {
            // Destino não existe no grafo - mostra toast informativo
            String msg = "Funcionalidade em desenvolvimento";
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }

    private void applyRoleRules(String role) {
        boolean isEstoque = Constants.ROLE_ESTOQUISTA.equals(role);
        boolean isGerente = Constants.ROLE_GERENTE.equals(role);
        boolean isDono = Constants.ROLE_DONO.equals(role);

        // Esconde todos primeiro
        layoutCardsEstoque.setVisibility(View.GONE);
        layoutCardsGerente.setVisibility(View.GONE);
        layoutCardsDono.setVisibility(View.GONE);
        layoutShortcuts.setVisibility(View.GONE);
        layoutShortcutsGestao.setVisibility(View.GONE);
        layoutShortcutsDono.setVisibility(View.GONE);

        if (isEstoque) {
            // Estoquista: cards de estoque + atalhos básicos
            layoutCardsEstoque.setVisibility(View.VISIBLE);
            layoutShortcuts.setVisibility(View.VISIBLE);
            tvAlertsTitle.setText("Alertas de estoque");
        } else if (isGerente) {
            // Gerente: cards de gestão + atalhos de gestão + sugestões
            layoutCardsGerente.setVisibility(View.VISIBLE);
            layoutShortcuts.setVisibility(View.VISIBLE); // Produtos, Escanear, Avaria
            layoutShortcutsGestao.setVisibility(View.VISIBLE); // Inventário, Avarias, Fornecedores
            tvAlertsTitle.setText("Alertas");
            tvSuggestionsTitle.setVisibility(View.VISIBLE);
            rvSuggestions.setVisibility(View.VISIBLE);
        } else if (isDono) {
            // Dono: cards financeiros + todos atalhos + config
            layoutCardsDono.setVisibility(View.VISIBLE);
            layoutShortcuts.setVisibility(View.VISIBLE);
            layoutShortcutsGestao.setVisibility(View.VISIBLE);
            layoutShortcutsDono.setVisibility(View.VISIBLE);
            tvAlertsTitle.setText("Visão geral");
            tvSuggestionsTitle.setVisibility(View.VISIBLE);
            rvSuggestions.setVisibility(View.VISIBLE);
        }

        // Atualiza contadores dos cards (placeholder - integração com backend futuramente)
        updateCardCounts(role);
    }

    private void updateCardCounts(String role) {
        // Por enquanto usa valores zerados - quando houver API de resumo, preencher aqui
        tvExpiringCount.setText("Vencendo em breve: 0");
        tvLowStockCount.setText("Estoque baixo: 0");
        tvPromotionsCount.setText("Promoções ativas: 0");
        tvPendingApprovals.setText("Aprovações pendentes: 0");
        tvFinancialRisk.setText("Risco financeiro: —");
        tvLossRisk.setText("Risco de perda: —");
        tvValueSaved.setText("Valor salvo: —");
        tvActiveStores.setText("Lojas ativas: 1");
    }

    private void updateEmptyState() {
        boolean hasAlerts = alertAdapter.getItemCount() > 0;
        boolean hasSuggestions = suggestionAdapter.getItemCount() > 0;
        String role = sessionManager.getRole();

        if (!hasAlerts && !hasSuggestions) {
            String emptyMsg;
            if (Constants.ROLE_ESTOQUISTA.equals(role)) {
                emptyMsg = "Nenhum alerta de estoque no momento";
            } else if (Constants.ROLE_GERENTE.equals(role)) {
                emptyMsg = "Nenhum alerta ou sugestão pendente";
            } else {
                emptyMsg = "Nenhum dado para exibir";
            }
            tvEmpty.setText(emptyMsg);
            showEmpty();
        } else {
            hideEmpty();
            rvAlerts.setVisibility(View.VISIBLE);
            if (hasSuggestions || Constants.ROLE_GERENTE.equals(role) || Constants.ROLE_DONO.equals(role)) {
                rvSuggestions.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showError(String message) {
        progressBar.setVisibility(View.GONE);
        containerError.setVisibility(View.VISIBLE);
        tvError.setText(message);
        containerEmpty.setVisibility(View.GONE);
        rvAlerts.setVisibility(View.GONE);
        rvSuggestions.setVisibility(View.GONE);
    }

    private void hideError() {
        containerError.setVisibility(View.GONE);
    }

    private void showEmpty() {
        progressBar.setVisibility(View.GONE);
        containerEmpty.setVisibility(View.VISIBLE);
        rvAlerts.setVisibility(View.GONE);
        rvSuggestions.setVisibility(View.GONE);
    }

    private void hideEmpty() {
        containerEmpty.setVisibility(View.GONE);
    }

    private void showSessionError() {
        progressBar.setVisibility(View.GONE);
        containerError.setVisibility(View.VISIBLE);
        tvError.setText("Sessão incompleta. Faça login novamente.");
        containerEmpty.setVisibility(View.GONE);
        rvAlerts.setVisibility(View.GONE);
        rvSuggestions.setVisibility(View.GONE);
        headerContainer.setVisibility(View.GONE);
    }
}