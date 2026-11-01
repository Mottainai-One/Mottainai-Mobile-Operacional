package com.mottainai.operacional.repository;

import android.app.Application;
import com.mottainai.operacional.models.ApiBatchResponse;
import com.mottainai.operacional.models.ApiInventoryCountCreateRequest;
import com.mottainai.operacional.models.ApiInventoryCountItemRequest;
import com.mottainai.operacional.models.ApiInventoryCountItemUpdateRequest;
import com.mottainai.operacional.models.ApiInventoryCountResponse;
import com.mottainai.operacional.models.ApiInventoryResponse;
import com.mottainai.operacional.models.InventoryItem;
import com.mottainai.operacional.models.InventorySession;
import com.mottainai.operacional.models.PageResponse;
import com.mottainai.operacional.models.ProductResponse;
import com.mottainai.operacional.network.ApiService;
import com.mottainai.operacional.network.RetrofitClient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Store scoped inventory counts persisted by the SQL operational API. */
public class InventoryRepository {
    public interface CallbackResult { void onSuccess(InventorySession session); void onError(String message); }
    public interface CatalogCallback { void onSuccess(List<com.mottainai.operacional.models.Product> products); void onError(String message); }
    private final ApiService api;
    private final Map<String, InventorySession> sessions = new HashMap<>();
    private final Map<String, String> countIdsByStore = new HashMap<>();

    public InventoryRepository(Application app) { api = RetrofitClient.getClient(app).create(ApiService.class); }

    /** Loads product rows and their store-specific stock balances from the SQL API. */
    public void loadCatalog(String storeId, CatalogCallback callback) {
        final Integer numericStore;
        try { numericStore = Integer.valueOf(storeId); }
        catch (Exception error) { callback.onError("O ID da loja operacional é inválido."); return; }
        api.getInventory(numericStore).enqueue(new Callback<List<ApiInventoryResponse>>() {
            @Override public void onResponse(Call<List<ApiInventoryResponse>> call, Response<List<ApiInventoryResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) { callback.onError("API de inventário respondeu HTTP " + response.code() + "."); return; }
                api.getBatches(numericStore).enqueue(new Callback<List<ApiBatchResponse>>() {
                    @Override public void onResponse(Call<List<ApiBatchResponse>> call, Response<List<ApiBatchResponse>> batchesResponse) {
                        if (!batchesResponse.isSuccessful() || batchesResponse.body() == null) { callback.onError("API de lotes respondeu HTTP " + batchesResponse.code() + "."); return; }
                        Map<Integer, Integer> productByBatch = new HashMap<>();
                        Map<Integer, ApiBatchResponse> batchById = new HashMap<>();
                        for (ApiBatchResponse batch : batchesResponse.body()) if (batch != null && batch.id != null) {
                            productByBatch.put(batch.id, batch.productId);
                            batchById.put(batch.id, batch);
                        }
                        api.getProductsPaged(0, 200).enqueue(new Callback<PageResponse<ProductResponse>>() {
                            @Override public void onResponse(Call<PageResponse<ProductResponse>> call, Response<PageResponse<ProductResponse>> productsResponse) {
                                if (!productsResponse.isSuccessful() || productsResponse.body() == null || productsResponse.body().getContent() == null) { callback.onError("API de produtos respondeu HTTP " + productsResponse.code() + "."); return; }
                                Map<Integer, Double> quantities = new HashMap<>();
                                Map<Integer, Double> minimums = new HashMap<>();
                                Map<Integer, ApiBatchResponse> firstBatch = new HashMap<>();
                                for (ApiInventoryResponse row : response.body()) {
                                    if (row == null || row.batchId == null) continue;
                                    Integer productId = productByBatch.get(row.batchId);
                                    if (productId == null) continue;
                                    quantities.merge(productId, row.currentQuantity == null ? 0d : row.currentQuantity, Double::sum);
                                    minimums.merge(productId, row.minimumQuantity == null ? 0d : row.minimumQuantity, Double::sum);
                                    firstBatch.putIfAbsent(productId, batchById.get(row.batchId));
                                }
                                List<com.mottainai.operacional.models.Product> result = new ArrayList<>();
                                for (ProductResponse dto : productsResponse.body().getContent()) {
                                    if (dto == null || dto.getId() == null) continue;
                                    com.mottainai.operacional.models.Product product = new com.mottainai.operacional.models.Product();
                                    product.setId(String.valueOf(dto.getId()));
                                    product.setName(dto.getName());
                                    product.setSku(dto.getBarcode());
                                    product.setImageUrl(dto.getImageUrl());
                                    product.setSupplier(dto.getBrand());
                                    product.setUnitMeasure(dto.getUnitMeasure());
                                    product.setCategoryId(dto.getCategoryId());
                                    product.setStoreId(storeId);
                                    product.setQuantity((int)Math.round(quantities.getOrDefault(dto.getId(), 0d)));
                                    product.setMinQuantity((int)Math.round(minimums.getOrDefault(dto.getId(), 0d)));
                                    ApiBatchResponse batch = firstBatch.get(dto.getId());
                                    if (batch != null) { product.setBatch(batch.batchCode); product.setExpiryDate(batch.expirationDate); }
                                    result.add(product);
                                }
                                callback.onSuccess(result);
                            }
                            @Override public void onFailure(Call<PageResponse<ProductResponse>> call, Throwable error) { callback.onError(networkError(error)); }
                        });
                    }
                    @Override public void onFailure(Call<List<ApiBatchResponse>> call, Throwable error) { callback.onError(networkError(error)); }
                });
            }
            @Override public void onFailure(Call<List<ApiInventoryResponse>> call, Throwable error) { callback.onError(networkError(error)); }
        });
    }

    public void load(String storeId, CallbackResult callback) {
        api.getInventoryCounts().enqueue(new Callback<List<ApiInventoryCountResponse>>() {
            @Override public void onResponse(Call<List<ApiInventoryCountResponse>> call, Response<List<ApiInventoryCountResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) { error(callback, response.code()); return; }
                ApiInventoryCountResponse active = null;
                for (ApiInventoryCountResponse item : response.body()) {
                    if (item != null && "IN_PROGRESS".equalsIgnoreCase(item.status)) { active = item; break; }
                }
                if (active == null) { sessions.remove(storeId); countIdsByStore.remove(storeId); callback.onSuccess(null); return; }
                loadCountDetails(storeId, String.valueOf(active.id), callback);
            }
            @Override public void onFailure(Call<List<ApiInventoryCountResponse>> call, Throwable error) { callback.onError(networkError(error)); }
        });
    }

    public void start(String storeId, String startedBy, CallbackResult callback) {
        load(storeId, new CallbackResult() {
            @Override public void onSuccess(InventorySession active) {
                if (active != null) { callback.onSuccess(active); return; }
                api.createInventoryCount(new ApiInventoryCountCreateRequest("Contagem iniciada pelo app operacional"))
                        .enqueue(new Callback<ApiInventoryCountResponse>() {
                    @Override public void onResponse(Call<ApiInventoryCountResponse> call, Response<ApiInventoryCountResponse> response) {
                        if (!response.isSuccessful() || response.body() == null || response.body().id == null) { error(callback, response.code()); return; }
                        loadInventorySnapshot(storeId, startedBy, response.body(), callback);
                    }
                    @Override public void onFailure(Call<ApiInventoryCountResponse> call, Throwable error) { callback.onError(networkError(error)); }
                });
            }
            @Override public void onError(String message) { callback.onError(message); }
        });
    }

    private void loadCountDetails(String storeId, String countId, CallbackResult callback) {
        api.getInventoryCount(countId).enqueue(new Callback<ApiInventoryCountResponse>() {
            @Override public void onResponse(Call<ApiInventoryCountResponse> call, Response<ApiInventoryCountResponse> response) {
                if (!response.isSuccessful() || response.body() == null) { error(callback, response.code()); return; }
                loadInventorySnapshot(storeId, "Colaborador #" + response.body().employeeId, response.body(), callback);
            }
            @Override public void onFailure(Call<ApiInventoryCountResponse> call, Throwable error) { callback.onError(networkError(error)); }
        });
    }

    private void loadInventorySnapshot(String storeId, String startedBy, ApiInventoryCountResponse count, CallbackResult callback) {
        final Integer numericStore;
        try { numericStore = Integer.valueOf(storeId); }
        catch (Exception error) { callback.onError("O ID da loja operacional é inválido."); return; }
        api.getInventory(numericStore).enqueue(new Callback<List<ApiInventoryResponse>>() {
            @Override public void onResponse(Call<List<ApiInventoryResponse>> call, Response<List<ApiInventoryResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) { error(callback, response.code()); return; }
                List<ApiInventoryResponse> inventory = response.body();
                api.getBatches(numericStore).enqueue(new Callback<List<ApiBatchResponse>>() {
                    @Override public void onResponse(Call<List<ApiBatchResponse>> call, Response<List<ApiBatchResponse>> batchesResponse) {
                        if (!batchesResponse.isSuccessful() || batchesResponse.body() == null) { error(callback, batchesResponse.code()); return; }
                        Map<Integer, ApiBatchResponse> batches = new HashMap<>();
                        for (ApiBatchResponse batch : batchesResponse.body()) if (batch != null && batch.id != null) batches.put(batch.id, batch);
                        api.getProductsPaged(0, 200).enqueue(new Callback<PageResponse<ProductResponse>>() {
                            @Override public void onResponse(Call<PageResponse<ProductResponse>> call, Response<PageResponse<ProductResponse>> productsResponse) {
                                if (!productsResponse.isSuccessful() || productsResponse.body() == null || productsResponse.body().getContent() == null) { error(callback, productsResponse.code()); return; }
                                Map<Integer, ProductResponse> products = new HashMap<>();
                                for (ProductResponse product : productsResponse.body().getContent()) if (product != null && product.getId() != null) products.put(product.getId(), product);
                                callback.onSuccess(buildSession(storeId, startedBy, count, inventory, batches, products));
                            }
                            @Override public void onFailure(Call<PageResponse<ProductResponse>> call, Throwable error) { callback.onError(networkError(error)); }
                        });
                    }
                    @Override public void onFailure(Call<List<ApiBatchResponse>> call, Throwable error) { callback.onError(networkError(error)); }
                });
            }
            @Override public void onFailure(Call<List<ApiInventoryResponse>> call, Throwable error) { callback.onError(networkError(error)); }
        });
    }

    private InventorySession buildSession(String storeId, String startedBy, ApiInventoryCountResponse count,
            List<ApiInventoryResponse> inventory, Map<Integer, ApiBatchResponse> batches,
            Map<Integer, ProductResponse> products) {
        Map<Integer, ApiInventoryCountResponse.Item> countedByInventory = new HashMap<>();
        if (count.items != null) for (ApiInventoryCountResponse.Item item : count.items)
            if (item != null && item.inventoryId != null && item.countedQuantity != null) countedByInventory.put(item.inventoryId, item);
        List<InventoryItem> rows = new ArrayList<>();
        for (ApiInventoryResponse row : inventory) {
            if (row == null || row.id == null || row.batchId == null) continue;
            ApiBatchResponse batch = batches.get(row.batchId);
            ProductResponse product = batch == null ? null : products.get(batch.productId);
            if (product == null) continue;
            ApiInventoryCountResponse.Item countItem = countedByInventory.get(row.id);
            int expected = row.currentQuantity == null ? 0 : (int) Math.round(row.currentQuantity);
            Integer counted = countItem == null ? null : (int) Math.round(countItem.countedQuantity);
            rows.add(new InventoryItem(String.valueOf(product.getId()), String.valueOf(row.id),
                    countItem == null || countItem.id == null ? null : String.valueOf(countItem.id),
                    product.getName(), product.getBarcode(), expected, counted,
                    countItem == null ? "" : countItem.observation));
        }
        InventorySession.Status status = "COMPLETED".equalsIgnoreCase(count.status)
                ? InventorySession.Status.FINALIZED : InventorySession.Status.IN_PROGRESS;
        InventorySession session = new InventorySession(String.valueOf(count.id), storeId,
                startedBy == null ? "Usuário operacional" : startedBy, count.startedAt,
                status, rows);
        sessions.put(storeId, session);
        countIdsByStore.put(storeId, String.valueOf(count.id));
        return session;
    }

    public void updateCount(String countId, String inventoryId, Integer quantity, CallbackResult callback) {
        InventorySession session = findSession(countId);
        InventoryItem item = findItem(session, inventoryId);
        if (session == null || item == null) { callback.onError("Item fora da contagem atual."); return; }
        String itemId = item.getCountItemId();
        if (quantity == null) {
            if (itemId == null) { callback.onSuccess(session); return; }
            api.deleteInventoryCountItem(countId, itemId).enqueue(new Callback<Void>() {
                @Override public void onResponse(Call<Void> call, Response<Void> response) { if (!response.isSuccessful()) error(callback, response.code()); else reload(session, callback); }
                @Override public void onFailure(Call<Void> call, Throwable error) { callback.onError(networkError(error)); }
            });
            return;
        }
        if (itemId == null) {
            api.addInventoryCountItem(countId, new ApiInventoryCountItemRequest(Integer.valueOf(inventoryId), quantity, item.getObservation()))
                    .enqueue(new Callback<ApiInventoryCountResponse.Item>() {
                @Override public void onResponse(Call<ApiInventoryCountResponse.Item> call, Response<ApiInventoryCountResponse.Item> response) {
                    if (!response.isSuccessful() || response.body() == null) { error(callback, response.code()); return; }
                    updateLocal(session, inventoryId, response.body(), quantity, item.getObservation(), callback);
                }
                @Override public void onFailure(Call<ApiInventoryCountResponse.Item> call, Throwable error) { callback.onError(networkError(error)); }
            });
        } else {
            api.updateInventoryCountItem(countId, itemId, new ApiInventoryCountItemUpdateRequest(quantity, item.getObservation()))
                    .enqueue(new Callback<ApiInventoryCountResponse.Item>() {
                @Override public void onResponse(Call<ApiInventoryCountResponse.Item> call, Response<ApiInventoryCountResponse.Item> response) {
                    if (!response.isSuccessful() || response.body() == null) { error(callback, response.code()); return; }
                    updateLocal(session, inventoryId, response.body(), quantity, item.getObservation(), callback);
                }
                @Override public void onFailure(Call<ApiInventoryCountResponse.Item> call, Throwable error) { callback.onError(networkError(error)); }
            });
        }
    }

    public void updateObservation(String countId, String inventoryId, String observation, CallbackResult callback) {
        InventorySession session = findSession(countId);
        InventoryItem item = findItem(session, inventoryId);
        if (session == null || item == null || item.getCountItemId() == null || item.getCountedQuantity() == null) {
            callback.onSuccess(session); return;
        }
        api.updateInventoryCountItem(countId, item.getCountItemId(),
                new ApiInventoryCountItemUpdateRequest(item.getCountedQuantity(), observation))
                .enqueue(new Callback<ApiInventoryCountResponse.Item>() {
            @Override public void onResponse(Call<ApiInventoryCountResponse.Item> call, Response<ApiInventoryCountResponse.Item> response) {
                if (!response.isSuccessful() || response.body() == null) { error(callback, response.code()); return; }
                updateLocal(session, inventoryId, response.body(), item.getCountedQuantity(), observation, callback);
            }
            @Override public void onFailure(Call<ApiInventoryCountResponse.Item> call, Throwable error) { callback.onError(networkError(error)); }
        });
    }

    private void updateLocal(InventorySession session, String inventoryId, ApiInventoryCountResponse.Item result,
            Integer quantity, String observation, CallbackResult callback) {
        List<InventoryItem> rows = new ArrayList<>();
        for (InventoryItem row : session.getItems()) {
            if (inventoryId.equals(row.getInventoryId())) rows.add(row.withServerCount(result.id == null ? null : String.valueOf(result.id), quantity, observation));
            else rows.add(row);
        }
        InventorySession updated = session.withItems(rows);
        sessions.put(session.getStoreId(), updated);
        callback.onSuccess(updated);
    }

    public void finish(String countId, CallbackResult callback) {
        InventorySession session = findSession(countId);
        if (session == null) { callback.onError("Contagem não encontrada."); return; }
        String key = UUID.randomUUID().toString();
        api.finishInventoryCount(countId, key).enqueue(new Callback<ApiInventoryCountResponse>() {
            @Override public void onResponse(Call<ApiInventoryCountResponse> call, Response<ApiInventoryCountResponse> response) {
                if (!response.isSuccessful() || response.body() == null) { error(callback, response.code()); return; }
                InventorySession finished = session.withStatus(InventorySession.Status.FINALIZED);
                sessions.put(session.getStoreId(), finished);
                callback.onSuccess(finished);
            }
            @Override public void onFailure(Call<ApiInventoryCountResponse> call, Throwable error) { callback.onError(networkError(error)); }
        });
    }

    private void reload(InventorySession session, CallbackResult callback) {
        api.getInventoryCount(session.getId()).enqueue(new Callback<ApiInventoryCountResponse>() {
            @Override public void onResponse(Call<ApiInventoryCountResponse> call, Response<ApiInventoryCountResponse> response) {
                if (!response.isSuccessful() || response.body() == null) { error(callback, response.code()); return; }
                Map<Integer, ApiInventoryCountResponse.Item> items = new HashMap<>();
                if (response.body().items != null) for (ApiInventoryCountResponse.Item item : response.body().items) items.put(item.inventoryId, item);
                List<InventoryItem> rows = new ArrayList<>();
                for (InventoryItem row : session.getItems()) {
                    ApiInventoryCountResponse.Item saved = items.get(Integer.valueOf(row.getInventoryId()));
                    rows.add(saved == null ? row.withServerCount(null, null, "")
                            : row.withServerCount(saved.id == null ? null : String.valueOf(saved.id),
                                saved.countedQuantity == null ? null : (int)Math.round(saved.countedQuantity), saved.observation));
                }
                InventorySession updated = session.withItems(rows);
                sessions.put(session.getStoreId(), updated);
                callback.onSuccess(updated);
            }
            @Override public void onFailure(Call<ApiInventoryCountResponse> call, Throwable error) { callback.onError(networkError(error)); }
        });
    }

    private InventorySession findSession(String id) {
        for (InventorySession value : sessions.values()) if (value.getId().equals(id)) return value;
        return null;
    }
    private InventoryItem findItem(InventorySession session, String inventoryId) {
        if (session == null) return null;
        for (InventoryItem item : session.getItems()) if (inventoryId.equals(item.getInventoryId())) return item;
        return null;
    }
    private String networkError(Throwable error) { return "Falha de rede com a API de inventário: " + error.getMessage(); }
    private void error(CallbackResult callback, int code) { callback.onError("API de inventário respondeu HTTP " + code + "."); }
}
