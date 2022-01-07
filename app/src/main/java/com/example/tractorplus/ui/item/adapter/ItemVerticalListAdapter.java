package com.example.tractorplus.ui.item.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;

import com.example.tractorplus.Config;
import com.example.tractorplus.R;
import com.example.tractorplus.databinding.ItemItemVerticalWithUserBinding;
import com.example.tractorplus.ui.common.DataBoundListAdapter;
import com.example.tractorplus.ui.common.DataBoundViewHolder;
import com.example.tractorplus.utils.Constants;
import com.example.tractorplus.utils.Objects;
import com.example.tractorplus.utils.Utils;
import com.example.tractorplus.viewobject.Item;

/**
 * Created by Panacea-Soft on 9/18/18.
 * Contact Email : teamps.is.cool@gmail.com
 */


public class ItemVerticalListAdapter extends DataBoundListAdapter<Item, ItemItemVerticalWithUserBinding> {

    private final androidx.databinding.DataBindingComponent dataBindingComponent;
    private final NewsClickCallback callback;
    private DataBoundListAdapter.DiffUtilDispatchedInterface diffUtilDispatchedInterface;

    public ItemVerticalListAdapter(androidx.databinding.DataBindingComponent dataBindingComponent,
                                   NewsClickCallback callback, DiffUtilDispatchedInterface diffUtilDispatchedInterface) {
        this.dataBindingComponent = dataBindingComponent;
        this.callback = callback;
        this.diffUtilDispatchedInterface = diffUtilDispatchedInterface;
    }

    @Override
    protected ItemItemVerticalWithUserBinding createBinding(ViewGroup parent) {
        ItemItemVerticalWithUserBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()),
                        R.layout.item_item_vertical_with_user, parent, false,
                        dataBindingComponent);
        binding.getRoot().setOnClickListener(v -> {
            Item item = binding.getItem();
            if (item != null && callback != null) {
                callback.onClick(item);
            }
        });

        return binding;
    }

    // For general animation
    @Override
    public void bindView(DataBoundViewHolder<ItemItemVerticalWithUserBinding> holder, int position) {
        super.bindView(holder, position);


        //setAnimation(holder.itemView, position);
    }

    @Override
    protected void dispatched() {
        if (diffUtilDispatchedInterface != null) {
            diffUtilDispatchedInterface.onDispatched();
        }
    }

    @Override
    protected void bind(ItemItemVerticalWithUserBinding binding, Item item) {
        binding.setItem(item);
        String currencySymbol = item.itemCurrency.currencySymbol;
        String price;
        try {
            price = Utils.format(Double.parseDouble(item.price));

        } catch (Exception e) {
            price = item.price;
        }
        String currencyPrice;
        if (Config.SYMBOL_SHOW_FRONT) {
            currencyPrice = currencySymbol + " " + price;
        } else {
            currencyPrice = price + " " + currencySymbol;
        }

        if(!item.price.equals("0") && !item.price.equals("")) {
            binding.priceTextView.setText(currencyPrice);
        } else {
            binding.priceTextView.setText(R.string.item_price_free);
        }

        if (item.isSoldOut.equals(Constants.ONE)) {
            binding.isSoldTextView.setVisibility(View.VISIBLE);
        } else {
            binding.isSoldTextView.setVisibility(View.GONE);
        }

        if (item.paidStatus.equals(Constants.ADSPROGRESS)){
            binding.addedDateStrTextView.setText(R.string.paid__sponsored);
        } else{
            binding.addedDateStrTextView.setText(item.addedDateStr);
        }

        if (Config.ENABLE_SUB_LOCATION && !item.itemTownshipLocation.townshipName.equals("")){
            binding.addressTextView.setText(item.itemTownshipLocation.townshipName);
        } else {
            binding.addressTextView.setText(item.itemLocation.name);
        }
    }

    @Override
    protected boolean areItemsTheSame(Item oldItem, Item newItem) {
        return Objects.equals(oldItem.id, newItem.id)
                && oldItem.itemLocation.name.equals(newItem.itemLocation.name)
                && oldItem.itemTownshipLocation.townshipName.equals(newItem.itemTownshipLocation.townshipName)
                && oldItem.title.equals(newItem.title)
                && oldItem.isFavourited.equals(newItem.isFavourited)
                && oldItem.favouriteCount.equals(newItem.favouriteCount)
                && oldItem.isSoldOut.equals(newItem.isSoldOut)
                && oldItem.addedUserId.equals(newItem.addedUserId);
    }

    @Override
    protected boolean areContentsTheSame(Item oldItem, Item newItem) {
        return Objects.equals(oldItem.id, newItem.id)
                && oldItem.itemLocation.name.equals(newItem.itemLocation.name)
                && oldItem.itemTownshipLocation.townshipName.equals(newItem.itemTownshipLocation.townshipName)
                && oldItem.title.equals(newItem.title)
                && oldItem.isFavourited.equals(newItem.isFavourited)
                && oldItem.favouriteCount.equals(newItem.favouriteCount)
                && oldItem.isSoldOut.equals(newItem.isSoldOut)
                && oldItem.addedUserId.equals(newItem.addedUserId);
    }

    public interface NewsClickCallback {
        void onClick(Item item);

    }


}
