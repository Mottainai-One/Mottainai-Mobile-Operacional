package com.mottainai.operacional.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ActivityInfo;
import android.hardware.camera2.CameraCharacteristics;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;
import com.mottainai.operacional.R;
import com.mottainai.operacional.activities.ProductDetailActivity;
import com.mottainai.operacional.activities.ProductFormActivity;
import com.mottainai.operacional.models.Product;
import com.mottainai.operacional.models.ScannerUiState;
import com.mottainai.operacional.scanner.BarcodeAnalyzer;
import com.mottainai.operacional.utils.RoleHelper;
import com.mottainai.operacional.utils.SessionManager;
import com.mottainai.operacional.viewmodels.ScannerViewModel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScannerFragment extends Fragment {

    private ScannerViewModel viewModel;
    private SessionManager sessionManager;

    // Views
    private PreviewView previewView;
    private ImageView viewfinderOverlay;
    private TextView tvScanHint;
    private View containerPermissionDenied;
    private TextView tvPermissionMessage;
    private com.google.android.material.button.MaterialButton btnRequestPermission;
    private com.google.android.material.button.MaterialButton btnOpenSettings;
    private View containerCameraUnavailable;
    private com.google.android.material.button.MaterialButton btnRetryCamera;
    private androidx.cardview.widget.CardView cardResult;
    private TextView tvResultTitle;
    private TextView tvProductName;
    private TextView tvProductSku;
    private TextView tvProductQuantity;
    private TextView tvProductMinQuantity;
    private TextView tvProductExpiry;
    private com.google.android.material.button.MaterialButton btnOpenDetail;
    private com.google.android.material.button.MaterialButton btnRegisterDamage;
    private com.google.android.material.button.MaterialButton btnScanAgain;
    private androidx.cardview.widget.CardView cardNotFound;
    private TextView tvNotFoundBarcode;
    private com.google.android.material.button.MaterialButton btnCreateProduct;
    private com.google.android.material.button.MaterialButton btnScanAgainNotFound;
    private androidx.cardview.widget.CardView cardError;
    private TextView tvErrorMessage;
    private com.google.android.material.button.MaterialButton btnRetry;
    private com.google.android.material.button.MaterialButton btnScanAgainError;
    private ProgressBar progressLookup;
    private com.google.android.material.floatingactionbutton.FloatingActionButton btnToggleTorch;
    private com.google.android.material.floatingactionbutton.FloatingActionButton btnCloseScanner;
    private com.google.android.material.floatingactionbutton.FloatingActionButton btnManualEntry;

    // Camera
    private ProcessCameraProvider cameraProvider;
    private androidx.camera.core.ImageAnalysis imageAnalysis;
    private boolean torchEnabled = false;
    private androidx.camera.core.Camera camera;
    private ExecutorService cameraExecutor;
    private boolean cameraStarting = false;
    private boolean cameraBound = false;
    private ToneGenerator toneGenerator;
    private Vibrator vibrator;

    // Permission
    private androidx.activity.result.ActivityResultLauncher<String> permissionLauncher;
    private androidx.activity.result.ActivityResultLauncher<Intent> settingsLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scanner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize tone generator and vibrator
        toneGenerator = new ToneGenerator(android.media.AudioManager.STREAM_NOTIFICATION, 100);
        vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);

        initViews(view);
        setupPermissionLauncher();
        setupSettingsLauncher();
        setupViewModel();
        setupClickListeners();
        checkCameraPermission();
    }

    private void initViews(View view) {
        previewView = view.findViewById(R.id.previewView);
        viewfinderOverlay = view.findViewById(R.id.viewfinderOverlay);
        tvScanHint = view.findViewById(R.id.tvScanHint);

        containerPermissionDenied = view.findViewById(R.id.containerPermissionDenied);
        tvPermissionMessage = view.findViewById(R.id.tvPermissionMessage);
        btnRequestPermission = view.findViewById(R.id.btnRequestPermission);
        btnOpenSettings = view.findViewById(R.id.btnOpenSettings);

        containerCameraUnavailable = view.findViewById(R.id.containerCameraUnavailable);
        btnRetryCamera = view.findViewById(R.id.btnRetryCamera);

        cardResult = view.findViewById(R.id.cardResult);
        tvResultTitle = view.findViewById(R.id.tvResultTitle);
        tvProductName = view.findViewById(R.id.tvProductName);
        tvProductSku = view.findViewById(R.id.tvProductSku);
        tvProductQuantity = view.findViewById(R.id.tvProductQuantity);
        tvProductMinQuantity = view.findViewById(R.id.tvProductMinQuantity);
        tvProductExpiry = view.findViewById(R.id.tvProductExpiry);
        btnOpenDetail = view.findViewById(R.id.btnOpenDetail);
        btnRegisterDamage = view.findViewById(R.id.btnRegisterDamage);
        btnScanAgain = view.findViewById(R.id.btnScanAgain);

        cardNotFound = view.findViewById(R.id.cardNotFound);
        tvNotFoundBarcode = view.findViewById(R.id.tvNotFoundBarcode);
        btnCreateProduct = view.findViewById(R.id.btnCreateProduct);
        btnScanAgainNotFound = view.findViewById(R.id.btnScanAgainNotFound);

        cardError = view.findViewById(R.id.cardError);
        tvErrorMessage = view.findViewById(R.id.tvErrorMessage);
        btnRetry = view.findViewById(R.id.btnRetry);
        btnScanAgainError = view.findViewById(R.id.btnScanAgainError);

        progressLookup = view.findViewById(R.id.progressLookup);
        btnToggleTorch = view.findViewById(R.id.btnToggleTorch);
        btnCloseScanner = view.findViewById(R.id.btnCloseScanner);
        btnManualEntry = view.findViewById(R.id.btnManualEntry);
    }

    private void setupPermissionLauncher() {
        permissionLauncher = registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) {
                        startCamera();
                    } else {
                        showPermissionDenied(!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA));
                    }
                }
        );
    }

    private void setupSettingsLauncher() {
        settingsLauncher = registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED) {
                        startCamera();
                    } else {
                        showPermissionDenied(true);
                    }
                }
        );
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ScannerViewModel.class);
        sessionManager = new SessionManager(requireContext());

        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            if (isAdded() && getView() != null) {
                renderState(state);
            }
        });
    }

    private void setupClickListeners() {
        btnRequestPermission.setOnClickListener(v -> requestCameraPermission());
        btnOpenSettings.setOnClickListener(v -> openAppSettings());
        btnRetryCamera.setOnClickListener(v -> checkCameraPermission());
        btnToggleTorch.setOnClickListener(v -> toggleTorch());
        btnCloseScanner.setOnClickListener(v -> requireActivity().onBackPressed());
        btnOpenDetail.setOnClickListener(v -> {
            ScannerUiState state = viewModel.getUiState().getValue();
            if (state instanceof ScannerUiState.Found) {
                navigateToDetail(((ScannerUiState.Found) state).product);
            }
        });
        btnRegisterDamage.setOnClickListener(v -> {
            ScannerUiState state = viewModel.getUiState().getValue();
            if (state instanceof ScannerUiState.Found) {
                openDamageRegistration(((ScannerUiState.Found) state).product);
            }
        });
        btnScanAgain.setOnClickListener(v -> viewModel.onRetryFromNotFound());
        btnCreateProduct.setOnClickListener(v -> {
            ScannerUiState state = viewModel.getUiState().getValue();
            if (state instanceof ScannerUiState.NotFound) {
                openNewProductForm(((ScannerUiState.NotFound) state).barcode);
            } else {
                openNewProductForm(null);
            }
        });
        btnScanAgainNotFound.setOnClickListener(v -> viewModel.onRetryFromNotFound());
        btnRetry.setOnClickListener(v -> viewModel.onRetryFromError());
        btnScanAgainError.setOnClickListener(v -> viewModel.onRetryFromError());
        btnManualEntry.setOnClickListener(v -> showManualEntryDialog());
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestCameraPermission();
        }
    }

    private void requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            // Show rationale dialog
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Permissão de Câmera")
                    .setMessage("O scanner precisa acessar a câmera para ler códigos de barras.")
                    .setPositiveButton("Permitir", (dialog, which) -> permissionLauncher.launch(Manifest.permission.CAMERA))
                    .setNegativeButton("Cancelar", null)
                    .show();
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void showPermissionDenied(boolean permanentlyDenied) {
        viewModel.getUiState().postValue(new com.mottainai.operacional.models.ScannerUiState.PermissionDenied(permanentlyDenied));
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + requireContext().getPackageName()));
        settingsLauncher.launch(intent);
    }

    private void startCamera() {
        if (cameraStarting || cameraBound) {
            Log.d("ScannerFragment", "startCamera skipped starting=" + cameraStarting + " bound=" + cameraBound);
            return;
        }

        if (previewView == null) {
            Log.e("ScannerFragment", "startCamera: previewView null");
            showCameraUnavailable("Preview não inicializado");
            return;
        }
        // Aguarda PreviewView anexar antes de obter surfaceProvider
        if (previewView.getSurfaceProvider() == null) {
            Log.d("ScannerFragment", "surfaceProvider null, adiando startCamera");
            previewView.post(() -> {
                if (isAdded() && getView() != null) startCamera();
            });
            return;
        }

        // Permissão já verificada pelo caller, mas revalida
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.e("ScannerFragment", "startCamera sem permissão");
            showCameraUnavailable("Permissão de câmera negada");
            return;
        }

        cameraStarting = true;
        Log.d("ScannerFragment", "Iniciando ProcessCameraProvider");

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                Log.d("ScannerFragment", "CameraProvider obtido, chamando bindPreview");
                bindPreview(cameraProvider);
            } catch (Exception e) {
                Log.e("ScannerFragment", "Falha ao obter CameraProvider", e);
                showCameraUnavailable("Falha ao obter câmera: " + e.getMessage());
            } finally {
                cameraStarting = false;
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
        try {
            if (!cameraProvider.hasCamera(cameraSelector)) {
                Log.e("ScannerFragment", "Câmera traseira não disponível no dispositivo");
                showCameraUnavailable("Câmera traseira não disponível");
                return;
            }
        } catch (Exception e) {
            Log.e("ScannerFragment", "Falha ao verificar câmera traseira", e);
            showCameraUnavailable("Erro ao verificar câmera: " + e.getMessage());
            return;
        }

        if (previewView == null || previewView.getSurfaceProvider() == null) {
            Log.e("ScannerFragment", "bindPreview: surfaceProvider null");
            showCameraUnavailable("Preview não pronto");
            return;
        }

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        if (cameraExecutor == null || cameraExecutor.isShutdown()) {
            cameraExecutor = Executors.newSingleThreadExecutor();
        }

        imageAnalysis = new ImageAnalysis.Builder()
                .setTargetRotation(getTargetRotation())
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        BarcodeAnalyzer analyzer = new BarcodeAnalyzer(requireContext(), barcode -> {
            if (isAdded() && getView() != null) {
                requireActivity().runOnUiThread(() -> viewModel.onBarcodeScanned(barcode));
            }
        });

        imageAnalysis.setAnalyzer(cameraExecutor, analyzer);

        try {
            Log.d("ScannerFragment", "bindToLifecycle com Preview+ImageAnalysis rotation=" + getTargetRotation());
            camera = cameraProvider.bindToLifecycle(getViewLifecycleOwner(),
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview, imageAnalysis);
            cameraBound = true;
            Log.d("ScannerFragment", "Câmera vinculada com sucesso");
            viewModel.getUiState().postValue(ScannerUiState.CameraReady.INSTANCE);
            btnToggleTorch.setVisibility(View.VISIBLE);
        } catch (IllegalArgumentException e) {
            Log.e("ScannerFragment", "bind falhou - câmera ocupada ou argumento inválido", e);
            showCameraUnavailable("Câmera ocupada por outro app");
        } catch (Exception e) {
            Log.e("ScannerFragment", "Falha ao fazer bind da câmera", e);
            showCameraUnavailable("Falha ao iniciar câmera: " + e.getMessage());
        }
    }

    private int getTargetRotation() {
        if (previewView.getDisplay() != null) {
            return previewView.getDisplay().getRotation();
        }
        return Surface.ROTATION_0;
    }

    private void showCameraUnavailable() {
        showCameraUnavailable("Câmera indisponível");
    }

    private void showCameraUnavailable(String reason) {
        Log.e("ScannerFragment", "showCameraUnavailable: " + reason);
        viewModel.getUiState().postValue(new ScannerUiState.Error(reason, false, 0));
    }

    private void renderState(ScannerUiState state) {
        if (!isAdded() || getView() == null) {
            return;
        }
        hideAllStates();

        if (state instanceof ScannerUiState.Idle) {
            showScanningUI();
        } else if (state instanceof ScannerUiState.RequestingPermission) {
            // Handled by permission flow
        } else if (state instanceof ScannerUiState.PermissionDenied) {
            ScannerUiState.PermissionDenied pd = (ScannerUiState.PermissionDenied) state;
            tvPermissionMessage.setText(pd.permanentlyDenied
                    ? "Permissão de câmera negada permanentemente. Habilite nas configurações."
                    : "Permissão de câmera necessária para escanear códigos de barras.");
            containerPermissionDenied.setVisibility(View.VISIBLE);
        } else if (state instanceof ScannerUiState.CameraReady) {
            showScanningUI();
        } else if (state instanceof ScannerUiState.Scanning) {
            showScanningUI();
        } else if (state instanceof ScannerUiState.LookingUp) {
            ScannerUiState.LookingUp lu = (ScannerUiState.LookingUp) state;
            progressLookup.setVisibility(View.VISIBLE);
        } else if (state instanceof ScannerUiState.Found) {
            ScannerUiState.Found f = (ScannerUiState.Found) state;
            showFoundState(f.product);
        } else if (state instanceof ScannerUiState.NotFound) {
            ScannerUiState.NotFound nf = (ScannerUiState.NotFound) state;
            showNotFoundState(nf.barcode);
        } else if (state instanceof ScannerUiState.Error) {
            ScannerUiState.Error e = (ScannerUiState.Error) state;
            showErrorState(e.message);
        }
    }

    private void hideAllStates() {
        previewView.setVisibility(View.VISIBLE);
        viewfinderOverlay.setVisibility(View.VISIBLE);
        tvScanHint.setVisibility(View.VISIBLE);
        containerPermissionDenied.setVisibility(View.GONE);
        containerCameraUnavailable.setVisibility(View.GONE);
        cardResult.setVisibility(View.GONE);
        cardNotFound.setVisibility(View.GONE);
        cardError.setVisibility(View.GONE);
        progressLookup.setVisibility(View.GONE);
        btnToggleTorch.setVisibility(View.GONE);
        if (btnManualEntry != null) btnManualEntry.setVisibility(View.VISIBLE);
    }

    private void showScanningUI() {
        previewView.setVisibility(View.VISIBLE);
        viewfinderOverlay.setVisibility(View.VISIBLE);
        tvScanHint.setVisibility(View.VISIBLE);
        btnToggleTorch.setVisibility(View.VISIBLE);
    }

    private android.os.Handler foundHandler = new android.os.Handler(Looper.getMainLooper());
    private Runnable foundAutoDismiss;

    private void showFoundState(Product product) {
        progressLookup.setVisibility(View.GONE);
        cardResult.setVisibility(View.VISIBLE);
        btnToggleTorch.setVisibility(View.GONE);

        // Play beep and vibrate on successful scan
        playBeepAndVibrate();

        tvResultTitle.setText("Produto Encontrado");
        tvProductName.setText(product.getName());
        tvProductSku.setText("Código: " + (product.getSku() != null ? product.getSku() : "—"));
        tvProductQuantity.setText("Quantidade: " + product.getQuantity() + " (contrato pendente)");
        tvProductMinQuantity.setText("Mínimo: " + product.getMinQuantity() + " (contrato pendente)");
        tvProductExpiry.setText("Validade: " + (product.getExpiryDate() != null ? product.getExpiryDate() : "— (pendente)"));

        btnOpenDetail.setOnClickListener(v -> {
            if (foundAutoDismiss != null) foundHandler.removeCallbacks(foundAutoDismiss);
            navigateToDetail(product);
        });
        btnRegisterDamage.setOnClickListener(v -> openDamageRegistration(product));
        btnScanAgain.setOnClickListener(v -> {
            if (foundAutoDismiss != null) foundHandler.removeCallbacks(foundAutoDismiss);
            viewModel.onRetryFromNotFound();
        });

        // Mantém o popup por pelo menos 10s; usuário pode escanear novamente antes via botão
        if (foundAutoDismiss != null) foundHandler.removeCallbacks(foundAutoDismiss);
        foundAutoDismiss = () -> {
            if (isAdded() && viewModel.getUiState().getValue() instanceof ScannerUiState.Found) {
                viewModel.onRetryFromNotFound();
            }
        };
        foundHandler.postDelayed(foundAutoDismiss, 10000);
    }

    private void showNotFoundState(String barcode) {
        progressLookup.setVisibility(View.GONE);
        cardNotFound.setVisibility(View.VISIBLE);
        btnToggleTorch.setVisibility(View.GONE);

        tvNotFoundBarcode.setText("Código: " + barcode);

        btnCreateProduct.setOnClickListener(v -> openNewProductForm(barcode));
        btnScanAgainNotFound.setOnClickListener(v -> viewModel.onRetryFromNotFound());
    }

    private void showErrorState(String message) {
        progressLookup.setVisibility(View.GONE);
        cardError.setVisibility(View.VISIBLE);
        btnToggleTorch.setVisibility(View.GONE);

        tvErrorMessage.setText(message);
        btnRetry.setOnClickListener(v -> viewModel.onRetryFromError());
        btnScanAgainError.setOnClickListener(v -> viewModel.onRetryFromError());
    }

    private void navigateToDetail(Product product) {
        NavController navController = Navigation.findNavController(requireView());
        android.os.Bundle args = new android.os.Bundle();
        args.putString("product_id", product.getId());
        args.putBoolean("is_new_product", false);
        Navigation.findNavController(requireView()).navigate(R.id.productDetailActivity, args);
    }

    private void openDamageRegistration(Product product) {
        // TODO MOBILE-06: navegar para registro de avaria
        Toast.makeText(requireContext(), "Registrar avaria — pendente (MOBILE-06)", Toast.LENGTH_SHORT).show();
    }

    private void openNewProductForm() {
        openNewProductForm(null);
    }

    private void openNewProductForm(String barcode) {
        boolean canCreate = com.mottainai.operacional.utils.RoleHelper.canRegisterProduct(sessionManager.getRole());
        if (!canCreate) {
            Toast.makeText(requireContext(), "Sem permissão para criar produto", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(requireContext(), ProductFormActivity.class);
        if (barcode != null) {
            intent.putExtra("barcode", barcode);
        }
        startActivity(intent);
    }

    private void openDamageRegistration() {
        Toast.makeText(requireContext(), "Registrar avaria — pendente (MOBILE-06)", Toast.LENGTH_SHORT).show();
    }

    private void toggleTorch() {
        if (camera == null) {
            Toast.makeText(requireContext(), "Câmera ainda não está pronta", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!camera.getCameraInfo().hasFlashUnit()) {
            Toast.makeText(requireContext(), "Este dispositivo não possui flash disponível", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean newState = !torchEnabled;

        camera.getCameraControl()
                .enableTorch(newState)
                .addListener(() -> {
                    torchEnabled = newState;
                    btnToggleTorch.setImageResource(
                            torchEnabled
                                    ? R.drawable.ic_flash_on
                                    : R.drawable.ic_flash_off
                    );
                }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void playBeepAndVibrate() {
        if (toneGenerator != null) {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 100);
        }
        if (vibrator != null && vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(android.os.VibrationEffect.createOneShot(100, -1));
            } else {
                vibrator.vibrate(100);
            }
        }
    }

    private void stopCamera() {
        if (imageAnalysis != null) {
            imageAnalysis.clearAnalyzer();
            imageAnalysis = null;
        }

        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }

        camera = null;

        if (cameraExecutor != null) {
            cameraExecutor.shutdownNow();
            cameraExecutor = null;
        }

        cameraStarting = false;
        cameraBound = false;
    }

    private void showManualEntryDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Digitar código manualmente");
        builder.setMessage("Digite o código de barras ou SKU do produto:");

        final androidx.appcompat.widget.AppCompatEditText input = new androidx.appcompat.widget.AppCompatEditText(requireContext());
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Buscar", (dialog, which) -> {
            String barcode = input.getText().toString().trim();
            if (!barcode.isEmpty()) {
                viewModel.onBarcodeScanned(barcode);
            } else {
                // Keep dialog open if empty
                Toast.makeText(requireContext(), "Digite um código válido", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.setCancelable(false);
        builder.show();
    }

    @Override
    public void onPause() {
        super.onPause();
        viewModel.onPause();
        stopCamera();
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.onResume();
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            if (!cameraBound) {
                // Use post to ensure PreviewView is attached
                previewView.post(this::startCamera);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopCamera();
    }
}