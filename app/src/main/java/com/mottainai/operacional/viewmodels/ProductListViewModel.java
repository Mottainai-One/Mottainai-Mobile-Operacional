package com.mottainai.operacional.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.mottainai.operacional.models.Product;
import com.mottainai.operacional.repository.MockProductRepository;
import com.mottainai.operacional.repository.ProductRepository;
import com.mottainai.operacional.utils.Constants;

import java.util.List;

/**
 * ViewModel para lista de produtos.
 * O modo mock é controlado por Constants.USE_MOCK_REPOSITORY.
 * Em produção, deve ser false para usar a API real.
 */
public class ProductListViewModel extends AndroidViewModel {

    private final ProductRepository productRepository;

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<List<Product>> products = new MutableLiveData<>();

    public ProductListViewModel(@NonNull Application application) {
        super(application);
        // Modo mock controlado por constante - alterar para false quando API estiver pronta
        boolean useMock = Constants.USE_MOCK_REPOSITORY;
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

    public MutableLiveData<List<Product>> getProducts() {
        return products;
    }

    public void loadProducts(String storeId) {
        loading.setValue(true);
        productRepository.fetchProducts(storeId, new ProductRepository.ProductListCallback() {
            @Override
            public void onSuccess(List<Product> productList) {
                products.postValue(productList);
                loading.postValue(false);
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
                loading.postValue(false);
            }
        });
    }

    public void refreshProducts(String storeId) {
        loadProducts(storeId);
    }
}