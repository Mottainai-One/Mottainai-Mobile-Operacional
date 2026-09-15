package com.mottainai.operacional.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.mottainai.operacional.models.Supplier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/** Fonte local por loja até a integração com /api/v1/suppliers estar habilitada. */
public class MockSupplierRepository {
    public interface Callback<T> { void onSuccess(T value); void onError(String message); }
    private static final String PREFS = "mottainai_suppliers";
    private final SharedPreferences preferences;
    private final Gson gson = new Gson();

    public MockSupplierRepository(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
    public void list(String storeId, Callback<List<Supplier>> callback) { callback.onSuccess(new ArrayList<>(read(storeId))); }
    public void findById(String storeId, String id, Callback<Supplier> callback) {
        for (Supplier supplier : read(storeId)) if (supplier.getId().equals(id)) { callback.onSuccess(supplier); return; }
        callback.onError("Fornecedor não encontrado.");
    }
    public void create(String storeId, String name, String cnpj, String contact, Callback<Supplier> callback) {
        List<Supplier> suppliers = read(storeId);
        Supplier supplier = new Supplier(UUID.randomUUID().toString(), name, cnpj, contact);
        suppliers.add(supplier); save(storeId, suppliers); callback.onSuccess(supplier);
    }
    public void update(String storeId, String id, String name, String cnpj, String contact, Callback<Supplier> callback) {
        List<Supplier> suppliers = read(storeId);
        for (int index = 0; index < suppliers.size(); index++) if (suppliers.get(index).getId().equals(id)) {
            Supplier updated = new Supplier(id, name, cnpj, contact);
            suppliers.set(index, updated); save(storeId, suppliers); callback.onSuccess(updated); return;
        }
        callback.onError("Fornecedor não encontrado.");
    }
    public void delete(String storeId, String id, Callback<Void> callback) {
        List<Supplier> suppliers = read(storeId);
        if (!suppliers.removeIf(supplier -> supplier.getId().equals(id))) { callback.onError("Fornecedor não encontrado."); return; }
        save(storeId, suppliers); callback.onSuccess(null);
    }
    private List<Supplier> read(String storeId) {
        String json = preferences.getString(key(storeId), null);
        if (json == null) return seed(storeId);
        Supplier[] suppliers = gson.fromJson(json, Supplier[].class);
        return suppliers == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(suppliers));
    }
    private void save(String storeId, List<Supplier> suppliers) { preferences.edit().putString(key(storeId), gson.toJson(suppliers)).apply(); }
    private List<Supplier> seed(String storeId) {
        List<Supplier> suppliers = new ArrayList<>();
        suppliers.add(new Supplier("supplier-verde-" + key(storeId), "Cooperativa Verde", "12345678000190", "contato@cooperativaverde.com"));
        suppliers.add(new Supplier("supplier-circular-" + key(storeId), "Distribuidora Circular", "98765432000110", "contato@distribuidoracircular.com"));
        save(storeId, suppliers); return suppliers;
    }
    private String key(String storeId) { return "suppliers_" + (storeId == null || storeId.trim().isEmpty() ? "demo-store" : storeId); }
}
