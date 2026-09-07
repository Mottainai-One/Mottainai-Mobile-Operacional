package com.mottainai.operacional.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.mottainai.operacional.R;

/**
 * Tela de decisão de uma sugestão da IA (aprovar/recusar).
 *
 * O SuggestionRepository só tem leitura (listenSuggestions); não existe ainda
 * endpoint para gravar a decisão. Os botões avisam "pendente", no mesmo padrão
 * que o resto do app já usa (ver ScannerFragment.openDamageRegistration).
 */
public class ApproveSuggestionFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_approve_suggestion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        String title = args != null ? args.getString("suggestion_title") : null;
        String description = args != null ? args.getString("suggestion_description") : null;

        TextView tvTitle = view.findViewById(R.id.tv_suggestion_title);
        TextView tvDescription = view.findViewById(R.id.tv_suggestion_description);
        tvTitle.setText(title != null ? title : "Sugestão");
        tvDescription.setText(description != null ? description : "");

        view.findViewById(R.id.btn_back).setOnClickListener(v ->
                Navigation.findNavController(view).popBackStack());

        view.findViewById(R.id.btn_approve).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Aprovar sugestão — pendente (aguarda endpoint)", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.btn_reject).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Recusar sugestão — pendente (aguarda endpoint)", Toast.LENGTH_SHORT).show());
    }
}
