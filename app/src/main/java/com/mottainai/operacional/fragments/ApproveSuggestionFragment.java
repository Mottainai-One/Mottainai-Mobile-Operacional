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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.NumberFormat;
import java.util.Locale;

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
        TextView tvStatus = view.findViewById(R.id.tv_suggestion_status);
        TextView tvAiDiscount = view.findViewById(R.id.tv_ai_discount);
        TextInputEditText inputDiscount = view.findViewById(R.id.input_discount_value);
        TextView tvFinalPrice = view.findViewById(R.id.tv_suggested_final_price);
        tvTitle.setText(title != null ? title : getString(R.string.suggestion_fallback_title));
        tvDescription.setText(description != null ? description
                : getString(R.string.suggestion_description_unavailable));
        tvStatus.setText(R.string.suggestion_pending);
        // Cenário mock até a API fornecer preço atual e desconto da IA.
        double currentPrice = args != null ? args.getDouble("suggestion_current_price", 12.90) : 12.90;
        double suggestedDiscount = args != null ? args.getDouble("suggestion_discount", 20) : 20;
        if (suggestedDiscount >= 0) inputDiscount.setText(String.valueOf(suggestedDiscount));
        tvAiDiscount.setText(getString(R.string.suggestion_ai_discount, suggestedDiscount));
        updateFinalPrice(tvFinalPrice, currentPrice, suggestedDiscount);
        final double priceForCalculation = currentPrice;
        inputDiscount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                try { updateFinalPrice(tvFinalPrice, priceForCalculation, Double.parseDouble(s.toString().replace(',', '.'))); }
                catch (NumberFormatException ignored) { updateFinalPrice(tvFinalPrice, priceForCalculation, -1); }
            }
            @Override public void afterTextChanged(Editable s) { }
        });

        view.findViewById(R.id.btn_back).setOnClickListener(v ->
                Navigation.findNavController(view).popBackStack());

        view.findViewById(R.id.btn_approve).setOnClickListener(v ->
                Toast.makeText(requireContext(), R.string.suggestion_decision_pending, Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.btn_reject).setOnClickListener(v ->
                Toast.makeText(requireContext(), R.string.suggestion_decision_pending, Toast.LENGTH_SHORT).show());
    }

    private static String extractDiscount(String description) {
        if (description == null) return null;
        Matcher matcher = Pattern.compile("(\\d{1,3}(?:[,.]\\d{1,2})?)\\s*%").matcher(description);
        return matcher.find() ? matcher.group(1).replace(',', '.') : null;
    }

    private void updateFinalPrice(TextView view, double currentPrice, double discount) {
        if (currentPrice < 0 || discount < 0 || discount > 100) {
            view.setText(R.string.suggestion_final_price_unavailable);
            return;
        }
        double finalPrice = currentPrice * (1 - discount / 100d);
        view.setText(getString(R.string.suggestion_final_price,
                NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(finalPrice)));
    }
}
