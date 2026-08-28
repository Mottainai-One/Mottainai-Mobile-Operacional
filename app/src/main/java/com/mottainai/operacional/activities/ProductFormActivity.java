package com.mottainai.operacional.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.mottainai.operacional.R;
import com.mottainai.operacional.models.OpenFoodFactsResponse;
import com.mottainai.operacional.models.ProductForm;
import com.mottainai.operacional.repository.OpenFoodFactsRepository;
import com.mottainai.operacional.utils.RoleHelper;
import com.mottainai.operacional.utils.SessionManager;
import com.mottainai.operacional.viewmodels.ProductFormViewModel;

public class ProductFormActivity extends AppCompatActivity {

    private ProductFormViewModel viewModel;
    private SessionManager sessionManager;
    private OpenFoodFactsRepository offRepo;

    private TextInputEditText etBarcode, etName, etDescription, etCategory, etBrand, etUnit, etWeight, etQuantity, etMinQuantity, etBatch, etSupplier, etExpiry;
    private SwitchMaterial swActive;
    private View progress;
    private View tvFormError;
    private android.widget.TextView tvErrorBarcode, tvErrorName, tvErrorCategory, tvErrorUnit, tvErrorWeight, tvErrorExpiry, tvOffInfo;
    private com.google.android.material.button.MaterialButton btnSearchOff, btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        // Bloqueio real: ESTOQUISTA não pode criar/editar
        if (!RoleHelper.canRegisterProduct(sessionManager.getRole())) {
            Toast.makeText(this, "Sem permissão para criar/editar produto", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        setContentView(R.layout.activity_product_form);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        offRepo = new OpenFoodFactsRepository();
        initViews();
        setupViewModel();

        String productId = getIntent().getStringExtra("product_id");
        if (productId != null && !productId.isEmpty()) {
            setTitle("Editar produto");
            viewModel.initEdit(productId);
        } else {
            setTitle("Novo produto");
            viewModel.initNew();
            // Se veio barcode do scanner
            String barcode = getIntent().getStringExtra("barcode");
            if (barcode != null) etBarcode.setText(barcode);
        }
    }

    private void initViews() {
        etBarcode = findViewById(R.id.et_barcode);
        etName = findViewById(R.id.et_name);
        etDescription = findViewById(R.id.et_description);
        etCategory = findViewById(R.id.et_category);
        etBrand = findViewById(R.id.et_brand);
        etUnit = findViewById(R.id.et_unit);
        etWeight = findViewById(R.id.et_weight);
        etQuantity = findViewById(R.id.et_quantity);
        etMinQuantity = findViewById(R.id.et_min_quantity);
        etBatch = findViewById(R.id.et_batch);
        etSupplier = findViewById(R.id.et_supplier);
        etExpiry = findViewById(R.id.et_expiry);
        swActive = findViewById(R.id.sw_active);
        progress = findViewById(R.id.progress_form);
        tvFormError = findViewById(R.id.tv_form_error);
        tvErrorBarcode = findViewById(R.id.tv_error_barcode);
        tvErrorName = findViewById(R.id.tv_error_name);
        tvErrorCategory = findViewById(R.id.tv_error_category);
        tvErrorUnit = findViewById(R.id.tv_error_unit);
        tvErrorWeight = findViewById(R.id.tv_error_weight);
        tvErrorExpiry = findViewById(R.id.tv_error_expiry);
        tvOffInfo = findViewById(R.id.tv_off_info);
        btnSearchOff = findViewById(R.id.btn_search_off);
        btnSave = findViewById(R.id.btn_save);
        findViewById(R.id.btn_cancel).setOnClickListener(v -> finish());
        btnSearchOff.setOnClickListener(v -> searchOff());
        btnSave.setOnClickListener(v -> onSave());
        // Validade: categorias padrão se vazio
        if (etCategory.getText() != null && TextUtils.isEmpty(etCategory.getText().toString())) etCategory.setText("1");
        if (etUnit.getText() != null && TextUtils.isEmpty(etUnit.getText().toString())) etUnit.setText("UN");
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ProductFormViewModel.class);
        viewModel.getLoading().observe(this, loading -> progress.setVisibility(loading ? View.VISIBLE : View.GONE));
        viewModel.getSaving().observe(this, saving -> {
            btnSave.setEnabled(!saving);
            progress.setVisibility(saving ? View.VISIBLE : View.GONE);
            if (saving) tvFormError.setVisibility(View.GONE);
        });
        viewModel.getForm().observe(this, form -> bindForm(form));
        viewModel.getErrors().observe(this, errors -> {
            showFieldError(tvErrorBarcode, errors.get("barcode"));
            showFieldError(tvErrorName, errors.get("name"));
            showFieldError(tvErrorCategory, errors.get("categoryId"));
            showFieldError(tvErrorUnit, errors.get("unitMeasure"));
            showFieldError(tvErrorWeight, errors.get("weight"));
            showFieldError(tvErrorExpiry, errors.get("expiryDate"));
            if (!errors.isEmpty()) {
                android.widget.TextView tv = findViewById(R.id.tv_form_error);
                tv.setText("Corrija os campos destacados");
                tv.setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.tv_form_error).setVisibility(View.GONE);
            }
        });
        viewModel.getSubmitError().observe(this, msg -> {
            if (msg != null) {
                android.widget.TextView tv = findViewById(R.id.tv_form_error);
                tv.setText(msg);
                tv.setVisibility(View.VISIBLE);
            }
        });
        viewModel.getSubmitSuccess().observe(this, product -> {
            if (product != null) {
                Toast.makeText(this, viewModel.isEditMode() ? "Produto atualizado" : "Produto criado", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        });
        viewModel.getLoadError().observe(this, msg -> {
            if (msg != null) {
                android.widget.TextView tv = findViewById(R.id.tv_form_error);
                tv.setText(msg);
                tv.setVisibility(View.VISIBLE);
            }
        });
    }

    private void bindForm(ProductForm f) {
        if (f == null) return;
        if (!equalsText(etBarcode, f.getBarcode())) etBarcode.setText(f.getBarcode());
        if (!equalsText(etName, f.getName())) etName.setText(f.getName());
        if (!equalsText(etDescription, f.getDescription())) etDescription.setText(f.getDescription());
        if (!equalsText(etCategory, f.getCategoryId() != null ? String.valueOf(f.getCategoryId()) : null)) etCategory.setText(f.getCategoryId() != null ? String.valueOf(f.getCategoryId()) : "");
        if (!equalsText(etBrand, f.getBrand())) etBrand.setText(f.getBrand());
        if (!equalsText(etUnit, f.getUnitMeasure())) etUnit.setText(f.getUnitMeasure());
        if (!equalsText(etWeight, f.getWeight())) etWeight.setText(f.getWeight());
        if (!equalsText(etQuantity, f.getQuantity())) etQuantity.setText(f.getQuantity());
        if (!equalsText(etMinQuantity, f.getMinQuantity())) etMinQuantity.setText(f.getMinQuantity());
        if (!equalsText(etBatch, f.getBatch())) etBatch.setText(f.getBatch());
        if (!equalsText(etSupplier, f.getSupplier())) etSupplier.setText(f.getSupplier());
        if (!equalsText(etExpiry, f.getExpiryDate())) etExpiry.setText(f.getExpiryDate());
        swActive.setChecked(f.isActive());
    }

    private boolean equalsText(TextInputEditText et, String v) {
        String cur = et.getText() != null ? et.getText().toString() : "";
        return cur.equals(v != null ? v : "");
    }

    private void showFieldError(android.widget.TextView tv, String msg) {
        if (msg != null) { tv.setText(msg); tv.setVisibility(View.VISIBLE); } else tv.setVisibility(View.GONE);
    }

    private ProductForm collectForm() {
        ProductForm f = viewModel.getForm().getValue();
        if (f == null) f = new ProductForm();
        f.setBarcode(text(etBarcode));
        f.setName(text(etName));
        f.setDescription(text(etDescription));
        try { f.setCategoryId(etCategory.getText()!=null && !TextUtils.isEmpty(etCategory.getText().toString()) ? Integer.parseInt(etCategory.getText().toString().trim()) : null); } catch (NumberFormatException e){ f.setCategoryId(null); }
        f.setBrand(text(etBrand));
        f.setUnitMeasure(text(etUnit));
        f.setWeight(text(etWeight));
        f.setQuantity(text(etQuantity));
        f.setMinQuantity(text(etMinQuantity));
        f.setBatch(text(etBatch));
        f.setSupplier(text(etSupplier));
        f.setExpiryDate(text(etExpiry));
        f.setActive(swActive.isChecked());
        return f;
    }

    private String text(TextInputEditText et) { return et.getText()!=null ? et.getText().toString().trim() : ""; }

    private void onSave() {
        ProductForm f = collectForm();
        viewModel.updateForm(f);
        viewModel.submit();
        // Foco no primeiro erro após validação
        viewModel.getErrors().observe(this, errors -> {
            if (errors.containsKey("barcode")) etBarcode.requestFocus();
            else if (errors.containsKey("name")) etName.requestFocus();
            else if (errors.containsKey("categoryId")) etCategory.requestFocus();
            else if (errors.containsKey("unitMeasure")) etUnit.requestFocus();
            else if (errors.containsKey("weight")) etWeight.requestFocus();
        });
    }

    private void searchOff() {
        String barcode = text(etBarcode);
        if (TextUtils.isEmpty(barcode)) { tvErrorBarcode.setText("Informe o código"); tvErrorBarcode.setVisibility(View.VISIBLE); return; }
        btnSearchOff.setEnabled(false);
        tvOffInfo.setVisibility(View.GONE);
        offRepo.fetchByBarcode(barcode, new OpenFoodFactsRepository.CallbackResult() {
            @Override public void onSuccess(OpenFoodFactsResponse.ProductData product) {
                runOnUiThread(() -> {
                    btnSearchOff.setEnabled(true);
                    ProductForm f = collectForm();
                    if (!TextUtils.isEmpty(product.getProductName())) f.setName(product.getProductName());
                    if (!TextUtils.isEmpty(product.getBrands())) f.setBrand(product.getBrands().split(",")[0].trim());
                    // quantity da OFF (ex: "500g") tenta extrair peso
                    if (!TextUtils.isEmpty(product.getQuantity())) {
                        String q = product.getQuantity().replaceAll("[^0-9.,]", "").replace(",", ".");
                        if (!q.isEmpty()) f.setWeight(q);
                    }
                    viewModel.updateForm(f);
                    tvOffInfo.setText("Preenchido via Open Food Facts — revise os campos");
                    tvOffInfo.setVisibility(View.VISIBLE);
                });
            }
            @Override public void onNotFound() {
                runOnUiThread(() -> {
                    btnSearchOff.setEnabled(true);
                    tvOffInfo.setText("Não encontrado na base pública — preencha manualmente");
                    tvOffInfo.setVisibility(View.VISIBLE);
                });
            }
            @Override public void onError(String msg) {
                runOnUiThread(() -> {
                    btnSearchOff.setEnabled(true);
                    tvOffInfo.setText(msg + " — você pode continuar manualmente");
                    tvOffInfo.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }
}