package com.prospective.tractorplus.ui.inapppurchase.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.databinding.DataBindingComponent;
import androidx.databinding.DataBindingUtil;

import com.android.billingclient.api.SkuDetails;
import com.prospective.tractorplus.R;
import com.prospective.tractorplus.databinding.ItemInAppPurchaseBinding;
import com.prospective.tractorplus.ui.common.DataBoundListAdapter;
import com.prospective.tractorplus.ui.common.DataBoundViewHolder;
import com.prospective.tractorplus.utils.Objects;

public class InAppPurchaseAdapter extends DataBoundListAdapter<SkuDetails, ItemInAppPurchaseBinding> {

    private final androidx.databinding.DataBindingComponent dataBindingComponent;
    private SkuDetailsClickCallback callback;
    private DataBoundListAdapter.DiffUtilDispatchedInterface diffUtilDispatchedInterface = null;

    public InAppPurchaseAdapter(DataBindingComponent dataBindingComponent, SkuDetailsClickCallback SkuDetailsClickCallback) {
        this.dataBindingComponent = dataBindingComponent;
        this.callback = SkuDetailsClickCallback;
    }

    @Override
    protected ItemInAppPurchaseBinding createBinding(ViewGroup parent) {

        ItemInAppPurchaseBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()),
                        R.layout.item_in_app_purchase, parent, false,
                        dataBindingComponent);

        binding.buyButton.setOnClickListener( v-> {
            SkuDetails skuDetails = binding.getSkuDetails();
            if (skuDetails != null && callback != null) {
                callback.onByeButtonClick(skuDetails);
            }
        });

        return binding;
    }

    @Override
    public void bindView(DataBoundViewHolder<ItemInAppPurchaseBinding> holder, int position) {
        super.bindView(holder, position);
    }

    @Override
    protected void dispatched() {
        if (diffUtilDispatchedInterface != null) {
            diffUtilDispatchedInterface.onDispatched();
        }
    }

    @Override
    protected void bind(ItemInAppPurchaseBinding binding, SkuDetails item) {
        binding.setSkuDetails(item);
        binding.titleTextView.setText(item.getTitle());
        binding.descriptionTextView.setText(item.getDescription());
        binding.priceTextView.setText(item.getPrice());
    }

    @Override
    protected boolean areItemsTheSame(SkuDetails oldItem, SkuDetails newItem) {
        return Objects.equals(oldItem.getTitle(), newItem.getTitle())
                && oldItem.getDescription().equals(newItem.getDescription())
                && oldItem.getPrice().equals(newItem.getPrice());
    }

    @Override
    protected boolean areContentsTheSame(SkuDetails oldItem, SkuDetails newItem) {
        return Objects.equals(oldItem.getTitle(), newItem.getTitle())
                && oldItem.getDescription().equals(newItem.getDescription())
                && oldItem.getPrice().equals(newItem.getPrice());
    }

    public interface SkuDetailsClickCallback {
        void onByeButtonClick(SkuDetails skuDetails);
    }
}

