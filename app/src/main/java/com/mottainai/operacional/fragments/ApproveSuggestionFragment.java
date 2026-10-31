package com.mottainai.operacional.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;

import com.google.android.material.textfield.TextInputEditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.mottainai.operacional.R;
import com.mottainai.operacional.databinding.FragmentApproveSuggestionBinding;
import com.mottainai.operacional.utils.AppLocales;

import java.text.NumberFormat;

/**
 * Tela de decisão de uma sugestão da IA (aprovar/recusar).
 *
 * O SuggestionRepository só tem leitura (listenSuggestions); não existe ainda
 * endpoint para gravar a decisão. Os botões avisam "pendente", no mesmo padrão
 * que o resto do app já usa (ver ScannerFragment.openDamageRegistration).
 */
public class ApproveSuggestionFragment extends Fragment {

    private FragmentApproveSuggestionBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentApproveSuggestionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        String title = args != null ? args.getString("suggestion_title") : null;
        String description = args != null ? args.getString("suggestion_description") : null;

        TextView tvTitle = binding.suggestionSummary.tvSuggestionTitle;
        TextView tvDescription = binding.suggestionSummary.tvSuggestionDescription;
        TextView tvStatus = binding.suggestionSummary.tvSuggestionStatus;
        TextView tvAiDiscount = binding.tvAiDiscount;
        TextInputEditText inputDiscount = binding.inputDiscountValue;
        TextView tvFinalPrice = binding.tvSuggestedFinalPrice;
        tvTitle.setText(title != null ? title : getString(R.string.suggestion_fallback_title));
        tvDescription.setText(description != null ? description
                : getString(R.string.suggestion_description_unavailable));
        tvStatus.setText(R.string.suggestion_pending);
        Double currentPrice = args != null && args.containsKey("suggestion_current_price")
                ? args.getDouble("suggestion_current_price") : null;
        Double suggestedDiscount = args != null && args.containsKey("suggestion_discount")
                ? args.getDouble("suggestion_discount") : null;
        if (suggestedDiscount != null && suggestedDiscount >= 0) {
            inputDiscount.setText(String.valueOf(suggestedDiscount));
            tvAiDiscount.setText(getString(R.string.suggestion_ai_discount, suggestedDiscount));
        } else {
            tvAiDiscount.setText(R.string.op_suggestion_discount_unavailable);
        }
        updateFinalPrice(tvFinalPrice, currentPrice, suggestedDiscount);
        inputDiscount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                try { updateFinalPrice(tvFinalPrice, currentPrice, Double.parseDouble(s.toString().replace(',', '.'))); }
                catch (NumberFormatException ignored) { updateFinalPrice(tvFinalPrice, currentPrice, null); }
            }
            @Override public void afterTextChanged(Editable s) { }
        });

        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(view).popBackStack());

        binding.btnApprove.setOnClickListener(v ->
                Toast.makeText(requireContext(), R.string.suggestion_decision_pending, Toast.LENGTH_SHORT).show());

        binding.btnReject.setOnClickListener(v ->
                Toast.makeText(requireContext(), R.string.suggestion_decision_pending, Toast.LENGTH_SHORT).show());
    }

    private void updateFinalPrice(TextView view, Double currentPrice, Double discount) {
        if (currentPrice == null || discount == null || currentPrice < 0 || discount < 0 || discount > 100) {
            view.setText(R.string.suggestion_final_price_unavailable);
            return;
        }
        double finalPrice = currentPrice * (1 - discount / 100d);
        view.setText(getString(R.string.suggestion_final_price,
                NumberFormat.getCurrencyInstance(AppLocales.PT_BR).format(finalPrice)));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
