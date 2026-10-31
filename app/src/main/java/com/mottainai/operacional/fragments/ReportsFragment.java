package com.mottainai.operacional.fragments;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.mottainai.operacional.R;
import com.mottainai.operacional.databinding.FragmentReportsBinding;
import com.mottainai.operacional.utils.RoleHelper;
import com.mottainai.operacional.utils.SessionManager;

/** Prévia interativa dos cenários do Figma; não há fonte de relatórios no app. */
public class ReportsFragment extends Fragment {
    private FragmentReportsBinding binding;
    private boolean weekly;
    private boolean sales;
    private int selectedBar = -1;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentReportsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!RoleHelper.isOwner(new SessionManager(requireContext()).getRole())) {
            NavHostFragment.findNavController(this).navigateUp();
            return;
        }
        binding.btnMonth.setOnClickListener(v -> { weekly = false; selectedBar = -1; render(); });
        binding.btnWeek.setOnClickListener(v -> { weekly = true; selectedBar = -1; render(); });
        binding.btnSales.setOnClickListener(v -> { sales = true; selectedBar = -1; render(); });
        binding.btnLosses.setOnClickListener(v -> { sales = false; selectedBar = -1; render(); });
        binding.btnOpenInventory.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putInt(ProductsListFragment.ARG_INITIAL_TAB, 1);
            NavHostFragment.findNavController(this).navigate(R.id.productsListFragment, args);
        });
        render();
    }

    private void render() {
        if (binding == null) return;
        select(binding.btnMonth, !weekly);
        select(binding.btnWeek, weekly);
        select(binding.btnSales, sales);
        select(binding.btnLosses, !sales);
        boolean available = !(sales && !weekly);
        binding.tvChartTitle.setText(!available ? R.string.op_report_unavailable
                : weekly ? (sales ? R.string.op_report_week_sales : R.string.op_report_week_losses)
                : R.string.op_report_month_losses);
        binding.tvReportTotal.setText(!available ? R.string.op_data_unavailable
                : weekly ? (sales ? R.string.op_report_week_sales_total : R.string.op_report_week_losses_total)
                : R.string.op_report_month_losses_total);
        int[] heights = weekly
                ? (sales ? new int[]{86, 104, 128, 118, 148, 111, 95}
                : new int[]{42, 54, 62, 78, 110, 100, 66})
                : new int[]{112, 127, 149};
        String[] labels = weekly
                ? new String[]{"13", "14", "15", "16", "17", "18", "19"}
                : new String[]{"01–07", "08–12", "13–19"};
        int[] barIds = {R.id.report_bar_1, R.id.report_bar_2, R.id.report_bar_3,
                R.id.report_bar_4, R.id.report_bar_5, R.id.report_bar_6, R.id.report_bar_7};
        if (selectedBar < 0 || selectedBar >= heights.length) selectedBar = heights.length - 1;
        for (int index = 0; index < barIds.length; index++) {
            View item = binding.getRoot().findViewById(barIds[index]);
            boolean show = available && index < heights.length;
            item.setVisibility(show ? View.VISIBLE : View.GONE);
            if (!show) continue;
            View bar = item.findViewById(R.id.bar);
            ViewGroup.LayoutParams size = bar.getLayoutParams();
            size.height = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    heights[index], getResources().getDisplayMetrics()));
            bar.setLayoutParams(size);
            bar.setBackgroundResource(index == selectedBar
                    ? R.drawable.op_report_bar_selected : R.drawable.op_report_bar);
            ((TextView) item.findViewById(R.id.label)).setText(labels[index]);
            int position = index;
            item.setOnClickListener(v -> { selectedBar = position; render(); });
        }
        if (!available) {
            binding.tvSelectedBar.setText(R.string.op_report_live_unavailable);
        } else if (selectedBar == heights.length - 1) {
            binding.tvSelectedBar.setText(weekly
                    ? (sales ? R.string.op_report_selected_week_sales : R.string.op_report_selected_week_losses)
                    : R.string.op_report_selected_month_losses);
        } else {
            binding.tvSelectedBar.setText(getString(R.string.op_report_selected_interval, labels[selectedBar]));
        }
    }

    private void select(TextView view, boolean selected) {
        view.setBackgroundResource(selected ? R.drawable.op_filter_selected : R.drawable.op_soft_card);
        view.setTextColor(requireContext().getColor(selected ? R.color.op_link : R.color.op_muted));
    }

    @Override public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
