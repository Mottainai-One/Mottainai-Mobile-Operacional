package com.mottainai.operacional.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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
 * Área Inteligência: Alertas, Sugestões e Chat ocupam a mesma tela por abas.
 * Alertas e sugestões permanecem nos repositórios atuais; o Chat é um fragmento
 * filho para preservar a conversa ao alternar entre as abas.
 */
public class IaAlertsFragment extends Fragment {

    public static final String ARG_INITIAL_TAB = "initial_ia_tab";
    public static final String TAB_ALERTS = "alerts";
    public static final String TAB_SUGGESTIONS = "suggestions";
    private static final int ALERT_WINDOW_DAYS = 7;
    private static final String CHAT_FRAGMENT_TAG = "intelligence_chat";

    private enum IntelligenceTab { ALERTS, SUGGESTIONS, CHAT }

    private TabLayout tabLayout;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private RecyclerView recyclerView;
    private FrameLayout chatContainer;

    private SessionManager sessionManager;
    private NavController navController;

    private final AlertAdapter alertAdapter = new AlertAdapter();
    private SuggestionAdapter suggestionAdapter;

    private AlertRepository alertRepository;
    private SuggestionRepository suggestionRepository;
    private ListenerRegistration alertsListener;
    private ListenerRegistration suggestionsListener;

    private IntelligenceTab selectedTab = IntelligenceTab.ALERTS;

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

        suggestionAdapter = new SuggestionAdapter(
                suggestion -> openSuggestionDecision(suggestion),
                suggestion -> openSuggestionDecision(suggestion),
                suggestion -> Toast.makeText(requireContext(), R.string.suggestion_decision_pending,
                        Toast.LENGTH_SHORT).show());

        tabLayout = view.findViewById(R.id.tab_ia);
        progressBar = view.findViewById(R.id.progress_ia);
        tvEmpty = view.findViewById(R.id.tv_ia_empty);
        recyclerView = view.findViewById(R.id.rv_ia);
        chatContainer = view.findViewById(R.id.chat_tab_container);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        configureTabs(canViewSuggestions);
        selectedTab = initialTab(canViewSuggestions);
        selectTab(selectedTab);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                Object tag = tab.getTag();
                if (tag instanceof IntelligenceTab) {
                    showTab((IntelligenceTab) tag);
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        showTab(selectedTab);
    }

    private IntelligenceTab initialTab(boolean canViewSuggestions) {
        Bundle arguments = getArguments();
        String requestedTab = arguments == null
                ? TAB_ALERTS
                : arguments.getString(ARG_INITIAL_TAB, TAB_ALERTS);
        if (TAB_SUGGESTIONS.equalsIgnoreCase(requestedTab) && canViewSuggestions) {
            return IntelligenceTab.SUGGESTIONS;
        }
        return IntelligenceTab.ALERTS;
    }

    private void selectTab(IntelligenceTab tabToSelect) {
        for (int position = 0; position < tabLayout.getTabCount(); position++) {
            TabLayout.Tab tab = tabLayout.getTabAt(position);
            if (tab != null && tabToSelect.equals(tab.getTag())) {
                tab.select();
                return;
            }
        }
    }

    private void configureTabs(boolean canViewSuggestions) {
        TabLayout.Tab alertsTab = tabLayout.getTabAt(0);
        TabLayout.Tab suggestionsTab = tabLayout.getTabAt(1);
        TabLayout.Tab chatTab = tabLayout.getTabAt(2);
        if (alertsTab != null) alertsTab.setTag(IntelligenceTab.ALERTS);
        if (suggestionsTab != null) suggestionsTab.setTag(IntelligenceTab.SUGGESTIONS);
        if (chatTab != null) chatTab.setTag(IntelligenceTab.CHAT);

        // Estoquista segue sem acesso às sugestões, mas o Chat permanece disponível.
        if (!canViewSuggestions && suggestionsTab != null) {
            tabLayout.removeTab(suggestionsTab);
        }

        TabLayout.Tab selected = tabLayout.getTabAt(tabLayout.getSelectedTabPosition());
        if (selected != null && selected.getTag() instanceof IntelligenceTab) {
            selectedTab = (IntelligenceTab) selected.getTag();
        }
    }

    private void showTab(IntelligenceTab tab) {
        selectedTab = tab;
        if (tab == IntelligenceTab.CHAT) {
            detachListeners();
            recyclerView.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.GONE);
            chatContainer.setVisibility(View.VISIBLE);
            ensureChatFragment();
            return;
        }

        chatContainer.setVisibility(View.GONE);
        recyclerView.setAdapter(tab == IntelligenceTab.SUGGESTIONS ? suggestionAdapter : alertAdapter);
        showLoading();
        if (tab == IntelligenceTab.SUGGESTIONS) {
            listenSuggestions();
        } else {
            listenAlerts();
        }
    }

    private void ensureChatFragment() {
        Fragment existing = getChildFragmentManager().findFragmentByTag(CHAT_FRAGMENT_TAG);
        if (existing != null) return;

        getChildFragmentManager().beginTransaction()
                .replace(R.id.chat_tab_container, new ChatAiFragment(), CHAT_FRAGMENT_TAG)
                .commit();
    }

    private void listenAlerts() {
        detachListeners();
        String storeId = sessionManager.getStoreId();
        alertsListener = alertRepository.listenAlerts(storeId, ALERT_WINDOW_DAYS, new AlertRepository.AlertCallback() {
            @Override
            public void onSuccess(java.util.List<com.mottainai.operacional.models.Alert> alerts) {
                if (!isAdded() || selectedTab != IntelligenceTab.ALERTS) return;
                alertAdapter.setAlerts(alerts);
                showContent(alerts.isEmpty(), "Nenhum alerta no momento");
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded() || selectedTab != IntelligenceTab.ALERTS) return;
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
                if (!isAdded() || selectedTab != IntelligenceTab.SUGGESTIONS) return;
                suggestionAdapter.setSuggestions(suggestions);
                showContent(suggestions.isEmpty(), "Nenhuma sugestão pendente");
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded() || selectedTab != IntelligenceTab.SUGGESTIONS) return;
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

    private void openSuggestionDecision(com.mottainai.operacional.models.Suggestion suggestion) {
        navController.navigate(R.id.action_iaAlertsFragment_to_approveSuggestionFragment,
                suggestionArgs(suggestion.getId(), suggestion.getTitle(), suggestion.getDescription()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        detachListeners();
    }
}
