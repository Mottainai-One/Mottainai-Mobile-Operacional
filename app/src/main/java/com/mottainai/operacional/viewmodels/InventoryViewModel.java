package com.mottainai.operacional.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mottainai.operacional.models.InventorySession;
import com.mottainai.operacional.repository.InventoryRepository;

import android.os.Handler;
import android.os.Looper;
import com.mottainai.operacional.models.InventoryItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** ViewModel for API-backed inventory counts. */
public class InventoryViewModel extends AndroidViewModel {

    private final InventoryRepository repository;
    private final MutableLiveData<InventorySession> session = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final Handler debounceHandler = new Handler(Looper.getMainLooper());
    private final Map<String, Runnable> pendingWrites = new HashMap<>();

    public InventoryViewModel(@NonNull Application application) {
        super(application);
        repository = new InventoryRepository(application);
    }

    public LiveData<InventorySession> getSession() {
        return session;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void load(String storeId) {
        loading.setValue(true);
        repository.load(storeId, new InventoryRepository.CallbackResult() {
            @Override
            public void onSuccess(InventorySession inventorySession) {
                session.setValue(inventorySession);
                loading.setValue(false);
            }

            @Override
            public void onError(String message) {
                error.setValue(message);
                loading.setValue(false);
            }
        });
    }

    public void start(String storeId, String startedBy) {
        loading.setValue(true);
        repository.start(storeId, startedBy, sessionCallback());
    }

    public void updateCount(String productId, Integer quantity) {
        InventorySession current = session.getValue();
        if (current == null || current.getStatus() == InventorySession.Status.FINALIZED) return;
        updateLocalItem(productId, quantity, null, false);
        scheduleWrite(productId);
    }

    public void updateObservation(String productId, String observation) {
        InventorySession current = session.getValue();
        if (current == null || current.getStatus() == InventorySession.Status.FINALIZED) return;
        updateLocalItem(productId, null, observation, true);
        scheduleWrite(productId);
    }

    public void finish() {
        InventorySession current = session.getValue();
        if (current == null) return;
        if (!current.isComplete()) {
            error.setValue("Confira todos os produtos antes de finalizar.");
            return;
        }
        if (current.hasUnexplainedDivergences()) {
            error.setValue("Registre uma observação em cada divergência antes de finalizar.");
            return;
        }
        loading.setValue(true);
        repository.finish(current.getId(), sessionCallback());
    }

    private InventoryRepository.CallbackResult sessionCallback() {
        return new InventoryRepository.CallbackResult() {
            @Override
            public void onSuccess(InventorySession inventorySession) {
                session.setValue(inventorySession);
                loading.setValue(false);
            }

            @Override
            public void onError(String message) {
                error.setValue(message);
                loading.setValue(false);
            }
        };
    }

    private void updateLocalItem(String inventoryId, Integer quantity, String observation, boolean updateObservation) {
        InventorySession current = session.getValue();
        if (current == null) return;
        List<InventoryItem> updated = new ArrayList<>();
        for (InventoryItem item : current.getItems()) {
            if (inventoryId.equals(item.getInventoryId())) {
                if (updateObservation) item = item.withObservation(observation);
                else item = item.withCountedQuantity(quantity);
            }
            updated.add(item);
        }
        session.setValue(current.withItems(updated));
    }

    private void scheduleWrite(String inventoryId) {
        Runnable previous = pendingWrites.remove(inventoryId);
        if (previous != null) debounceHandler.removeCallbacks(previous);
        Runnable write = () -> {
            pendingWrites.remove(inventoryId);
            InventorySession current = session.getValue();
            if (current == null || current.getStatus() == InventorySession.Status.FINALIZED) return;
            InventoryItem latest = null;
            for (InventoryItem item : current.getItems()) if (inventoryId.equals(item.getInventoryId())) { latest = item; break; }
            if (latest == null) return;
            final InventoryItem item = latest;
            repository.updateCount(current.getId(), inventoryId, item.getCountedQuantity(), new InventoryRepository.CallbackResult() {
                @Override public void onSuccess(InventorySession updated) {
                    if (updated != null) session.postValue(updated);
                    if (item.getCountedQuantity() != null) repository.updateObservation(current.getId(), inventoryId,
                            item.getObservation(), new InventoryRepository.CallbackResult() {
                                @Override public void onSuccess(InventorySession withObservation) { if (withObservation != null) session.postValue(withObservation); }
                                @Override public void onError(String message) { error.postValue(message); }
                            });
                }
                @Override public void onError(String message) { error.postValue(message); }
            });
        };
        pendingWrites.put(inventoryId, write);
        debounceHandler.postDelayed(write, 650L);
    }
}
