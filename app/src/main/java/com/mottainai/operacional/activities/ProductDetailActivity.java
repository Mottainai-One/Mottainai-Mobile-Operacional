package com.mottainai.operacional.activities;

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
import com.mottainai.operacional.utils.RoleHelper;
import com.mottainai.operacional.utils.SessionManager;
import com.mottainai.operacional.viewmodels.ProductDetailViewModel;

public class ProductDetailActivity extends AppCompatActivity {

    private ProductDetailViewModel viewModel;
    private SessionManager sessionManager;
    private String productId;
    private boolean isNewProduct;
    private final androidx.activity.result.ActivityResultLauncher<android.content.Intent> formLauncher =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(), r -> {
                if (r.getResultCode() == RESULT_OK && productId != null) viewModel.loadProduct(productId);
            });

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
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
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
        btnRegisterDamage.setOnClickListener(v -> {
            if (productId == null) return;
            android.content.Intent intent = new android.content.Intent(this, RegisterDamageActivity.class);
            intent.putExtra("product_id", productId);
            startActivity(intent);
        });

        boolean canEdit = RoleHelper.canRegisterProduct(sessionManager.getRole());
        btnEditProduct.setVisibility(canEdit ? View.VISIBLE : View.GONE);
        btnEditProduct.setOnClickListener(v -> {
            if (!canEdit) {
                Toast.makeText(this, "Sem permissão para editar", Toast.LENGTH_SHORT).show();
                return;
            }
            if (productId == null) return;
            android.content.Intent intent = new android.content.Intent(this, ProductFormActivity.class);
            intent.putExtra("product_id", productId);
            formLauncher.launch(intent);
        });
    }

    /** Delega ao ViewModel; Activity não acessa Repository diretamente. */
    private void loadProduct(String id) {
        viewModel.loadProduct(id);
    }

    private void setupNewProduct() {
        // Redireciona para ProductFormActivity para criação real
        boolean canCreate = RoleHelper.canRegisterProduct(sessionManager.getRole());
        if (!canCreate) {
            showError("Sem permissão para criar produto");
            btnEditProduct.setVisibility(View.GONE);
            return;
        }
        android.content.Intent intent = new android.content.Intent(this, ProductFormActivity.class);
        startActivity(intent);
        finish();
    }

    private void bindProduct(Product product) {
        progressContainer.setVisibility(View.GONE);

        tvProductName.setText(product.getName());
        // SKU na UI corresponde a barcode da API
        tvProductSku.setText("Código: " + (product.getSku() != null ? product.getSku() : "—"));
        // Campos de inventário pendentes — exibir placeholder até /api/v1/inventory existir
        tvProductQuantity.setText("Quantidade: " + product.getQuantity() + " (contrato pendente)");
        tvProductMinQuantity.setText("Mínimo: " + product.getMinQuantity() + " (contrato pendente)");
        tvProductBatch.setText("Lote: " + (product.getBatch() != null ? product.getBatch() : "— (pendente)"));
        tvProductExpiry.setText("Validade: " + (product.getExpiryDate() != null ? product.getExpiryDate() : "— (pendente)"));
        tvProductSupplier.setText("Fornecedor: " + (product.getSupplier() != null ? product.getSupplier() : "—"));
        tvProductStore.setText("Loja: " + (product.getStoreId() != null ? product.getStoreId() : sessionManager.getStoreId() + " (token)"));

        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.ic_product_placeholder)
                    .error(R.drawable.ic_product_placeholder)
                    .into(ivProductImage);
        } else {
            ivProductImage.setImageResource(R.drawable.ic_product_placeholder);
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