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
import com.mottainai.operacional.databinding.ActivityProductDetailBinding;
import com.mottainai.operacional.models.Product;
import com.mottainai.operacional.utils.RoleHelper;
import com.mottainai.operacional.utils.SessionManager;
import com.mottainai.operacional.utils.ThemeSettings;
import com.mottainai.operacional.viewmodels.ProductDetailViewModel;

public class ProductDetailActivity extends AppCompatActivity {

    private ActivityProductDetailBinding binding;

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
        ThemeSettings.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        binding = ActivityProductDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
        binding.btnBack.setOnClickListener(v -> finish());
        ivProductImage = binding.ivProductImage;
        tvProductName = binding.tvProductName;
        tvProductSku = binding.tvProductSku;
        tvProductQuantity = binding.tvProductQuantity;
        tvProductMinQuantity = binding.tvProductMinQuantity;
        tvProductBatch = binding.tvProductBatch;
        tvProductExpiry = binding.tvProductExpiry;
        tvProductSupplier = binding.tvProductSupplier;
        tvProductStore = binding.tvProductStore;
        btnRegisterDamage = binding.btnRegisterDamage;
        btnEditProduct = binding.btnEditProduct;
        progressContainer = binding.progressContainer;
        errorContainer = binding.errorContainer;
        tvError = binding.tvError;
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
        // Quantidades e lotes exigem o contrato operacional de inventário.
        tvProductQuantity.setText(R.string.op_stock_unavailable);
        tvProductMinQuantity.setVisibility(View.GONE);
        tvProductBatch.setText("Lote: " + (product.getBatch() != null ? product.getBatch() : "—"));
        tvProductExpiry.setText("Validade: " + (product.getExpiryDate() != null ? product.getExpiryDate() : "—"));
        tvProductSupplier.setText("Fornecedor: " + (product.getSupplier() != null ? product.getSupplier() : "—"));
        tvProductStore.setVisibility(View.GONE);

        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.op_box)
                    .error(R.drawable.op_box)
                    .into(ivProductImage);
        } else {
            ivProductImage.setImageResource(R.drawable.op_box);
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
