package com.prospective.tractorplus.viewobject.offerholder;

import com.prospective.tractorplus.utils.Constants;

public class OfferListParameterHolder {

    public String returnType;

    public OfferListParameterHolder() {

        this.returnType = "";
    }

    public OfferListParameterHolder getSellerOfferList() {
        this.returnType = Constants.OFFER_TYPE_SELLER;

        return this;
    }

    public OfferListParameterHolder getBuyerOfferList() {
        this.returnType = Constants.OFFER_TYPE_BUYER;

        return this;
    }

    public String getMapKey() {

        String result = "";

        if (!returnType.isEmpty()) {
            result += returnType;
        }
        return result;
    }
}
