package com.mottainai.operacional.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.ListenerRegistration;

import com.mottainai.operacional.models.Alert;
import com.mottainai.operacional.models.Suggestion;
import com.mottainai.operacional.repository.AlertRepository;
import com.mottainai.operacional.repository.SuggestionRepository;

import java.util.List;

public class HomeViewModel extends AndroidViewModel {

    private final AlertRepository alertRepository = new AlertRepository();
    private final SuggestionRepository suggestionRepository = new SuggestionRepository();

    private ListenerRegistration alertListener;
    private ListenerRegistration suggestionListener;

    // Flags síncronas para saber quando cada fonte terminou de carregar.
    // (Não confiar em LiveData.getValue() logo após postValue(), que é assíncrono.)
    private boolean alertsLoaded = false;
    private boolean suggestionsLoaded = false;

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<List<com.mottainai.operacional.models.Alert>> alerts = new MutableLiveData<>();
    private final MutableLiveData<List<com.mottainai.operacional.models.Suggestion>> suggestions = new MutableLiveData<>();

    public HomeViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<Boolean> getLoading() {
        return loading;
    }

    public MutableLiveData<String> getError() {
        return error;
    }

    public MutableLiveData<List<com.mottainai.operacional.models.Alert>> getAlerts() {
        return alerts;
    }

    public MutableLiveData<List<com.mottainai.operacional.models.Suggestion>> getSuggestions() {
        return suggestions;
    }

    public void loadData(String storeId) {
        loading.setValue(true);
        error.setValue(null);
        alertsLoaded = false;
        suggestionsLoaded = false;

        // Alertas
        alertRepository.listenAlerts(storeId, 7, new AlertRepository.AlertCallback() {
            @Override
            public void onSuccess(List<com.mottainai.operacional.models.Alert> alertList) {
                alerts.postValue(alertList);
                alertsLoaded = true;
                checkLoadingDone();
            }

            @Override
            public void onError(Exception e) {
                error.postValue(e.getMessage());
                alertsLoaded = true;
                loading.postValue(false);
            }
        });

        // Sugestões
        suggestionRepository.listenSuggestions(storeId, new SuggestionRepository.SuggestionCallback() {
            @Override
            public void onSuccess(List<com.mottainai.operacional.models.Suggestion> suggestionList) {
                suggestions.postValue(suggestionList);
                suggestionsLoaded = true;
                checkLoadingDone();
            }

            @Override
            public void onError(Exception e) {
                error.postValue(e.getMessage());
                suggestionsLoaded = true;
                loading.postValue(false);
            }
        });
    }

    private void checkLoadingDone() {
        if (alertsLoaded && suggestionsLoaded) {
            loading.postValue(false);
        }
    }

    public void retry(String storeId) {
        loadData(storeId);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (alertListener != null) {
            alertListener.remove();
        }
        if (suggestionListener != null) {
            suggestionListener.remove();
        }
    }
}