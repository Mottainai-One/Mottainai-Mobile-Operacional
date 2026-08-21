package com.mottainai.operacional.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.mottainai.operacional.R;
import com.mottainai.operacional.models.Product;
import com.mottainai.operacional.repository.ProductRepository;
import com.mottainai.operacional.utils.RoleHelper;
import com.mottainai.operacional.utils.SessionManager;
import com.mottainai.operacional.viewmodels.ProductDetailViewModel;

public class ProductDetailActivity extends AppCompatActivity {

    private ProductDetailViewModel viewModel;
    private SessionManager sessionManager;
    private String productId;
    private boolean isNewProduct;

    // Views
    private ImageView ivProductImage;
    private TextView tvProductName;
    private TextView tvProductSku;
    private TextView tvProductQuantity;
    private TextView tvProductMinQuantity;
    private TextView tvProductBatch;
    private TextView tvProductExpiry;
    private TextView tvProductSupplier;
    private TextView tvProductStore;
    private Button btnRegisterDamage;
    private Button btnEditProduct;
    private View progressContainer;
    private View errorContainer;
    private TextView tvError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        sessionManager = new SessionManager(this);
        productId = getIntent().getStringExtra("product_id");
        isNewProduct = getIntent().getBooleanExtra("is_new_product", false);

        initViews();
        setupViewModel();
        setupButtons();

        if (isNewProduct) {
            setupNewProduct();
        } else if (productId != null) {
            loadProduct(productId);
        } else {
            showError("ID do produto não fornecido");
        }
    }

    private void initViews() {
        ivProductImage = findViewById(R.id.iv_product_image);
        tvProductName = findViewById(R.id.tv_product_name);
        tvProductSku = findViewById(R.id.tv_product_sku);
        tvProductQuantity = findViewById(R.id.tv_product_quantity);
        tvProductMinQuantity = findViewById(R.id.tv_product_min_quantity);
        tvProductBatch = findViewById(R.id.tv_product_batch);
        tvProductExpiry = findViewById(R.id.tv_product_expiry);
        tvProductSupplier = findViewById(R.id.tv_product_supplier);
        tvProductStore = findViewById(R.id.tv_product_store);
        btnRegisterDamage = findViewById(R.id.btn_register_damage);
        btnEditProduct = findViewById(R.id.btn_edit_product);
        progressContainer = findViewById(R.id.progress_container);
        errorContainer = findViewById(R.id.error_container);
        tvError = findViewById(R.id.tv_error);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ProductDetailViewModel.class);
        viewModel.getLoading().observe(this, isLoading -> {
            progressContainer.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            if (isLoading) {
                hideError();
            }
        });

        viewModel.getError().observe(this, errorMsg -> {
            if (errorMsg != null) {
                showError(errorMsg);
            }
        });

        viewModel.getProduct().observe(this, product -> {
            if (product != null) {
                bindProduct(product);
            }
        });
    }

    private void setupButtons() {
        String role = new SessionManager(this).getRole();

        btnRegisterDamage.setOnClickListener(v -> {
            Toast.makeText(this, "Registrar Avaria - Em implementação", Toast.LENGTH_SHORT).show();
            // TODO: Navegar para tela de registrar avaria
        });

        boolean canEdit = RoleHelper.canRegisterProduct(new SessionManager(this).getRole());
        btnEditProduct.setVisibility(canEdit ? View.VISIBLE : View.GONE);
        btnEditProduct.setOnClickListener(v -> {
            Toast.makeText(this, "Editar Produto - Em implementação", Toast.LENGTH_SHORT).show();
            // TODO: Navegar para tela de edição
        });
    }

    private void loadProduct(String id) {
        new ProductRepository(getApplication()).fetchProductById(id, new ProductRepository.ProductCallback() {
            @Override
            public void onSuccess(Product product) {
                runOnUiThread(() -> bindProduct(product));
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> showError(message));
            }
        });
    }

    private void setupNewProduct() {
        // Modo criação - mostrar campos editáveis
        progressContainer.setVisibility(View.GONE);
        // TODO: Implementar formulário de criação
        Toast.makeText(this, "Criação de novo produto - Em implementação", Toast.LENGTH_SHORT).show();
    }

    private void bindProduct(Product product) {
        progressContainer.setVisibility(View.GONE);

        tvProductName.setText(product.getName());
        tvProductSku.setText("SKU: " + product.getSku());
        tvProductQuantity.setText("Quantidade: " + product.getQuantity());
        tvProductMinQuantity.setText("Mínimo: " + product.getMinQuantity());

        tvProductBatch.setText("Lote: " + (product.getBatch() != null ? product.getBatch() : "—"));
        tvProductExpiry.setText("Validade: " + (product.getExpiryDate() != null ? product.getExpiryDate() : "—"));
        tvProductSupplier.setText("Fornecedor: " + (product.getSupplier() != null ? product.getSupplier() : "—"));
        tvProductStore.setText("Loja: " + (product.getStoreId() != null ? product.getStoreId() : "—"));

        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.ic_product_placeholder)
                    .error(R.drawable.ic_product_placeholder)
                    .into((ImageView) findViewById(R.id.iv_product_image));
        } else {
            ((ImageView) findViewById(R.id.iv_product_image)).setImageResource(R.drawable.ic_product_placeholder);
        }
    }

    private void showError(String message) {
        progressContainer.setVisibility(View.GONE);
        errorContainer.setVisibility(View.VISIBLE);
        tvError.setText(message);
    }

    private void hideError() {
        errorContainer.setVisibility(View.GONE);
    }
}