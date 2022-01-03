package com.panaceasoft.psbuyandsell.ui.item.itemtownshiplocation.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;

import com.panaceasoft.psbuyandsell.R;
import com.panaceasoft.psbuyandsell.databinding.ItemItemTownshipLocationBinding;
import com.panaceasoft.psbuyandsell.ui.common.DataBoundListAdapter;
import com.panaceasoft.psbuyandsell.ui.common.DataBoundViewHolder;
import com.panaceasoft.psbuyandsell.utils.Objects;
import com.panaceasoft.psbuyandsell.viewobject.ItemTownshipLocation;

public class ItemTownshipLocationAdapter extends DataBoundListAdapter<ItemTownshipLocation, ItemItemTownshipLocationBinding> {

    private final androidx.databinding.DataBindingComponent dataBindingComponent;
    private final ItemTownshipLocationAdapter.NewsClickCallback callback;
    private DiffUtilDispatchedInterface diffUtilDispatchedInterface = null;
    public String locationId = "";

    public ItemTownshipLocationAdapter(androidx.databinding.DataBindingComponent dataBindingComponent,
                                       ItemTownshipLocationAdapter.NewsClickCallback callback,
                                       DiffUtilDispatchedInterface diffUtilDispatchedInterface) {
        this.dataBindingComponent = dataBindingComponent;
        this.callback = callback;
        this.diffUtilDispatchedInterface = diffUtilDispatchedInterface;
    }

    public ItemTownshipLocationAdapter(androidx.databinding.DataBindingComponent dataBindingComponent,
                                       ItemTownshipLocationAdapter.NewsClickCallback callback, String locationId) {
        this.dataBindingComponent = dataBindingComponent;
        this.callback = callback;
        this.locationId = locationId;
    }

    @Override
    protected ItemItemTownshipLocationBinding createBinding(ViewGroup parent) {
        ItemItemTownshipLocationBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()),
                        R.layout.item_item_township_location, parent, false,
                        dataBindingComponent);

        binding.getRoot().setOnClickListener(v -> {

            ItemTownshipLocation itemCurrency = binding.getItemLocation();

            if (itemCurrency != null && callback != null) {

                binding.groupview.setBackgroundColor(parent.getResources().getColor(R.color.md_green_50));

                callback.onClick(itemCurrency, itemCurrency.id);
            }
        });
        return binding;
    }

    @Override
    public void bindView(DataBoundViewHolder<ItemItemTownshipLocationBinding> holder, int position) {
        super.bindView(holder, position);

    }

    @Override
    protected void dispatched() {
        if (diffUtilDispatchedInterface != null) {
            diffUtilDispatchedInterface.onDispatched();
        }
    }

    @Override
    protected void bind(ItemItemTownshipLocationBinding binding, ItemTownshipLocation item) {
        binding.setItemLocation(item);

        if (locationId != null) {
            if (item.id.equals(locationId)) {
                binding.groupview.setBackgroundColor(binding.groupview.getResources().getColor((R.color.md_green_50)));
            }else{
                binding.groupview.setBackgroundColor(binding.groupview.getResources().getColor(R.color.md_white_1000));
            }
        }

    }

    @Override
    protected boolean areItemsTheSame(ItemTownshipLocation oldItem, ItemTownshipLocation newItem) {
        return Objects.equals(oldItem.id, newItem.id);
    }

    @Override
    protected boolean areContentsTheSame(ItemTownshipLocation oldItem, ItemTownshipLocation newItem) {
        return Objects.equals(oldItem.id, newItem.id);
    }

    public interface NewsClickCallback {
        void onClick(ItemTownshipLocation itemType, String id);
    }

}
