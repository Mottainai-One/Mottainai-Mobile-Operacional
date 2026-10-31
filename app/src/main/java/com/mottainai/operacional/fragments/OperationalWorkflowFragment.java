package com.mottainai.operacional.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.mottainai.operacional.R;
import com.mottainai.operacional.databinding.FragmentOperationalWorkflowBinding;
import com.mottainai.operacional.utils.Constants;
import com.mottainai.operacional.utils.RoleHelper;
import com.mottainai.operacional.utils.SessionManager;

/** Fluxos de protótipo do Figma para reposição e recebimento, sem persistência de estoque. */
public class OperationalWorkflowFragment extends Fragment {
    public static final String ARG_WORKFLOW_TYPE = "workflow_type";
    public static final String TYPE_REPLENISHMENT = "replenishment";
    public static final String TYPE_RECEIVING = "receiving";

    private static final int STAGE_OVERVIEW = 0;
    private static final int STAGE_FORM = 1;
    private static final int STAGE_REVIEW = 2;
    private static final int STAGE_PROGRESS = 3;
    private static final int STAGE_SUCCESS = 4;
    private static final int STAGE_ERROR = 5;
    private static final int EXPECTED_STOCK = 10;
    private static final int SHELF_STOCK = 2;
    private static final int EXPECTED_DELIVERY = 24;

    private FragmentOperationalWorkflowBinding binding;
    private boolean receiving;
    private int stage = STAGE_OVERVIEW;
    private int quantity = 6;
    private String batch = "B07";
    private String expiry = "30/11/2026";

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentOperationalWorkflowBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String role = RoleHelper.normalize(new SessionManager(requireContext()).getRole());
        if (!Constants.ROLE_ESTOQUISTA.equals(role)) {
            NavHostFragment.findNavController(this).navigateUp();
            return;
        }
        String type = getArguments() == null ? TYPE_REPLENISHMENT
                : getArguments().getString(ARG_WORKFLOW_TYPE, TYPE_REPLENISHMENT);
        receiving = TYPE_RECEIVING.equals(type);
        if (receiving) quantity = EXPECTED_DELIVERY - 2;
        bindOverview();
        bindActions();
        render();
    }

    private void bindOverview() {
        if (receiving) {
            binding.tvOverviewHeading.setText(R.string.op_flow_receiving_heading);
            binding.tvOverviewIntro.setText(R.string.op_flow_receiving_intro);
            binding.tvOverviewContextTitle.setText(R.string.op_flow_receiving_supplier);
            binding.tvOverviewContext.setText(R.string.op_flow_receiving_context);
            binding.btnOverviewPrimary.setText(R.string.op_flow_receiving_start);
            binding.btnOverviewSecondary.setText(R.string.op_flow_back_to_tasks);
        } else {
            binding.tvOverviewHeading.setText(R.string.op_flow_restock_heading);
            binding.tvOverviewIntro.setText(R.string.op_flow_restock_intro);
            binding.tvOverviewContextTitle.setText(R.string.op_flow_restock_product);
            binding.tvOverviewContext.setText(R.string.op_flow_restock_context);
            binding.btnOverviewPrimary.setText(R.string.op_flow_restock_start);
            binding.btnOverviewSecondary.setText(R.string.op_flow_view_counts);
        }
    }

    private void bindActions() {
        binding.btnOverviewPrimary.setOnClickListener(v -> {
            stage = STAGE_FORM;
            render();
        });
        binding.btnOverviewSecondary.setOnClickListener(v -> {
            if (receiving) {
                NavHostFragment.findNavController(this).navigateUp();
            } else {
                NavHostFragment.findNavController(this).navigate(R.id.countInventoryFragment);
            }
        });
        binding.btnFormPrimary.setOnClickListener(v -> continueToReview());
        binding.btnFormCancel.setOnClickListener(v -> {
            stage = STAGE_OVERVIEW;
            render();
        });
        binding.btnReviewPrimary.setOnClickListener(v -> submitPrototype());
        binding.btnReviewSecondary.setOnClickListener(v -> {
            stage = STAGE_FORM;
            render();
        });
        binding.btnSuccessPrimary.setOnClickListener(v -> {
            if (receiving) {
                NavHostFragment.findNavController(this).navigateUp();
            } else {
                stage = STAGE_OVERVIEW;
                render();
            }
        });
        binding.btnSuccessSecondary.setOnClickListener(v -> {
            if (receiving) {
                stage = STAGE_OVERVIEW;
                quantity = 22;
                batch = "B07";
                expiry = "30/11/2026";
                render();
            } else {
                Bundle args = new Bundle();
                args.putInt(ProductsListFragment.ARG_INITIAL_TAB, 1);
                NavHostFragment.findNavController(this).navigate(R.id.productsListFragment, args);
            }
        });
        binding.btnErrorRetry.setOnClickListener(v -> {
            stage = STAGE_REVIEW;
            render();
        });
    }

    private void continueToReview() {
        EditText quantityInput = binding.etQuantity;
        String rawQuantity = quantityInput.getText() == null ? ""
                : quantityInput.getText().toString().trim();
        try {
            quantity = Integer.parseInt(rawQuantity);
        } catch (NumberFormatException exception) {
            showFormError(getString(R.string.damage_quantity_invalid));
            return;
        }
        if (quantity <= 0) {
            showFormError(getString(R.string.damage_quantity_positive));
            return;
        }
        if (!receiving && quantity > EXPECTED_STOCK) {
            showFormError("O depósito tem somente 10 unidades disponíveis.");
            return;
        }
        if (receiving) {
            batch = binding.etBatch.getText() == null ? ""
                    : binding.etBatch.getText().toString().trim();
            expiry = binding.etExpiry.getText() == null ? ""
                    : binding.etExpiry.getText().toString().trim();
            if (TextUtils.isEmpty(batch) || TextUtils.isEmpty(expiry)) {
                showFormError("Informe o lote e a validade da entrega.");
                return;
            }
        }
        binding.tvFormError.setVisibility(View.GONE);
        stage = STAGE_REVIEW;
        render();
    }

    private void submitPrototype() {
        stage = STAGE_PROGRESS;
        render();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (binding == null) return;
            stage = STAGE_SUCCESS;
            render();
        }, 450L);
    }

    private void showFormError(String message) {
        binding.tvFormError.setText(message);
        binding.tvFormError.setVisibility(View.VISIBLE);
    }

    private void render() {
        if (binding == null) return;
        binding.stateOverview.setVisibility(stage == STAGE_OVERVIEW ? View.VISIBLE : View.GONE);
        binding.stateForm.setVisibility(stage == STAGE_FORM ? View.VISIBLE : View.GONE);
        binding.stateReview.setVisibility(stage == STAGE_REVIEW ? View.VISIBLE : View.GONE);
        binding.stateProgress.setVisibility(stage == STAGE_PROGRESS ? View.VISIBLE : View.GONE);
        binding.stateSuccess.setVisibility(stage == STAGE_SUCCESS ? View.VISIBLE : View.GONE);
        binding.stateError.setVisibility(stage == STAGE_ERROR ? View.VISIBLE : View.GONE);
        if (stage == STAGE_FORM) renderForm();
        if (stage == STAGE_REVIEW) renderReview();
        if (stage == STAGE_PROGRESS) binding.tvProgressMessage.setText(receiving
                ? R.string.op_flow_sending : R.string.op_flow_restock_confirm);
        if (stage == STAGE_SUCCESS) renderSuccess();
        if (stage == STAGE_ERROR) renderError();
    }

    private void renderForm() {
        binding.tvFormHeading.setText(receiving ? R.string.op_flow_receiving_form_heading
                : R.string.op_flow_restock_form_heading);
        binding.tvFormProduct.setText(R.string.op_flow_restock_product);
        binding.tvFormProductMeta.setText(receiving ? R.string.op_flow_receiving_code
                : R.string.op_flow_restock_code);
        binding.tvQuantityLabel.setText(receiving ? R.string.op_flow_received_quantity
                : R.string.op_flow_restock_quantity);
        binding.etQuantity.setText(String.valueOf(quantity));
        binding.groupBatch.setVisibility(receiving ? View.VISIBLE : View.GONE);
        binding.etBatch.setText(batch);
        binding.etExpiry.setText(expiry);
        binding.tvFormLocationTitle.setVisibility(receiving ? View.GONE : View.VISIBLE);
        binding.tvFormLocation.setVisibility(receiving ? View.GONE : View.VISIBLE);
        binding.tvFormLocationTitle.setText(R.string.op_flow_restock_source_title);
        binding.tvFormLocation.setText(R.string.op_flow_restock_source);
        binding.btnFormPrimary.setText(receiving ? R.string.op_flow_receiving_review
                : R.string.op_flow_restock_review);
        binding.tvFormError.setVisibility(View.GONE);
    }

    private void renderReview() {
        if (receiving) {
            int difference = quantity - EXPECTED_DELIVERY;
            binding.tvReviewHeading.setText(R.string.op_flow_receiving_review_heading);
            binding.tvReviewProduct.setText(R.string.op_flow_restock_product);
            binding.tvReviewMeta.setText("Lote " + batch + " — validade " + expiry);
            binding.tvReviewSummary.setText(getString(R.string.op_flow_expected) + "       "
                    + getString(R.string.op_flow_received, quantity) + "       "
                    + getString(R.string.op_flow_difference, difference > 0
                    ? "+" + difference : difference < 0 ? "−" + Math.abs(difference) : "0"));
            binding.groupStockProjection.setVisibility(View.GONE);
            binding.tvReviewWarning.setVisibility(difference == 0 ? View.GONE : View.VISIBLE);
            if (difference < 0) {
                binding.tvReviewWarning.setText(getString(R.string.op_flow_receiving_warning,
                        Math.abs(difference)));
                binding.tvReviewWarning.setVisibility(View.VISIBLE);
            } else if (difference > 0) {
                binding.tvReviewWarning.setText("Entrega com " + difference + " unidades a mais");
                binding.tvReviewWarning.setVisibility(View.VISIBLE);
            }
            binding.tvReviewNote.setText(R.string.op_flow_receiving_note);
            binding.btnReviewPrimary.setText(R.string.op_flow_send_conference);
            binding.btnReviewSecondary.setText(R.string.op_flow_correct_data);
        } else {
            int depositAfter = EXPECTED_STOCK - quantity;
            int shelfAfter = SHELF_STOCK + quantity;
            binding.tvReviewHeading.setText(R.string.op_flow_restock_review_heading);
            binding.tvReviewProduct.setText(R.string.op_flow_restock_product);
            binding.tvReviewMeta.setText("Lote A03");
            binding.tvReviewSummary.setText(getString(R.string.op_flow_restock_movement, quantity)
                    + "\nDepósito → Gôndola");
            binding.groupStockProjection.setVisibility(View.VISIBLE);
            binding.tvReviewDeposit.setText(getString(R.string.op_flow_restock_deposit,
                    EXPECTED_STOCK, depositAfter));
            binding.tvReviewShelf.setText(getString(R.string.op_flow_restock_shelf,
                    SHELF_STOCK, shelfAfter));
            binding.tvReviewWarning.setVisibility(View.GONE);
            binding.tvReviewNote.setText(getString(R.string.op_flow_restock_total,
                    EXPECTED_STOCK + SHELF_STOCK) + "\n"
                    + getString(R.string.op_flow_restock_balance_note));
            binding.btnReviewPrimary.setText(R.string.op_flow_restock_confirm);
            binding.btnReviewSecondary.setText(R.string.op_flow_correct_quantity);
        }
    }

    private void renderSuccess() {
        if (receiving) {
            binding.tvSuccessTitle.setText(R.string.op_flow_awaiting_manager);
            binding.tvSuccessBody.setText("Arroz tipo 1 — 5 kg\nLote " + batch
                    + " — validade " + expiry + "\n24 unidades pedidas — " + quantity
                    + " recebidas\nO Gerente precisa conferir a diferença antes de disponibilizar o lote.");
            binding.tvSuccessNote.setText(R.string.op_flow_receiving_note);
            binding.btnSuccessPrimary.setText(R.string.op_flow_back_to_tasks);
            binding.btnSuccessSecondary.setText(R.string.op_flow_another_delivery);
        } else {
            binding.tvSuccessTitle.setText(R.string.op_flow_restock_success_title);
            binding.tvSuccessBody.setText(quantity + " unidades movimentadas\nArroz tipo 1 — 5 kg — lote A03\nDepósito: "
                    + (EXPECTED_STOCK - quantity) + " un. — gôndola: "
                    + (SHELF_STOCK + quantity) + " un.\nRegistro concluído agora");
            binding.tvSuccessNote.setText(R.string.op_prototype_local_notice);
            binding.btnSuccessPrimary.setText(R.string.op_flow_back_to_prelist);
            binding.btnSuccessSecondary.setText(R.string.op_flow_view_stock);
        }
    }

    private void renderError() {
        binding.tvErrorTitle.setText(receiving ? R.string.op_flow_receiving_error_title
                : R.string.op_flow_error_title);
        binding.tvErrorMessage.setText(receiving ? R.string.op_flow_receiving_error_body
                : R.string.op_flow_restock_error_body);
        binding.btnErrorRetry.setText(R.string.op_flow_retry_review);
    }

    @Override public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
