package com.mottainai.operacional.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.mottainai.operacional.models.Supplier;
import com.mottainai.operacional.models.SupplierAddress;
import com.mottainai.operacional.repository.SupplierRepository;
import java.util.List;

public class SupplierViewModel extends AndroidViewModel {
    private final SupplierRepository repository;
    private final MutableLiveData<List<Supplier>> suppliers = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> completed = new MutableLiveData<>();
    public SupplierViewModel(@NonNull Application app) { super(app); repository = new SupplierRepository(app); }
    public LiveData<List<Supplier>> getSuppliers() { return suppliers; }
    public LiveData<String> getError() { return error; }
    public LiveData<Boolean> getCompleted() { return completed; }
    public void clearCompletion() { completed.setValue(false); }
    public void load(String storeId) { repository.list(storeId, new SupplierRepository.Callback<List<Supplier>>() { public void onSuccess(List<Supplier> value) { suppliers.setValue(value); } public void onError(String message) { error.setValue(message); } }); }
    public void find(String storeId, String id, SupplierRepository.Callback<Supplier> callback) { repository.findById(storeId, id, callback); }
    public void create(String storeId, String name, String cnpj, String email, String phone, SupplierAddress address) { repository.create(storeId, name, cnpj, email, phone, address, new SupplierRepository.Callback<Supplier>() { public void onSuccess(Supplier value) { load(storeId); completed.setValue(true); } public void onError(String message) { error.setValue(message); } }); }
    public void update(String storeId, String id, String name, String email, String phone, SupplierAddress address) { repository.update(storeId, id, name, email, phone, address, new SupplierRepository.Callback<Supplier>() { public void onSuccess(Supplier value) { load(storeId); completed.setValue(true); } public void onError(String message) { error.setValue(message); } }); }
    public void delete(String storeId, String id) { repository.delete(storeId, id, new SupplierRepository.Callback<Void>() { public void onSuccess(Void value) { load(storeId); completed.setValue(true); } public void onError(String message) { error.setValue(message); } }); }
}
