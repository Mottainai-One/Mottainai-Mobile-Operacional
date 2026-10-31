package com.mottainai.operacional.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.mottainai.operacional.R;
import com.mottainai.operacional.databinding.FragmentLocalReviewBinding;
import com.mottainai.operacional.utils.Constants;
import com.mottainai.operacional.utils.RoleHelper;
import com.mottainai.operacional.utils.SessionManager;

/** Prévia local das revisões gerenciais representadas no Figma; nenhuma ação altera o estoque. */
public class LocalReviewFragment extends Fragment {
    public static final String ARG_REVIEW_TYPE = "review_type";
    public static final String TYPE_COUNT = "count";
    public static final String TYPE_RECEIVING = "receiving";
    public static final String TYPE_DAMAGE = "damage";

    private FragmentLocalReviewBinding binding;
    private String type = TYPE_COUNT;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLocalReviewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        type = getArguments() == null ? TYPE_COUNT
                : getArguments().getString(ARG_REVIEW_TYPE, TYPE_COUNT);
        String role = RoleHelper.normalize(new SessionManager(requireContext()).getRole());
        boolean allowed = TYPE_COUNT.equals(type)
                ? Constants.ROLE_DONO.equals(role) : Constants.ROLE_GERENTE.equals(role);
        if (!allowed) {
            NavHostFragment.findNavController(this).navigateUp();
            return;
        }
        render();
        binding.btnReviewActionPrimary.setOnClickListener(v -> showResult(false));
        binding.btnReviewActionSecondary.setOnClickListener(v -> {
            if (TYPE_COUNT.equals(type)) showResult(true);
            else NavHostFragment.findNavController(this).navigateUp();
        });
        binding.btnReviewResultBack.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());
    }

    private void render() {
        if (TYPE_COUNT.equals(type)) {
            binding.tvReviewTitle.setText(R.string.op_count_review_title);
            binding.tvReviewSubtitle.setText(R.string.op_count_review_subtitle);
            binding.tvReviewProduct.setText(R.string.op_flow_restock_product);
            binding.tvReviewContext.setText(R.string.op_count_review_context);
            binding.tvReviewEvidence.setText(R.string.op_count_review_evidence);
            binding.tvReviewWarning.setText(R.string.op_count_review_warning);
            binding.btnReviewActionPrimary.setText(R.string.op_count_review_approve);
            binding.btnReviewActionSecondary.setText(R.string.op_count_review_recount);
        } else if (TYPE_RECEIVING.equals(type)) {
            binding.tvReviewTitle.setText(R.string.op_manager_receiving_title);
            binding.tvReviewSubtitle.setText(R.string.op_manager_receiving_subtitle);
            binding.tvReviewProduct.setText(R.string.op_flow_restock_product);
            binding.tvReviewContext.setText(R.string.op_manager_receiving_context);
            binding.tvReviewEvidence.setText(R.string.op_manager_receiving_evidence);
            binding.tvReviewWarning.setText(R.string.op_manager_receiving_warning);
            binding.btnReviewActionPrimary.setText(R.string.op_manager_receiving_confirm);
            binding.btnReviewActionSecondary.setText(R.string.op_review_back);
        } else {
            binding.tvReviewTitle.setText(R.string.op_manager_damage_title);
            binding.tvReviewSubtitle.setText(R.string.op_manager_damage_subtitle);
            binding.tvReviewProduct.setText(R.string.op_flow_restock_product);
            binding.tvReviewContext.setText(R.string.op_manager_damage_context);
            binding.tvReviewEvidence.setText(R.string.op_manager_damage_evidence);
            binding.tvReviewWarning.setText(R.string.op_manager_damage_warning);
            binding.btnReviewActionPrimary.setText(R.string.op_manager_damage_confirm);
            binding.btnReviewActionSecondary.setText(R.string.op_review_back);
        }
    }

    private void showResult(boolean recount) {
        binding.groupReviewDetail.setVisibility(View.GONE);
        binding.groupReviewResult.setVisibility(View.VISIBLE);
        if (TYPE_COUNT.equals(type) && recount) {
            binding.tvReviewResultTitle.setText(R.string.op_count_recount_title);
            binding.tvReviewResultBody.setText(R.string.op_count_recount_body);
        } else if (TYPE_COUNT.equals(type)) {
            binding.tvReviewResultTitle.setText(R.string.op_count_approved_title);
            binding.tvReviewResultBody.setText(R.string.op_count_approved_body);
        } else if (TYPE_RECEIVING.equals(type)) {
            binding.tvReviewResultTitle.setText(R.string.op_manager_receiving_preview_title);
            binding.tvReviewResultBody.setText(R.string.op_manager_receiving_preview_body);
        } else {
            binding.tvReviewResultTitle.setText(R.string.op_manager_damage_preview_title);
            binding.tvReviewResultBody.setText(R.string.op_manager_damage_preview_body);
        }
    }

    @Override public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
