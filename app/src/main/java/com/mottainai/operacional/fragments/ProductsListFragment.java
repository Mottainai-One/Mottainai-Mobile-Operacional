package com.mottainai.operacional.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import androidx.activity.result.contract.ActivityResultContracts;

import com.mottainai.operacional.R;
import com.mottainai.operacional.activities.ProductDetailActivity;
import com.mottainai.operacional.adapters.ProductAdapter;
import com.mottainai.operacional.databinding.FragmentProductsListBinding;
import com.mottainai.operacional.utils.RoleHelper;
import com.mottainai.operacional.utils.SessionManager;
import com.mottainai.operacional.viewmodels.ProductListViewModel;

public class ProductsListFragment extends Fragment {

    private FragmentProductsListBinding binding;
    private ProductListViewModel viewModel;
    private ProductAdapter adapter;
    private SessionManager sessionManager;
    private long lastClickTime = 0;
    private final androidx.activity.result.ActivityResultLauncher<Intent> formLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK) loadProducts();
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProductsListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());

        setupRecyclerView();
        setupSearchView();
        setupTabs();
        setupFab();
        setupRetry();
        setupViewModel();
        // Carrega apenas na primeira criação; LiveData sobrevive à rotação.
        if (savedInstanceState == null) {
            loadProducts();
        }
    }

    private void setupRecyclerView() {
        adapter = new ProductAdapter();
        adapter.setOnProductClickListener(product -> {
            // Evita clique duplo abrindo duas Activities
            if (SystemClock.elapsedRealtime() - lastClickTime < 600) return;
            lastClickTime = SystemClock.elapsedRealtime();
            Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            startActivity(intent);
        });

        binding.rvProducts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvProducts.setAdapter(adapter);
    }

    private void setupTabs() {
        if (binding.tabProducts != null) {
            binding.tabProducts.addOnTabSelectedListener(new com.google.android.material.tabs.TabLayout.OnTabSelectedListener() {
                @Override public void onTabSelected(com.google.android.material.tabs.TabLayout.Tab tab) {
                    int pos = tab.getPosition();
                    if (pos == 1) {
                        Toast.makeText(requireContext(), "Inventário — pendente (aguarda endpoint)", Toast.LENGTH_SHORT).show();
                        binding.tabProducts.selectTab(binding.tabProducts.getTabAt(0));
                    } else if (pos == 2) {
                        Toast.makeText(requireContext(), "Fornecedores — pendente (aguarda contrato)", Toast.LENGTH_SHORT).show();
                        binding.tabProducts.selectTab(binding.tabProducts.getTabAt(0));
                    }
                }
                @Override public void onTabUnselected(com.google.android.material.tabs.TabLayout.Tab tab) {}
                @Override public void onTabReselected(com.google.android.material.tabs.TabLayout.Tab tab) {
                    if (tab.getPosition() != 0) {
                        Toast.makeText(requireContext(), "Funcionalidade pendente", Toast.LENGTH_SHORT).show();
                        binding.tabProducts.selectTab(binding.tabProducts.getTabAt(0));
                    }
                }
            });
        }
    }

    private void setupSearchView() {
        binding.svProductSearch.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // TODO contrato pendente: backend search query (?search=) ainda não confirmado.
                // Hoje filtra apenas sobre a página carregada (filtro local). Não finge busca global.
                filterProducts(newText);
                return true;
            }
        });
    }

    private void setupFab() {
        String role = sessionManager.getRole();
        boolean canWrite = RoleHelper.canRegisterProduct(role);
        binding.fabAddProduct.setVisibility(canWrite ? View.VISIBLE : View.GONE);
        binding.fabAddProduct.setOnClickListener(v -> {
            if (!canWrite) {
                Toast.makeText(requireContext(), "Sem permissão para criar produto", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(requireContext(), com.mottainai.operacional.activities.ProductFormActivity.class);
            formLauncher.launch(intent);
        });
    }

    private void setupRetry() {
        binding.btnRetry.setOnClickListener(v -> loadProducts());
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ProductListViewModel.class);

        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressProducts.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            if (isLoading) {
                hideError();
                hideEmpty();
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null) {
                // 401/403 tratados como sessão: direcionar para login já é feito em MainActivity,
                // mas exibir mensagem adequada aqui.
                showError(errorMsg);
            }
        });

        viewModel.getProducts().observe(getViewLifecycleOwner(), products -> {
            adapter.submitList(products);
            checkEmptyState();
        });
    }

    private void loadProducts() {
        String storeId = sessionManager.getStoreId();
        if (storeId == null || storeId.isEmpty()) {
            showError("Sessão incompleta. Faça login novamente.");
            return;
        }
        viewModel.loadProducts(storeId);
    }

    private void filterProducts(String query) {
        if (adapter.getCurrentList() == null) return;
        if (TextUtils.isEmpty(query)) {
            adapter.getFilter().filter("");
        } else {
            adapter.getFilter().filter(query);
        }
        // Após filtro, atualizar empty se busca não encontrou nada na página
        binding.rvProducts.post(this::checkEmptyState);
    }

    private void checkEmptyState() {
        if (adapter.getItemCount() == 0) {
            String q = binding.svProductSearch.getQuery() != null ? binding.svProductSearch.getQuery().toString() : "";
            if (!TextUtils.isEmpty(q)) {
                binding.tvEmpty.setText("Nenhum resultado para \"" + q + "\"");
            } else {
                binding.tvEmpty.setText(getString(R.string.empty_products));
            }
            showEmpty();
        } else {
            hideEmpty();
        }
    }

    private void showError(String message) {
        binding.progressProducts.setVisibility(View.GONE);
        binding.containerError.setVisibility(View.VISIBLE);
        binding.tvError.setText(message);
        binding.containerEmpty.setVisibility(View.GONE);
        binding.rvProducts.setVisibility(View.GONE);
    }

    private void hideError() {
        binding.containerError.setVisibility(View.GONE);
    }

    private void showEmpty() {
        binding.progressProducts.setVisibility(View.GONE);
        binding.containerEmpty.setVisibility(View.VISIBLE);
        binding.rvProducts.setVisibility(View.GONE);
    }

    private void hideEmpty() {
        binding.containerEmpty.setVisibility(View.GONE);
        binding.rvProducts.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Não recarregar a cada onResume para evitar múltiplas consultas na rotação/volta.
        // Atualiza apenas se lista estiver vazia (ex.: após criar produto em outra tela).
        if (adapter.getItemCount() == 0 && viewModel.getProducts().getValue() == null) {
            loadProducts();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}