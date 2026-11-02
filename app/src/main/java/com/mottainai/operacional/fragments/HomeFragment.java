package com.mottainai.operacional.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mottainai.operacional.R;
import com.mottainai.operacional.adapters.AlertAdapter;
import com.mottainai.operacional.databinding.FragmentHomeBinding;
import com.mottainai.operacional.models.Alert;
import com.mottainai.operacional.models.SalesSummaryResponse;
import com.mottainai.operacional.models.Suggestion;
import com.mottainai.operacional.models.User;
import com.mottainai.operacional.repository.SalesSummaryRepository;
import com.mottainai.operacional.repository.UserRepository;
import com.mottainai.operacional.utils.Constants;
import com.mottainai.operacional.utils.RoleHelper;
import com.mottainai.operacional.utils.SessionManager;
import com.mottainai.operacional.viewmodels.HomeViewModel;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Entrada por perfil da seção Mobile Operacional. Os dados visíveis vêm das fontes existentes. */
public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private SessionManager session;
    private NavController navController;
    private AlertAdapter stockAlerts;
    private AlertAdapter managerAlerts;
    private Call<SalesSummaryResponse> salesSummaryCall;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        session = new SessionManager(requireContext());
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        setupRole();
        setupLists();
        setupActions();
        observeState();
        if (!session.hasCompleteProfile()) {
            showError(getString(R.string.op_session_incomplete));
        } else {
            viewModel.loadData(session.getStoreId());
        }
    }

    private void setupRole() {
        String role = RoleHelper.normalize(session.getRole());
        String roleLabel = RoleHelper.roleToLabel(role);
        String storeId = session.getStoreId();
        String storeLabel = storeId == null ? "" : getString(R.string.op_store_id, storeId);
        binding.tvStoreInfo.setText(roleLabel + " — " + storeLabel);
        binding.homeStock.setVisibility(Constants.ROLE_ESTOQUISTA.equals(role) ? View.VISIBLE : View.GONE);
        binding.homeManager.setVisibility(Constants.ROLE_GERENTE.equals(role) ? View.VISIBLE : View.GONE);
        binding.homeOwner.setVisibility(Constants.ROLE_DONO.equals(role) ? View.VISIBLE : View.GONE);

        if (Constants.ROLE_ESTOQUISTA.equals(role)) {
            String name = session.getName();
            binding.tvWelcome.setText(getString(R.string.op_stock_greeting,
                    name == null || name.trim().isEmpty() ? getString(R.string.op_team_member) : name));
        } else if (Constants.ROLE_GERENTE.equals(role)) {
            binding.tvWelcome.setText(R.string.op_manager_greeting);
        } else {
            binding.tvWelcome.setText(R.string.op_owner_greeting);
        }

        binding.managerAdjustments.label.setText(R.string.op_metric_adjustments);
        binding.managerTasks.label.setText(R.string.op_metric_tasks);
        binding.managerTeam.label.setText(R.string.op_metric_team);
        binding.ownerLosses.label.setText(R.string.op_metric_losses);
        binding.ownerLowStock.label.setText(R.string.op_metric_low_stock);
        binding.ownerLosses.value.setText(R.string.op_data_unavailable);
        binding.managerTasks.value.setText(R.string.op_data_unavailable);
        binding.managerTeam.value.setText(R.string.op_data_unavailable);

        if (Constants.ROLE_DONO.equals(role)) {
            loadOwnerSales();
        }

        if (Constants.ROLE_GERENTE.equals(role) && storeId != null && !storeId.trim().isEmpty()) {
            new UserRepository().listTeam(storeId, new UserRepository.TeamCallback() {
                @Override public void onSuccess(List<User> users) {
                    if (binding == null) return;
                    int count = users == null ? 0 : users.size();
                    binding.managerTeam.value.setText(getResources().getQuantityString(
                            R.plurals.op_team_member_count, count, count));
                }

                @Override public void onError(Exception error) {
                    if (binding != null) binding.managerTeam.value.setText(R.string.op_data_unavailable);
                }
            });
        }
    }

    private void setupLists() {
        stockAlerts = new AlertAdapter();
        managerAlerts = new AlertAdapter();
        binding.rvStockAlerts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvStockAlerts.setAdapter(stockAlerts);
        binding.rvManagerAlerts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvManagerAlerts.setAdapter(managerAlerts);
    }

    private void loadOwnerSales() {
        binding.ownerSales.setText(R.string.op_sales_loading);
        Integer storeId;
        try {
            storeId = Integer.valueOf(session.getStoreId());
        } catch (RuntimeException error) {
            binding.ownerSales.setText(R.string.op_data_unavailable);
            return;
        }
        salesSummaryCall = new SalesSummaryRepository(requireContext()).getCurrentMonth(storeId);
        salesSummaryCall.enqueue(new Callback<SalesSummaryResponse>() {
            @Override
            public void onResponse(Call<SalesSummaryResponse> call, Response<SalesSummaryResponse> response) {
                if (binding == null) return;
                SalesSummaryResponse summary = response.body();
                if (!response.isSuccessful() || summary == null || summary.getTotalAmount() == null) {
                    binding.ownerSales.setText(R.string.op_data_unavailable);
                    return;
                }
                NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
                binding.ownerSales.setText(currency.format(summary.getTotalAmount()));
            }

            @Override
            public void onFailure(Call<SalesSummaryResponse> call, Throwable error) {
                if (binding != null && !call.isCanceled()) {
                    binding.ownerSales.setText(R.string.op_data_unavailable);
                }
            }
        });
    }

    private void setupActions() {
        binding.btnRetry.setOnClickListener(v -> {
            if (session.hasCompleteProfile()) viewModel.retry(session.getStoreId());
        });
        binding.btnStockInventory.setOnClickListener(v -> openInventory());
        binding.btnStockTasks.setOnClickListener(v ->
                navController.navigate(R.id.stockTasksFragment));
        binding.btnManagerSuggestion.setOnClickListener(v -> openIntelligence("suggestions"));
        binding.btnManagerConferences.setOnClickListener(v ->
                navController.navigate(R.id.managerConferencesFragment));
        binding.btnOwnerInventory.setOnClickListener(v -> openInventory());
        binding.btnOwnerCountReviews.setOnClickListener(v ->
                navController.navigate(R.id.ownerCountReviewFragment));
        binding.btnOwnerReport.setOnClickListener(v -> navController.navigate(R.id.reportsFragment));
    }

    private void observeState() {
        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.progressHome.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE);
            if (Boolean.TRUE.equals(loading)) binding.containerError.setVisibility(View.GONE);
        });
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.trim().isEmpty()) showError(error);
        });
        viewModel.getSessionError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) showError(getString(R.string.op_session_incomplete));
        });
        viewModel.getAlerts().observe(getViewLifecycleOwner(), this::showAlerts);
        viewModel.getSuggestions().observe(getViewLifecycleOwner(), this::showSuggestions);
    }

    private void showAlerts(List<Alert> alerts) {
        stockAlerts.setAlerts(alerts);
        managerAlerts.setAlerts(alerts);
        int size = alerts == null ? 0 : alerts.size();
        binding.stockAlertsEmpty.setVisibility(size == 0 ? View.VISIBLE : View.GONE);
        binding.rvStockAlerts.setVisibility(size == 0 ? View.GONE : View.VISIBLE);
        binding.managerAlertsEmpty.setVisibility(size == 0 ? View.VISIBLE : View.GONE);
        binding.rvManagerAlerts.setVisibility(size == 0 ? View.GONE : View.VISIBLE);
        binding.managerAdjustments.value.setText(String.format(java.util.Locale.getDefault(), "%02d", size));

        int lowStock = 0;
        if (alerts != null) {
            for (Alert alert : alerts) {
                if ("ATENCAO".equalsIgnoreCase(alert.getSeverity())
                        || "CRITICO".equalsIgnoreCase(alert.getSeverity())) lowStock++;
            }
        }
        binding.ownerLowStock.value.setText(getResources().getQuantityString(
                R.plurals.op_product_count, lowStock, lowStock));
    }

    private void showSuggestions(List<Suggestion> suggestions) {
        Suggestion pending = null;
        if (suggestions != null) {
            for (Suggestion suggestion : suggestions) {
                String status = suggestion.getStatus();
                if (status == null || status.trim().isEmpty()
                        || "pending".equalsIgnoreCase(status.trim())) {
                    pending = suggestion;
                    break;
                }
            }
        }
        String title = pending == null ? null : pending.getTitle();
        binding.managerHeroTitle.setText(title == null || title.trim().isEmpty()
                ? getString(R.string.op_no_suggestions) : title);
        binding.btnManagerSuggestion.setVisibility(pending == null ? View.GONE : View.VISIBLE);
    }

    private void showError(String message) {
        binding.progressHome.setVisibility(View.GONE);
        binding.containerError.setVisibility(View.VISIBLE);
        binding.tvError.setText(message);
    }

    private void openInventory() {
        Bundle args = new Bundle();
        args.putInt("initial_tab", 1);
        navController.navigate(R.id.productsListFragment, args);
    }

    private void openIntelligence(String tab) {
        Bundle args = new Bundle();
        args.putString("initial_ia_tab", tab);
        navController.navigate(R.id.iaAlertsFragment, args);
    }

    @Override
    public void onDestroyView() {
        if (salesSummaryCall != null) {
            salesSummaryCall.cancel();
            salesSummaryCall = null;
        }
        super.onDestroyView();
        binding = null;
    }
}
