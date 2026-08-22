package com.mottainai.operacional.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.mottainai.operacional.models.Product;
import com.mottainai.operacional.repository.MockProductRepository;
import com.mottainai.operacional.repository.ProductRepository;

public class ProductDetailViewModel extends AndroidViewModel {

    private final ProductRepository productRepository;

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Product> product = new MutableLiveData<>();

    public ProductDetailViewModel(@NonNull Application application) {
        super(application);
        boolean useMock = true; // TODO: trocar para false quando backend estiver pronto
        if (useMock) {
            productRepository = new MockProductRepository(application);
        } else {
            productRepository = new ProductRepository(application);
        }
    }

    public MutableLiveData<Boolean> getLoading() {
        return loading;
    }

    public MutableLiveData<String> getError() {
        return error;
    }

    public MutableLiveData<Product> getProduct() {
        return product;
    }

    public void loadProduct(String productId) {
        loading.setValue(true);
        productRepository.fetchProductById(productId, new ProductRepository.ProductCallback() {
            @Override
            public void onSuccess(Product loadedProduct) {
                product.postValue(loadedProduct);
                loading.postValue(false);
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
                loading.postValue(false);
            }
        });
    }
}