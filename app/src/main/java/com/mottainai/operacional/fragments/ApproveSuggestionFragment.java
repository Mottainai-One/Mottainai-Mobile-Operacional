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
import com.mottainai.operacional.network.ApiService;
import com.mottainai.operacional.network.RetrofitClient;
import com.mottainai.operacional.models.ApiSuggestionDecisionRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Tela de decisão de uma sugestão da IA (aprovar/recusar).
 *
 * Decisions are persisted by the authenticated SQL API.
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

        binding.btnApprove.setOnClickListener(v -> decide(view, args, "APPROVED"));
        binding.btnReject.setOnClickListener(v -> decide(view, args, "REJECTED"));
    }

    private void decide(View view, Bundle args, String decision) {
        String id = args == null ? null : args.getString("suggestion_id");
        if (id == null || id.trim().isEmpty()) {
            Toast.makeText(requireContext(), "ID da sugestão ausente.", Toast.LENGTH_SHORT).show();
            return;
        }
        binding.btnApprove.setEnabled(false);
        binding.btnReject.setEnabled(false);
        ApiService api = RetrofitClient.getClient(requireContext()).create(ApiService.class);
        api.decideSuggestion(id, new ApiSuggestionDecisionRequest(decision)).enqueue(new Callback<com.mottainai.operacional.models.ApiSuggestionResponse>() {
            @Override public void onResponse(Call<com.mottainai.operacional.models.ApiSuggestionResponse> call,
                    Response<com.mottainai.operacional.models.ApiSuggestionResponse> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(requireContext(), decision.equals("APPROVED") ? "Sugestão aprovada." : "Sugestão recusada.", Toast.LENGTH_SHORT).show();
                    androidx.navigation.Navigation.findNavController(view).popBackStack();
                } else {
                    binding.btnApprove.setEnabled(true);
                    binding.btnReject.setEnabled(true);
                    Toast.makeText(requireContext(), "API recusou a decisão (HTTP " + response.code() + ").", Toast.LENGTH_LONG).show();
                }
            }
            @Override public void onFailure(Call<com.mottainai.operacional.models.ApiSuggestionResponse> call, Throwable error) {
                if (!isAdded()) return;
                binding.btnApprove.setEnabled(true);
                binding.btnReject.setEnabled(true);
                Toast.makeText(requireContext(), "Não foi possível salvar a decisão na API.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateFinalPrice(TextView view, Double currentPrice, Double discount) {
        if (currentPrice == null || discount == null || currentPrice < 0 || discount < 0 || discount > 100) {
            view.setText(R.string.suggestion_final_price_unavailable);
            return;
        }
        double finalPrice = currentPrice * (1 - discount / 100d);
        view.setText(getString(R.string.suggestion_final_price,
                NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(finalPrice)));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
