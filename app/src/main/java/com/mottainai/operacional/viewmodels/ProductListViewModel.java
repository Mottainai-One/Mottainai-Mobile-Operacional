package com.mottainai.operacional.viewmodels;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.mottainai.operacional.models.Product;
import com.mottainai.operacional.repository.MockProductRepository;
import com.mottainai.operacional.repository.ProductRepository;
import com.mottainai.operacional.R;
import com.mottainai.operacional.utils.Constants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel para lista de produtos.
 * Loads the operational product API and keeps a labeled offline snapshot on network loss.
 */
public class ProductListViewModel extends AndroidViewModel {

    private final ProductRepository productRepository;

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<List<Product>> products = new MutableLiveData<>();
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> offlineSnapshot = new MutableLiveData<>(false);
    private final MutableLiveData<Long> lastSyncAt = new MutableLiveData<>();
    private final Gson gson = new Gson();

    public ProductListViewModel(@NonNull Application application) {
        super(application);
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

    public MutableLiveData<String> getSearchQuery() {
        return searchQuery;
    }

    public MutableLiveData<Boolean> getOfflineSnapshot() {
        return offlineSnapshot;
    }

    public MutableLiveData<Long> getLastSyncAt() {
        return lastSyncAt;
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query == null ? "" : query);
    }

    public void loadProducts(String storeId) {
        loading.setValue(true);
        error.setValue(null);
        productRepository.fetchProducts(storeId, new ProductRepository.ProductListCallback() {
            @Override
            public void onSuccess(List<Product> productList) {
                if (!Constants.USE_MOCK_REPOSITORY && productList != null) {
                    saveOfflineCopy(storeId, productList);
                    lastSyncAt.postValue(System.currentTimeMillis());
                }
                offlineSnapshot.postValue(false);
                products.postValue(productList);
                error.postValue(null);
                loading.postValue(false);
            }

            @Override
            public void onError(String message) {
                if (isNetworkError(message)) {
                    List<Product> cachedProducts = readOfflineCopy(storeId);
                    if (cachedProducts != null && !cachedProducts.isEmpty()) {
                        products.postValue(cachedProducts);
                        offlineSnapshot.postValue(true);
                        lastSyncAt.postValue(readOfflineCopyTimestamp(storeId));
                        error.postValue(null);
                        loading.postValue(false);
                        return;
                    }
                    message = getApplication().getString(R.string.op_offline_not_received);
                }
                error.postValue(message);
                loading.postValue(false);
            }
        });
    }

    public void refreshProducts(String storeId) {
        loadProducts(storeId);
    }

    private boolean isNetworkError(String message) {
        return message != null && message.startsWith("Erro de rede:");
    }

    private void saveOfflineCopy(String storeId, List<Product> productList) {
        if (storeId == null || storeId.trim().isEmpty()) return;
        offlinePreferences().edit()
                .putString(cacheKey(storeId), gson.toJson(productList))
                .putLong(timestampKey(storeId), System.currentTimeMillis())
                .apply();
    }

    private List<Product> readOfflineCopy(String storeId) {
        if (storeId == null || storeId.trim().isEmpty()) return null;
        String json = offlinePreferences().getString(cacheKey(storeId), null);
        if (json == null || json.trim().isEmpty()) return null;
        try {
            Type listType = new TypeToken<ArrayList<Product>>() {}.getType();
            return gson.fromJson(json, listType);
        } catch (RuntimeException ignored) {
            offlinePreferences().edit().remove(cacheKey(storeId)).remove(timestampKey(storeId)).apply();
            return null;
        }
    }

    private long readOfflineCopyTimestamp(String storeId) {
        if (storeId == null || storeId.trim().isEmpty()) return 0L;
        return offlinePreferences().getLong(timestampKey(storeId), 0L);
    }

    private SharedPreferences offlinePreferences() {
        return getApplication().getSharedPreferences("operational_product_cache", 0);
    }

    private String cacheKey(String storeId) {
        return "products_" + storeId;
    }

    private String timestampKey(String storeId) {
        return "products_updated_at_" + storeId;
    }
}
