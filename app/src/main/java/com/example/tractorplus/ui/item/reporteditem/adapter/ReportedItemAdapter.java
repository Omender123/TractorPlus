package com.example.tractorplus.ui.item.reporteditem.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.databinding.DataBindingUtil;
import com.example.tractorplus.R;
import com.example.tractorplus.databinding.ItemReportedItemAdapterBinding;
import com.example.tractorplus.ui.common.DataBoundListAdapter;
import com.example.tractorplus.utils.Objects;
import com.example.tractorplus.viewobject.ReportedItem;


public class ReportedItemAdapter extends DataBoundListAdapter<ReportedItem, ItemReportedItemAdapterBinding> {

    private final androidx.databinding.DataBindingComponent dataBindingComponent;
    private ReportedItemClickCallback callback;
    private DataBoundListAdapter.DiffUtilDispatchedInterface diffUtilDispatchedInterface = null;

    public ReportedItemAdapter(androidx.databinding.DataBindingComponent dataBindingComponent, ReportedItemClickCallback reportedItemClickCallback) {
        this.dataBindingComponent = dataBindingComponent;
        this.callback = reportedItemClickCallback;
    }

    @Override
    protected ItemReportedItemAdapterBinding createBinding(ViewGroup parent) {
        ItemReportedItemAdapterBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()),
                        R.layout.item_reported_item_adapter, parent, false,
                        dataBindingComponent);

        binding.getRoot().setOnClickListener(v -> {
            ReportedItem reportedItem = binding.getReporteditem();
            if (reportedItem != null && callback != null) {
                callback.onClick(reportedItem);
            }

        });

        return binding;
    }

    @Override
    protected void dispatched() {
        if (diffUtilDispatchedInterface != null) {
            diffUtilDispatchedInterface.onDispatched();
        }
    }

    @Override
    protected void bind(ItemReportedItemAdapterBinding binding, ReportedItem item) {
        binding.setReporteditem(item);
        binding.reportedNameTextView.setText(item.title);

    }

    @Override
    protected boolean areItemsTheSame(ReportedItem oldItem, ReportedItem newItem) {
        return Objects.equals(oldItem.id, newItem.id)
                && oldItem.title.equals(newItem.title);
    }

    @Override
    protected boolean areContentsTheSame(ReportedItem oldItem, ReportedItem newItem) {
        return Objects.equals(oldItem.id, newItem.id)
                && oldItem.title.equals(newItem.title);
    }

    public interface ReportedItemClickCallback {
        void onClick(ReportedItem reportedItem);
    }

}
