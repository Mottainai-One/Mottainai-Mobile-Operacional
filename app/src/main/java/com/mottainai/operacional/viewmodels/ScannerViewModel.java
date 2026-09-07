package com.mottainai.operacional.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.mottainai.operacional.models.Product;
import com.mottainai.operacional.models.ScannerUiState;
import com.mottainai.operacional.repository.ProductRepository;
import com.mottainai.operacional.repository.MockProductRepository;
import com.mottainai.operacional.utils.Constants;

import java.util.Locale;

public class ScannerViewModel extends AndroidViewModel {

    private final ProductRepository repository;
    private boolean isProcessing = false;
    private String lastScannedBarcode = null;
    private long lastScanTime = 0;
    private static final long SCAN_DEBOUNCE_MS = 2000; // 2 seconds debounce

    private final MutableLiveData<ScannerUiState> uiState = new MutableLiveData<>(ScannerUiState.Idle.INSTANCE);

    public ScannerViewModel(@NonNull Application application) {
        super(application);
        boolean useMock = Constants.USE_MOCK_REPOSITORY;
        repository = useMock
                ? new MockProductRepository(application)
                : new ProductRepository(application);
    }

    public MutableLiveData<ScannerUiState> getUiState() {
        return uiState;
    }

    public void onBarcodeScanned(String barcode) {
        String normalized = barcode == null ? "" : barcode.trim();
        if (normalized.isEmpty()) {
            return;
        }

        // Debounce: prevent duplicate scans within time window.
        if (isProcessing) {
            return;
        }
        long now = System.currentTimeMillis();
        if (normalized.equals(lastScannedBarcode)
                && (now - lastScanTime) < SCAN_DEBOUNCE_MS) {
            return;
        }

        lastScannedBarcode = normalized;
        lastScanTime = now;
        isProcessing = true;
        uiState.postValue(new ScannerUiState.LookingUp(normalized));

        // MockProductRepository e ProductRepository compartilham este contrato.
        repository.fetchProductByBarcode(normalized, new ProductRepository.ProductCallback() {
            @Override
            public void onSuccess(Product product) {
                isProcessing = false;
                if (product != null) {
                    uiState.postValue(new ScannerUiState.Found(product));
                } else {
                    uiState.postValue(new ScannerUiState.NotFound(normalized));
                }
            }

            @Override
            public void onError(String message) {
                isProcessing = false;
                // Check if it's a 404
                String normalizedMessage = message == null
                        ? ""
                        : message.toLowerCase(Locale.ROOT);
                if (normalizedMessage.contains("404")
                        || normalizedMessage.contains("não encontrado")
                        || normalizedMessage.contains("nao encontrado")) {
                    uiState.postValue(new ScannerUiState.NotFound(normalized));
                } else {
                    uiState.postValue(new ScannerUiState.Error(message, true, 0));
                }
            }
        });
    }

    public void onProductHandled() {
        // Called when user has seen the result and wants to scan again
        isProcessing = false;
        lastScannedBarcode = null;
        lastScanTime = 0;
        uiState.postValue(ScannerUiState.Idle.INSTANCE);
    }

    public void onRetryFromNotFound() {
        isProcessing = false;
        lastScannedBarcode = null;
        lastScanTime = 0;
        uiState.postValue(ScannerUiState.Idle.INSTANCE);
    }

    public void onRetryFromError() {
        isProcessing = false;
        lastScannedBarcode = null;
        lastScanTime = 0;
        uiState.postValue(ScannerUiState.Idle.INSTANCE);
    }

    public void clearProcessingState() {
        isProcessing = false;
        lastScannedBarcode = null;
        lastScanTime = 0;
    }

    public void onPause() {
        // Release processing lock when fragment pauses
        isProcessing = false;
    }

    public void onResume() {
        // Resume scanning if we were in scanning state
        ScannerUiState current = uiState.getValue();
        if (current instanceof ScannerUiState.CameraReady || current instanceof ScannerUiState.Scanning) {
            // Continue scanning
        }
    }
}
