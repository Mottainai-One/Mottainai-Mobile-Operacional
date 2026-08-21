package com.mottainai.operacional.fragments;

import android.content.Intent;
import android.os.Bundle;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
        setupFab();
        setupViewModel();
        loadProducts();
    }

    private void setupRecyclerView() {
        adapter = new ProductAdapter();
        adapter.setOnProductClickListener(product -> {
            Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            startActivity(intent);
        });

        binding.rvProducts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvProducts.setAdapter(adapter);
    }

    private void setupSearchView() {
        binding.svProductSearch.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
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
            Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
            intent.putExtra("is_new_product", true);
            startActivity(intent);
        });
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
        if (storeId != null) {
            viewModel.loadProducts(storeId);
        } else {
            showError("ID da loja não encontrado na sessão");
        }
    }

    private void filterProducts(String query) {
        if (adapter.getCurrentList() == null) return;

        if (TextUtils.isEmpty(query)) {
            adapter.getFilter().filter("");
        } else {
            adapter.getFilter().filter(query);
        }
    }

    private void checkEmptyState() {
        if (adapter.getCurrentList() == null || adapter.getCurrentList().isEmpty()) {
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
        String storeId = sessionManager.getStoreId();
        if (storeId != null) {
            viewModel.refreshProducts(storeId);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}