package com.mottainai.operacional.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.mottainai.operacional.R;
import com.mottainai.operacional.models.DamageUiState;
import com.mottainai.operacional.viewmodels.DamageViewModel;

import java.io.File;

public class RegisterDamageActivity extends AppCompatActivity {

    private DamageViewModel viewModel;
    private MaterialAutoCompleteTextView actvReason;
    private com.google.android.material.textfield.TextInputEditText etQuantity, etNote;
    private android.widget.TextView tvProductName, tvProductSku, tvErrorReason, tvErrorQuantity, tvDamageError;
    private android.widget.ImageView ivPreview;
    private View progress;
    private com.google.android.material.button.MaterialButton btnSubmit;
    private Uri pendingCameraUri;

    private final androidx.activity.result.ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) launchCamera();
            });

    private final androidx.activity.result.ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && pendingCameraUri != null) {
                    viewModel.setPhotoUri(pendingCameraUri);
                    ivPreview.setVisibility(View.VISIBLE);
                    Glide.with(this).load(pendingCameraUri).into(ivPreview);
                }
            });

    private final androidx.activity.result.ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    viewModel.setPhotoUri(uri);
                    ivPreview.setVisibility(View.VISIBLE);
                    Glide.with(this).load(uri).into(ivPreview);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_damage);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String productId = getIntent().getStringExtra("product_id");
        if (productId == null) { finish(); return; }

        initViews();
        setupViewModel(productId);
        setupReasonDropdown();
        setupPhotoButtons();
        setupSubmit();
    }

    private void initViews() {
        tvProductName = findViewById(R.id.tv_product_name);
        tvProductSku = findViewById(R.id.tv_product_sku);
        actvReason = findViewById(R.id.actv_reason);
        etQuantity = findViewById(R.id.et_quantity);
        etNote = findViewById(R.id.et_note);
        tvErrorReason = findViewById(R.id.tv_error_reason);
        tvErrorQuantity = findViewById(R.id.tv_error_quantity);
        tvDamageError = findViewById(R.id.tv_damage_error);
        ivPreview = findViewById(R.id.iv_preview);
        progress = findViewById(R.id.progress_damage);
        btnSubmit = findViewById(R.id.btn_submit);
        findViewById(R.id.btn_cancel).setOnClickListener(v -> finish());
    }

    private void setupViewModel(String productId) {
        viewModel = new ViewModelProvider(this).get(DamageViewModel.class);
        viewModel.getUiState().observe(this, state -> {
            if (state instanceof DamageUiState.LoadingProduct) progress.setVisibility(View.VISIBLE);
            else progress.setVisibility(View.GONE);

            if (state instanceof DamageUiState.FormReady) {
                com.mottainai.operacional.models.Product p = ((DamageUiState.FormReady) state).product;
                tvProductName.setText(p.getName());
                tvProductSku.setText("Código: " + p.getSku());
            } else if (state instanceof DamageUiState.Error) {
                tvDamageError.setText(((DamageUiState.Error) state).message);
                tvDamageError.setVisibility(View.VISIBLE);
            } else if (state instanceof DamageUiState.Uploading) {
                progress.setVisibility(View.VISIBLE);
                btnSubmit.setEnabled(false);
            } else if (state instanceof DamageUiState.Submitting) {
                progress.setVisibility(View.VISIBLE);
                btnSubmit.setEnabled(false);
            } else if (state instanceof DamageUiState.Success) {
                Toast.makeText(this, "Avaria registrada", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                tvDamageError.setVisibility(View.GONE);
                btnSubmit.setEnabled(true);
            }
        });
        viewModel.getErrors().observe(this, errors -> {
            tvErrorReason.setVisibility(errors.containsKey("reason") ? View.VISIBLE : View.GONE);
            if (errors.containsKey("reason")) tvErrorReason.setText(errors.get("reason"));
            tvErrorQuantity.setVisibility(errors.containsKey("quantity") ? View.VISIBLE : View.GONE);
            if (errors.containsKey("quantity")) tvErrorQuantity.setText(errors.get("quantity"));
        });
        viewModel.getPhotoUri().observe(this, uri -> {
            if (uri != null) {
                ivPreview.setVisibility(View.VISIBLE);
                Glide.with(this).load(uri).into(ivPreview);
            }
        });
        viewModel.init(productId);
    }

    private void setupReasonDropdown() {
        String[] reasons = new String[]{"Vencido", "Embalagem danificada", "Quebra", "Contaminação", "Outro"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, reasons);
        actvReason.setAdapter(adapter);
    }

    private void setupPhotoButtons() {
        findViewById(R.id.btn_camera).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) launchCamera();
            else permissionLauncher.launch(Manifest.permission.CAMERA);
        });
        findViewById(R.id.btn_gallery).setOnClickListener(v -> galleryLauncher.launch("image/*"));
    }

    private void launchCamera() {
        try {
            File file = File.createTempFile("damage_" + System.currentTimeMillis(), ".jpg", getCacheDir());
            pendingCameraUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
            cameraLauncher.launch(pendingCameraUri);
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao abrir câmera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupSubmit() {
        btnSubmit.setOnClickListener(v -> {
            String reason = actvReason.getText() != null ? actvReason.getText().toString() : "";
            String qty = etQuantity.getText() != null ? etQuantity.getText().toString() : "";
            String note = etNote.getText() != null ? etNote.getText().toString() : "";
            viewModel.submit(reason, qty, note);
        });
    }

    @Override public boolean onSupportNavigateUp() { finish(); return true; }
}