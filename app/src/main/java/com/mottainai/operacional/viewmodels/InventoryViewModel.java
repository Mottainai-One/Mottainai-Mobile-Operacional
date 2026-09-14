package com.mottainai.operacional.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mottainai.operacional.models.InventorySession;
import com.mottainai.operacional.repository.MockInventoryRepository;

/** ViewModel da contagem de inventário em modo demonstração. */
public class InventoryViewModel extends AndroidViewModel {

    private final MockInventoryRepository repository;
    private final MutableLiveData<InventorySession> session = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public InventoryViewModel(@NonNull Application application) {
        super(application);
        repository = new MockInventoryRepository(application);
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
        repository.load(storeId, new MockInventoryRepository.Callback() {
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
        repository.updateCount(current.getId(), productId, quantity, sessionCallback());
    }

    public void updateObservation(String productId, String observation) {
        InventorySession current = session.getValue();
        if (current == null || current.getStatus() == InventorySession.Status.FINALIZED) return;
        repository.updateObservation(current.getId(), productId, observation, sessionCallback());
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

    private MockInventoryRepository.Callback sessionCallback() {
        return new MockInventoryRepository.Callback() {
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
}
