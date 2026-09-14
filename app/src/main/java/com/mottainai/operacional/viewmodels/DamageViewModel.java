package com.mottainai.operacional.viewmodels;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mottainai.operacional.models.Damage;
import com.mottainai.operacional.models.DamageRequest;
import com.mottainai.operacional.models.DamageUiState;
import com.mottainai.operacional.models.Product;
import com.mottainai.operacional.repository.DamageRepository;
import com.mottainai.operacional.repository.ProductRepository;
import com.mottainai.operacional.repository.MockProductRepository;
import com.mottainai.operacional.storage.PhotoStorageRepository;
import com.mottainai.operacional.utils.Constants;
import com.mottainai.operacional.utils.SessionManager;

import java.util.HashMap;
import java.util.Map;

public class DamageViewModel extends AndroidViewModel {

    private final ProductRepository productRepository;
    private final DamageRepository damageRepository;
    private final PhotoStorageRepository photoRepository;
    private final SessionManager sessionManager;

    private final MutableLiveData<DamageUiState> uiState = new MutableLiveData<>(new DamageUiState.LoadingProduct());
    private final MutableLiveData<Map<String,String>> errors = new MutableLiveData<>(new HashMap<>());
    private final MutableLiveData<Uri> photoUri = new MutableLiveData<>();

    private String productId;
    private Product product;
    private boolean submitting = false;

    public DamageViewModel(@NonNull Application application) {
        super(application);
        boolean useMock = Constants.USE_MOCK_REPOSITORY;
        productRepository = useMock ? new MockProductRepository(application) : new ProductRepository(application);
        damageRepository = new DamageRepository(application);
        photoRepository = new PhotoStorageRepository();
        sessionManager = new SessionManager(application);
    }

    public LiveData<DamageUiState> getUiState() { return uiState; }
    public LiveData<Map<String,String>> getErrors() { return errors; }
    public LiveData<Uri> getPhotoUri() { return photoUri; }
    public void setPhotoUri(Uri uri) { photoUri.setValue(uri); }

    public void init(String productId) {
        this.productId = productId;
        uiState.setValue(new DamageUiState.LoadingProduct());
        productRepository.fetchProductById(productId, new ProductRepository.ProductCallback() {
            @Override public void onSuccess(Product p) { product = p; uiState.postValue(new DamageUiState.FormReady(p)); }
            @Override public void onError(String message) { uiState.postValue(new DamageUiState.Error(message)); }
        });
    }

    public Product getProduct() { return product; }

    public boolean validate(String reason, String quantity, String note) {
        Map<String,String> err = new HashMap<>();
        if (reason == null || reason.trim().isEmpty()) err.put("reason", "Motivo obrigatório");
        if (quantity == null || quantity.trim().isEmpty()) err.put("quantity", "Quantidade obrigatória");
        else {
            try {
                int q = Integer.parseInt(quantity.trim());
                if (q <= 0) err.put("quantity", "Deve ser maior que zero");
            } catch (NumberFormatException e) { err.put("quantity", "Número inválido"); }
        }
        if (note != null && note.length() > 500) err.put("note", "Máximo 500 caracteres");
        errors.setValue(err);
        return err.isEmpty();
    }

    public void submit(String reason, String quantityStr, String note) {
        if (submitting) return;
        if (!validate(reason, quantityStr, note)) return;
        if (product == null) { uiState.setValue(new DamageUiState.Error("Produto não carregado")); return; }
        submitting = true;
        uiState.setValue(new DamageUiState.Submitting());
        int qty = Integer.parseInt(quantityStr.trim());
        String cleanNote = note != null && !note.trim().isEmpty() ? note.trim() : null;
        Uri uri = photoUri.getValue();
        String storeId = sessionManager.getStoreId();

        // 1) upload foto se houver
        if (uri != null) {
            uiState.setValue(new DamageUiState.Uploading());
            photoRepository.uploadPhoto(getApplication().getApplicationContext(), uri, storeId, productId, new PhotoStorageRepository.PhotoCallback() {
                @Override public void onSuccess(String photoUrl) { postDamage(reason, qty, cleanNote, photoUrl); }
                @Override public void onError(String message) { submitting=false; uiState.postValue(new DamageUiState.Error("Falha no upload: " + message)); }
            });
        } else {
            postDamage(reason, qty, cleanNote, null);
        }
    }

    private void postDamage(String reason, int quantity, String note, String photoUrl) {
        DamageRequest req = new DamageRequest(productId, reason, quantity, note, photoUrl);
        damageRepository.createDamage(req, new DamageRepository.DamageCallback() {
            @Override public void onSuccess(Damage damage) { submitting=false; uiState.postValue(new DamageUiState.Success(damage)); }
            @Override public void onError(String message) { submitting=false; uiState.postValue(new DamageUiState.Error(message)); }
        });
    }
}