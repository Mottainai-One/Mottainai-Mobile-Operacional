package com.mottainai.operacional.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mottainai.operacional.R;
import com.mottainai.operacional.activities.ProductDetailActivity;
import com.mottainai.operacional.adapters.ProductAdapter;
import com.mottainai.operacional.databinding.FragmentProductCatalogBinding;
import com.mottainai.operacional.utils.RoleHelper;
import com.mottainai.operacional.utils.SessionManager;
import com.mottainai.operacional.viewmodels.ProductListViewModel;

/** Content of the Lista tab inside the Produtos section. */
public class ProductCatalogFragment extends Fragment {

    private FragmentProductCatalogBinding binding;
    private ProductListViewModel viewModel;
    private ProductAdapter adapter;
    private SessionManager sessionManager;
    private long lastClickTime;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProductCatalogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        setupRecyclerView();
        setupSearchView();
        setupFab();
        setupRetry();
        setupViewModel();
        ensureProductsLoaded();
    }

    private void setupRecyclerView() {
        adapter = new ProductAdapter();
        adapter.setOnProductClickListener(product -> {
            if (SystemClock.elapsedRealtime() - lastClickTime < 600) return;
            lastClickTime = SystemClock.elapsedRealtime();
            Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            startActivity(intent);
        });

        binding.rvProducts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvProducts.setAdapter(adapter);
    }

    private void setupSearchView() {
        binding.svProductSearch.setIconified(false);
        binding.svProductSearch.setOnClickListener(v -> focusSearchInput());

        View searchPlate = binding.svProductSearch.findViewById(androidx.appcompat.R.id.search_plate);
        searchPlate.setOnClickListener(v -> focusSearchInput());

        EditText searchInput = binding.svProductSearch
                .findViewById(androidx.appcompat.R.id.search_src_text);
        searchInput.setOnClickListener(v -> focusSearchInput());

        binding.svProductSearch.setOnQueryTextListener(
                new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        // Enquanto o contrato de busca remota não é confirmado,
                        // o filtro opera sobre a página já carregada.
                        filterProducts(newText);
                        return true;
                    }
                });
    }

    private void focusSearchInput() {
        binding.svProductSearch.setIconified(false);
        EditText searchInput = binding.svProductSearch
                .findViewById(androidx.appcompat.R.id.search_src_text);
        searchInput.requestFocus();

        InputMethodManager keyboard = (InputMethodManager) requireContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (keyboard != null) {
            keyboard.showSoftInput(searchInput, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void setupFab() {
        boolean canWrite = RoleHelper.canRegisterProduct(sessionManager.getRole());
        binding.fabAddProduct.setVisibility(canWrite ? View.VISIBLE : View.GONE);
        binding.fabAddProduct.setOnClickListener(v -> {
            if (!canWrite) {
                Toast.makeText(requireContext(), "Sem permissão para criar produto", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
            intent.putExtra("is_new_product", true);
            startActivity(intent);
        });
    }

    private void setupRetry() {
        binding.btnRetry.setOnClickListener(v -> loadProducts());
    }

    private void setupViewModel() {
        // A lista pertence à seção Produtos, não somente a esta página. Assim
        // os dados sobrevivem à troca entre as três abas do ViewPager.
        viewModel = new ViewModelProvider(requireParentFragment()).get(ProductListViewModel.class);

        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressProducts.setVisibility(isLoading ? View.VISIBLE : View.GONE);
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

    private void ensureProductsLoaded() {
        if (viewModel.getProducts().getValue() == null
                && !Boolean.TRUE.equals(viewModel.getLoading().getValue())) {
            loadProducts();
        }
    }

    private void filterProducts(String query) {
        if (adapter.getCurrentList() == null) return;
        adapter.getFilter().filter(TextUtils.isEmpty(query) ? "" : query);
        binding.rvProducts.post(this::checkEmptyState);
    }

    private void checkEmptyState() {
        if (adapter.getItemCount() == 0) {
            String query = binding.svProductSearch.getQuery() == null
                    ? "" : binding.svProductSearch.getQuery().toString();
            binding.tvEmpty.setText(TextUtils.isEmpty(query)
                    ? getString(R.string.empty_products)
                    : "Nenhum resultado para \"" + query + "\"");
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
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
