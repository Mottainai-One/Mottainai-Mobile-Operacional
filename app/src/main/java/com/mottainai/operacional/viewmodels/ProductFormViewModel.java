package com.mottainai.operacional.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mottainai.operacional.models.Product;
import com.mottainai.operacional.models.ProductForm;
import com.mottainai.operacional.models.ProductResponse;
import com.mottainai.operacional.models.ProductUpsertRequest;
import com.mottainai.operacional.repository.ProductRepository;
import com.mottainai.operacional.repository.MockProductRepository;
import com.mottainai.operacional.utils.Constants;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class ProductFormViewModel extends AndroidViewModel {

    private final ProductRepository repository;

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> saving = new MutableLiveData<>(false);
    private final MutableLiveData<ProductForm> form = new MutableLiveData<>(new ProductForm());
    private final MutableLiveData<Map<String,String>> errors = new MutableLiveData<>(new HashMap<>());
    private final MutableLiveData<String> submitError = new MutableLiveData<>();
    private final MutableLiveData<Product> submitSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> loadError = new MutableLiveData<>();

    private String productId; // null = novo
    private boolean submitting = false;

    public ProductFormViewModel(@NonNull Application application) {
        super(application);
        boolean useMock = Constants.USE_MOCK_REPOSITORY;
        repository = useMock ? new MockProductRepository(application) : new ProductRepository(application);
    }

    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<Boolean> getSaving() { return saving; }
    public LiveData<ProductForm> getForm() { return form; }
    public LiveData<Map<String,String>> getErrors() { return errors; }
    public LiveData<String> getSubmitError() { return submitError; }
    public LiveData<Product> getSubmitSuccess() { return submitSuccess; }
    public LiveData<String> getLoadError() { return loadError; }
    public String getProductId() { return productId; }
    public boolean isEditMode() { return productId != null; }

    public void initNew() {
        productId = null;
        ProductForm f = new ProductForm();
        form.setValue(f);
    }

    public void initEdit(String id) {
        productId = id;
        loadProduct(id);
    }

    private void loadProduct(String id) {
        loading.setValue(true);
        loadError.setValue(null);
        repository.fetchProductById(id, new ProductRepository.ProductCallback() {
            @Override public void onSuccess(Product p) {
                ProductForm f = new ProductForm();
                f.setBarcode(p.getSku());
                f.setName(p.getName());
                f.setBrand(p.getSupplier());
                // Campos de contrato: categoryId=1 default se não houver, unitMeasure=UN
                f.setCategoryId(1);
                f.setUnitMeasure("UN");
                f.setWeight("0");
                f.setActive(true);
                // Versão não exposta no mock; tentar extrair de ProductResponse via campo extra se houver
                form.postValue(f);
                loading.postValue(false);
            }
            @Override public void onError(String message) {
                loadError.postValue(message);
                loading.postValue(false);
            }
        });
    }

    public void updateForm(ProductForm f) { form.setValue(f); }

    public boolean validate() {
        ProductForm f = form.getValue();
        Map<String,String> err = new HashMap<>();
        if (f == null) { errors.setValue(err); return false; }
        if (f.getBarcode() == null || f.getBarcode().trim().isEmpty()) err.put("barcode", "Código obrigatório");
        else if (f.getBarcode().trim().length() > 30) err.put("barcode", "Máximo 30 caracteres");
        if (f.getName() == null || f.getName().trim().isEmpty()) err.put("name", "Nome obrigatório");
        else if (f.getName().trim().length() > 150) err.put("name", "Máximo 150 caracteres");
        if (f.getBrand() != null && f.getBrand().length() > 100) err.put("brand", "Máximo 100 caracteres");
        if (f.getCategoryId() == null) err.put("categoryId", "Categoria obrigatória");
        if (f.getUnitMeasure() == null || f.getUnitMeasure().trim().isEmpty()) err.put("unitMeasure", "Unidade obrigatória");
        else if (f.getUnitMeasure().trim().length() > 20) err.put("unitMeasure", "Máximo 20 caracteres");
        if (f.getWeight() == null || f.getWeight().trim().isEmpty()) err.put("weight", "Peso obrigatório");
        else {
            try {
                BigDecimal w = f.getWeightAsBigDecimal();
                if (w == null || w.compareTo(BigDecimal.ZERO) < 0) err.put("weight", "Peso inválido");
            } catch (Exception e) { err.put("weight", "Peso inválido"); }
        }
        // Campos pendentes (quantity etc.) validados localmente mas não enviados
        if (f.getQuantity() != null && !f.getQuantity().isEmpty()) {
            try { int q = Integer.parseInt(f.getQuantity()); if (q < 0) err.put("quantity", "Não pode ser negativo"); } catch (NumberFormatException e){ err.put("quantity","Número inválido"); }
        }
        if (f.getMinQuantity() != null && !f.getMinQuantity().isEmpty()) {
            try { int q = Integer.parseInt(f.getMinQuantity()); if (q < 0) err.put("minQuantity", "Não pode ser negativo"); } catch (NumberFormatException e){ err.put("minQuantity","Número inválido"); }
        }
        // validade: yyyy-MM-dd se preenchida
        if (f.getExpiryDate() != null && !f.getExpiryDate().trim().isEmpty()) {
            if (!f.getExpiryDate().matches("\\d{4}-\\d{2}-\\d{2}")) err.put("expiryDate","Formato yyyy-MM-dd");
        }
        errors.setValue(err);
        return err.isEmpty();
    }

    public void submit() {
        if (submitting) return;
        if (!validate()) return;
        ProductForm f = form.getValue();
        if (f == null) return;
        submitting = true;
        saving.setValue(true);
        submitError.setValue(null);
        ProductUpsertRequest req = f.toUpsertRequest();
        if (isEditMode()) {
            repository.updateProduct(productId, req, new ProductRepository.ProductCallback() {
                @Override public void onSuccess(Product product) {
                    submitting = false; saving.postValue(false); submitSuccess.postValue(product);
                }
                @Override public void onError(String message) {
                    submitting = false; saving.postValue(false);
                    if (message != null && message.contains("409")) submitError.postValue("Conflito: produto foi alterado. Recarregue.");
                    else submitError.postValue(message);
                }
            });
        } else {
            repository.createProduct(req, new ProductRepository.ProductCallback() {
                @Override public void onSuccess(Product product) {
                    submitting = false; saving.postValue(false); submitSuccess.postValue(product);
                }
                @Override public void onError(String message) {
                    submitting = false; saving.postValue(false); submitError.postValue(message);
                }
            });
        }
    }
}