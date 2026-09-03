package com.mottainai.operacional.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.ListenerRegistration;

import com.mottainai.operacional.R;
import com.mottainai.operacional.adapters.AlertAdapter;
import com.mottainai.operacional.adapters.SuggestionAdapter;
import com.mottainai.operacional.repository.AlertRepository;
import com.mottainai.operacional.repository.SuggestionRepository;
import com.mottainai.operacional.utils.RoleHelper;
import com.mottainai.operacional.utils.SessionManager;

/**
 * Aba "IA": alertas de estoque e, para quem pode aprovar (Gerente/Dono), as
 * sugestões pendentes.
 *
 * Alertas e sugestões vêm do Firestore em tempo real, via os repositórios que
 * já existem no projeto (AlertRepository/SuggestionRepository). Não há ainda
 * endpoint para aprovar/recusar uma sugestão — esse passo fica pendente na
 * ApproveSuggestionFragment, com o mesmo aviso "pendente" que o resto do app já
 * usa para funcionalidades que aguardam contrato de API.
 */
public class IaAlertsFragment extends Fragment {

    private static final int ALERT_WINDOW_DAYS = 7;

    private TabLayout tabLayout;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private RecyclerView recyclerView;

    private SessionManager sessionManager;
    private NavController navController;

    private final AlertAdapter alertAdapter = new AlertAdapter();
    private SuggestionAdapter suggestionAdapter;

    private AlertRepository alertRepository;
    private SuggestionRepository suggestionRepository;
    private ListenerRegistration alertsListener;
    private ListenerRegistration suggestionsListener;

    private boolean showingSuggestions = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ia_alerts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        sessionManager = new SessionManager(requireContext());
        alertRepository = new AlertRepository();
        suggestionRepository = new SuggestionRepository();

        String role = sessionManager.getRole();
        boolean canViewSuggestions = RoleHelper.canViewSuggestions(role);

        suggestionAdapter = new SuggestionAdapter(suggestion ->
                navController.navigate(R.id.action_iaAlertsFragment_to_approveSuggestionFragment,
                        suggestionArgs(suggestion.getId(), suggestion.getTitle(), suggestion.getDescription())));

        tabLayout = view.findViewById(R.id.tab_ia);
        progressBar = view.findViewById(R.id.progress_ia);
        tvEmpty = view.findViewById(R.id.tv_ia_empty);
        recyclerView = view.findViewById(R.id.rv_ia);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Estoquista não decide sugestões, então nem mostramos a aba.
        TabLayout.Tab suggestionsTab = tabLayout.getTabAt(1);
        if (!canViewSuggestions && suggestionsTab != null) {
            tabLayout.removeTab(suggestionsTab);
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                showingSuggestions = tab.getPosition() == 1;
                bindCurrentTab();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        view.findViewById(R.id.btn_ask_ai).setOnClickListener(v ->
                navController.navigate(R.id.action_iaAlertsFragment_to_chatAiFragment));

        bindCurrentTab();
    }

    private void bindCurrentTab() {
        recyclerView.setAdapter(showingSuggestions ? suggestionAdapter : alertAdapter);
        showLoading();
        if (showingSuggestions) {
            listenSuggestions();
        } else {
            listenAlerts();
        }
    }

    private void listenAlerts() {
        detachListeners();
        String storeId = sessionManager.getStoreId();
        alertsListener = alertRepository.listenAlerts(storeId, ALERT_WINDOW_DAYS, new AlertRepository.AlertCallback() {
            @Override
            public void onSuccess(java.util.List<com.mottainai.operacional.models.Alert> alerts) {
                if (!isAdded()) return;
                alertAdapter.setAlerts(alerts);
                showContent(alerts.isEmpty(), "Nenhum alerta no momento");
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;
                showContent(true, "Não foi possível carregar os alertas");
            }
        });
    }

    private void listenSuggestions() {
        detachListeners();
        String storeId = sessionManager.getStoreId();
        suggestionsListener = suggestionRepository.listenSuggestions(storeId, new SuggestionRepository.SuggestionCallback() {
            @Override
            public void onSuccess(java.util.List<com.mottainai.operacional.models.Suggestion> suggestions) {
                if (!isAdded()) return;
                suggestionAdapter.setSuggestions(suggestions);
                showContent(suggestions.isEmpty(), "Nenhuma sugestão pendente");
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;
                showContent(true, "Não foi possível carregar as sugestões");
            }
        });
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
    }

    private void showContent(boolean empty, String emptyMessage) {
        progressBar.setVisibility(View.GONE);
        if (empty) {
            tvEmpty.setText(emptyMessage);
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void detachListeners() {
        if (alertsListener != null) { alertsListener.remove(); alertsListener = null; }
        if (suggestionsListener != null) { suggestionsListener.remove(); suggestionsListener = null; }
    }

    private static Bundle suggestionArgs(String id, String title, String description) {
        Bundle args = new Bundle();
        args.putString("suggestion_id", id);
        args.putString("suggestion_title", title);
        args.putString("suggestion_description", description);
        return args;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        detachListeners();
    }
}
