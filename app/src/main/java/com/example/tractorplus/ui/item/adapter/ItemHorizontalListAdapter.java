package com.example.tractorplus.ui.item.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;

import com.example.tractorplus.Config;
import com.example.tractorplus.R;
import com.example.tractorplus.databinding.ItemItemHorizontalWithUserBinding;
import com.example.tractorplus.ui.common.DataBoundListAdapter;
import com.example.tractorplus.ui.common.DataBoundViewHolder;
import com.example.tractorplus.utils.Constants;
import com.example.tractorplus.utils.Objects;
import com.example.tractorplus.utils.Utils;
import com.example.tractorplus.viewobject.Item;

public class ItemHorizontalListAdapter extends DataBoundListAdapter<Item, ItemItemHorizontalWithUserBinding> {

    private final androidx.databinding.DataBindingComponent dataBindingComponent;
    private final ItemHorizontalListAdapter.NewsClickCallback callback;
    private DataBoundListAdapter.DiffUtilDispatchedInterface diffUtilDispatchedInterface;

    public ItemHorizontalListAdapter(androidx.databinding.DataBindingComponent dataBindingComponent,
                                     ItemHorizontalListAdapter.NewsClickCallback callback,
                                     DiffUtilDispatchedInterface diffUtilDispatchedInterface) {
        this.dataBindingComponent = dataBindingComponent;
        this.callback = callback;
        this.diffUtilDispatchedInterface = diffUtilDispatchedInterface;
    }

    @Override
    protected ItemItemHorizontalWithUserBinding createBinding(ViewGroup parent) {
        ItemItemHorizontalWithUserBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()),
                        R.layout.item_item_horizontal_with_user, parent, false,
                        dataBindingComponent);
        binding.getRoot().setOnClickListener(v -> {
            Item item = binding.getItem();
            if (item != null && callback != null) {
                callback.onClick(item);
            }
        });
        return binding;


    }


    @Override
    public void bindView(DataBoundViewHolder<ItemItemHorizontalWithUserBinding> holder, int position) {
        super.bindView(holder, position);
    }

    @Override
    protected void dispatched() {
        if (diffUtilDispatchedInterface != null) {
            diffUtilDispatchedInterface.onDispatched();
        }
    }

    @Override
    protected void bind(ItemItemHorizontalWithUserBinding binding, Item item) {

        binding.setItem(item);

        binding.conditionTextView.setText(binding.getRoot().getResources().getString(R.string.item_condition__type, item.itemCondition.name));
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
                && oldItem.title.equals(newItem.title)
                && oldItem.itemLocation.name.equals(newItem.itemLocation.name)
                && oldItem.itemTownshipLocation.townshipName.equals(newItem.itemTownshipLocation.townshipName)
                && oldItem.isFavourited.equals(newItem.isFavourited)
                && oldItem.favouriteCount.equals(newItem.favouriteCount)
                && oldItem.isSoldOut.equals(newItem.isSoldOut)
                && oldItem.addedUserId.equals(newItem.addedUserId)
                && oldItem.user.userName.equals(newItem.user.userName);
    }

    @Override
    protected boolean areContentsTheSame(Item oldItem, Item newItem) {
        return Objects.equals(oldItem.id, newItem.id)
                && oldItem.title.equals(newItem.title)
                && oldItem.isFavourited.equals(newItem.isFavourited)
                && oldItem.itemLocation.name.equals(newItem.itemLocation.name)
                && oldItem.itemTownshipLocation.townshipName.equals(newItem.itemTownshipLocation.townshipName)
                && oldItem.favouriteCount.equals(newItem.favouriteCount)
                && oldItem.isSoldOut.equals(newItem.isSoldOut)
                && oldItem.addedUserId.equals(newItem.addedUserId)
                && oldItem.user.userName.equals(newItem.user.userName);
    }

    public interface NewsClickCallback {
        void onClick(Item item);
    }

}


