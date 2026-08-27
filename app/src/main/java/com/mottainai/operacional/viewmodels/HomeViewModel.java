package com.mottainai.operacional.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.ListenerRegistration;

import com.mottainai.operacional.models.Alert;
import com.mottainai.operacional.models.Suggestion;
import com.mottainai.operacional.repository.AlertRepository;
import com.mottainai.operacional.repository.SuggestionRepository;

import java.util.Collections;
import java.util.List;

/**
 * Estado único da Home: loading, vazio, erro, dados.
 * Evita espalhar múltiplas flags por observer separados.
 */
public class HomeViewModel extends AndroidViewModel {

    private final AlertRepository alertRepository = new AlertRepository();
    private final SuggestionRepository suggestionRepository = new SuggestionRepository();

    private ListenerRegistration alertListener;
    private ListenerRegistration suggestionListener;

    // Flags síncronas para saber quando cada fonte terminou de carregar.
    // Não confiar em getValue() logo após postValue(), que é assíncrono.
    private boolean alertsLoaded = false;
    private boolean suggestionsLoaded = false;

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> sessionError = new MutableLiveData<>();
    private final MutableLiveData<List<Alert>> alerts = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<List<Suggestion>> suggestions = new MutableLiveData<>(Collections.emptyList());

    public HomeViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<String> getSessionError() {
        return sessionError;
    }

    public LiveData<List<Alert>> getAlerts() {
        return alerts;
    }

    public LiveData<List<Suggestion>> getSuggestions() {
        return suggestions;
    }

    public void loadData(String storeId) {
        loading.setValue(true);
        error.setValue(null);
        sessionError.setValue(null);
        alertsLoaded = false;
        suggestionsLoaded = false;

        alertListener = alertRepository.listenAlerts(storeId, 7,
                new AlertRepository.AlertCallback() {
                    @Override
                    public void onSuccess(List<Alert> alertList) {
                        alerts.setValue(alertList != null ? alertList : Collections.emptyList());
                        alertsLoaded = true;
                        checkLoadingDone();
                    }

                    @Override
                    public void onError(Exception e) {
                        error.setValue(e != null ? e.getMessage() : "Erro desconhecido");
                        alertsLoaded = true;
                        loading.setValue(false);
                    }
                });

        suggestionListener = suggestionRepository.listenSuggestions(storeId,
                new SuggestionRepository.SuggestionCallback() {
                    @Override
                    public void onSuccess(List<Suggestion> suggestionList) {
                        suggestions.setValue(suggestionList != null ? suggestionList : Collections.emptyList());
                        suggestionsLoaded = true;
                        checkLoadingDone();
                    }

                    @Override
                    public void onError(Exception e) {
                        error.setValue(e != null ? e.getMessage() : "Erro desconhecido");
                        suggestionsLoaded = true;
                        loading.setValue(false);
                    }
                });
    }

    private void checkLoadingDone() {
        if (alertsLoaded && suggestionsLoaded) {
            loading.setValue(false);
        }
    }

    public void retry(String storeId) {
        // Cancela listeners antigos antes de recarregar para evitar duplicação
        if (alertListener != null) {
            alertListener.remove();
            alertListener = null;
        }
        if (suggestionListener != null) {
            suggestionListener.remove();
            suggestionListener = null;
        }
        loadData(storeId);
    }

    public void clearSessionError() {
        sessionError.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (alertListener != null) {
            alertListener.remove();
            alertListener = null;
        }
        if (suggestionListener != null) {
            suggestionListener.remove();
            suggestionListener = null;
        }
    }
}