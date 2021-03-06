package com.prospective.tractorplus.ui.offer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;

import com.prospective.tractorplus.Config;
import com.prospective.tractorplus.R;
import com.prospective.tractorplus.databinding.ItemOfferBuyerListAdapterBinding;
import com.prospective.tractorplus.ui.common.DataBoundListAdapter;
import com.prospective.tractorplus.ui.common.DataBoundViewHolder;
import com.prospective.tractorplus.utils.Constants;
import com.prospective.tractorplus.utils.Objects;
import com.prospective.tractorplus.utils.Utils;
import com.prospective.tractorplus.viewobject.Offer;

public class OfferBuyerAdapter extends DataBoundListAdapter<Offer, ItemOfferBuyerListAdapterBinding> {

    private final androidx.databinding.DataBindingComponent dataBindingComponent;
    private final OfferBuyerAdapter.BuyerListClickCallback callback;
    private DataBoundListAdapter.DiffUtilDispatchedInterface diffUtilDispatchedInterface;

    public OfferBuyerAdapter(androidx.databinding.DataBindingComponent dataBindingComponent,
                             OfferBuyerAdapter.BuyerListClickCallback callback,
                             DiffUtilDispatchedInterface diffUtilDispatchedInterface) {
        this.dataBindingComponent = dataBindingComponent;
        this.callback = callback;
        this.diffUtilDispatchedInterface = diffUtilDispatchedInterface;

    }

    @Override
    protected ItemOfferBuyerListAdapterBinding createBinding(ViewGroup parent) {

        ItemOfferBuyerListAdapterBinding binding = (ItemOfferBuyerListAdapterBinding) DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()),
                        R.layout.item_offer_buyer_list_adapter, parent, false,
                        dataBindingComponent);

        binding.getRoot().setOnClickListener(v -> {

            Offer offer = binding.getOfferList();

            if (offer != null && callback != null) {
                callback.OnClick(offer, offer.id);
            }
        });
        return binding;
    }

    @Override
    public void bindView(DataBoundViewHolder<ItemOfferBuyerListAdapterBinding> holder, int position) {
        super.bindView(holder, position);

    }

    @Override
    protected void dispatched() {
        if (diffUtilDispatchedInterface != null) {
            diffUtilDispatchedInterface.onDispatched();
        }
    }

    @Override
    protected void bind(ItemOfferBuyerListAdapterBinding binding, Offer offer) {
        binding.setOfferList(offer);

        if (offer.isAccept.equals("1")) {
            binding.acceptTextView.setText(R.string.offer__accept_text);
        } else {
            binding.acceptTextView.setText(R.string.offer__offer_text);
        }

        if (!offer.item.itemCurrency.currencySymbol.equals("") && !offer.item.price.equals("")) {
            String currencySymbol = offer.item.itemCurrency.currencySymbol;
            String price;
            try {
                price = Utils.format(Double.parseDouble(offer.item.price));
            } catch (Exception e) {
                price = offer.item.price;
            }
            String currencyPrice;
            if (Config.SYMBOL_SHOW_FRONT) {
                currencyPrice = currencySymbol + " " + price;
            } else {
                currencyPrice = price + " " + currencySymbol;
            }
            binding.priceTextView.setText(currencyPrice);
        }
        binding.itemConditionTextView.setText(binding.getRoot().getResources().getString(R.string.item_condition__type, offer.item.itemCondition.name));

        if (offer.sellerUnreadCount.equals(Constants.ZERO)) {
            binding.countTextView.setVisibility(View.GONE);
        } else {
            binding.countTextView.setVisibility(View.VISIBLE);
        }

        if (offer.item.isSoldOut.equals(Constants.ONE)) {
            binding.soldTextView.setVisibility(View.VISIBLE);
        } else {
            binding.soldTextView.setVisibility(View.GONE);

        }

    }

    @Override
    protected boolean areItemsTheSame(Offer oldItem, Offer newItem) {
        return Objects.equals(oldItem.id, newItem.id) &&
                oldItem.addedDate.equals(newItem.addedDate) &&
                oldItem.sellerUnreadCount.equals(newItem.sellerUnreadCount) &&
                oldItem.isAccept.equals(newItem.isAccept);
    }

    @Override
    protected boolean areContentsTheSame(Offer oldItem, Offer newItem) {
        return Objects.equals(oldItem.id, newItem.id) &&
                oldItem.addedDate.equals(newItem.addedDate) &&
                oldItem.sellerUnreadCount.equals(newItem.sellerUnreadCount) &&
                oldItem.isAccept.equals(newItem.isAccept);
    }

    public interface BuyerListClickCallback {
        void OnClick(Offer offer, String id);
    }
}
