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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mottainai.operacional.R;
import com.mottainai.operacional.adapters.AlertAdapter;
import com.mottainai.operacional.adapters.SuggestionAdapter;
import com.mottainai.operacional.utils.RoleHelper;
import com.mottainai.operacional.utils.SessionManager;
import com.mottainai.operacional.viewmodels.HomeViewModel;

public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;
    private SessionManager sessionManager;

    // Views
    private ProgressBar progressBar;
    private View containerError;
    private TextView tvError;
    private Button btnRetry;
    private View containerEmpty;
    private TextView tvEmpty;
    private RecyclerView rvAlerts;
    private RecyclerView rvSuggestions;
    private TextView tvSuggestionsTitle;
    private View layoutCardsEstoque;
    private View layoutCardsGerente;
    private View layoutCardsDono;
    private View layoutShortcuts;

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

        initViews(view);
        setupRecyclerViews();
        setupViewModel();
        setupRetryButton();
        setupShortcuts();
    }

    private void initViews(View view) {
        progressBar = view.findViewById(R.id.progress_home);
        containerError = view.findViewById(R.id.container_error);
        tvError = view.findViewById(R.id.tv_error);
        btnRetry = view.findViewById(R.id.btn_retry);
        containerEmpty = view.findViewById(R.id.container_empty);
        tvEmpty = view.findViewById(R.id.tv_empty);
        rvAlerts = view.findViewById(R.id.rv_alerts);
        rvSuggestions = view.findViewById(R.id.rv_suggestions);
        tvSuggestionsTitle = view.findViewById(R.id.tv_suggestions_title);

        layoutCardsEstoque = view.findViewById(R.id.layout_cards_estoque);
        layoutCardsGerente = view.findViewById(R.id.layout_cards_gerente);
        layoutCardsDono = view.findViewById(R.id.layout_cards_dono);
        layoutShortcuts = view.findViewById(R.id.layout_shortcuts);
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

        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            if (isLoading) {
                hideError();
                hideEmpty();
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null) {
                showError(errorMsg);
            }
        });

        viewModel.getAlerts().observe(getViewLifecycleOwner(), alerts -> {
            alertAdapter.setAlerts(alerts);
            checkEmptyState();
            String role = sessionManager.getRole();
            if (role != null) {
                applyRoleRules(role);
            }
        });

        viewModel.getSuggestions().observe(getViewLifecycleOwner(), suggestions -> {
            suggestionAdapter.setSuggestions(suggestions);
            checkEmptyState();
        });

        String storeId = sessionManager.getStoreId();
        if (storeId != null) {
            viewModel.loadData(storeId);
        } else {
            showError("Sessão inválida. Faça login novamente.");
        }
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
        View btnProducts = getView().findViewById(R.id.btn_shortcut_products);
        View btnScan = getView().findViewById(R.id.btn_shortcut_scan);
        View btnDamage = getView().findViewById(R.id.btn_shortcut_damage);

        if (btnProducts != null) {
            btnProducts.setOnClickListener(v -> {
                // Navegar para Products
            });
        }
        if (btnScan != null) {
            btnScan.setOnClickListener(v -> {
                // Navegar para Scanner
            });
        }
        if (btnDamage != null) {
            btnDamage.setOnClickListener(v -> {
                // Navegar para Avaria
            });
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

    private void checkEmptyState() {
        boolean hasAlerts = alertAdapter.getItemCount() > 0;
        boolean hasSuggestions = suggestionAdapter.getItemCount() > 0;

        if (!hasAlerts && !hasSuggestions) {
            showEmpty();
        } else {
            hideEmpty();
            rvAlerts.setVisibility(View.VISIBLE);
            rvSuggestions.setVisibility(View.VISIBLE);
        }
    }

    private void applyRoleRules(String role) {
        boolean isEstoque = RoleHelper.canRegisterProduct(role) && !RoleHelper.isOwner(role); // Estoquista
        boolean isGerente = RoleHelper.canRegisterProduct(role) && !RoleHelper.isOwner(role); // Gerente/Dono
        boolean isDono = RoleHelper.isOwner(role);

        // Cards de estoque (Estoquista)
        layoutCardsEstoque.setVisibility(isEstoque ? View.VISIBLE : View.GONE);
        layoutShortcuts.setVisibility(isEstoque ? View.VISIBLE : View.GONE);

        // Cards de gerente
        layoutCardsGerente.setVisibility(isGerente ? View.VISIBLE : View.GONE);

        // Cards de dono
        layoutCardsDono.setVisibility(isDono ? View.VISIBLE : View.GONE);

        // Sugestões (Gerente e Dono)
        boolean showSuggestions = RoleHelper.canViewSuggestions(role);
        rvSuggestions.setVisibility(showSuggestions ? View.VISIBLE : View.GONE);
        tvSuggestionsTitle.setVisibility(showSuggestions ? View.VISIBLE : View.GONE);
    }
}