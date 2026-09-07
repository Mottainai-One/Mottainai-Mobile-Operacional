package com.mottainai.operacional.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.mottainai.operacional.R;

/**
 * Assistente de IA. Ainda não existe backend de chat, então a tela mostra um
 * roteiro fixo (mesma honestidade que o resto do app: "pendente" ao tocar
 * numa pergunta rápida, em vez de fingir uma resposta).
 */
public class ChatAiFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_ai, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btn_back).setOnClickListener(v ->
                Navigation.findNavController(view).popBackStack());

        View.OnClickListener pending = v ->
                Toast.makeText(requireContext(), "Chat com IA — pendente (aguarda backend)", Toast.LENGTH_SHORT).show();
        view.findViewById(R.id.tv_quick_question_1).setOnClickListener(pending);
        view.findViewById(R.id.tv_quick_question_2).setOnClickListener(pending);
    }
}
