package com.panaceasoft.psbuyandsell.viewobject.holder;

import com.panaceasoft.psbuyandsell.utils.Constants;

public class SubCategoryParameterHolder {

    public String order_by, order_type, cityId;

    public SubCategoryParameterHolder() {

        this.order_by = Constants.FILTERING_ADDED_DATE;
        this.order_type = Constants.FILTERING_DESC;
        this.cityId = "";

    }

    public SubCategoryParameterHolder getTrendingSubCategories()
    {
        this.cityId = "";
        this.order_by = Constants.FILTERING_TRENDING;
        this.order_type = Constants.FILTERING_DESC;

        return this;
    }

    public String getSubCategoryMapKey() {
        String result = "";

        if (!cityId.isEmpty()) {
            result += cityId;
        }

        if (!order_by.isEmpty()) {
            result += order_by;
        }

        if (!order_type.isEmpty()) {
            result += order_type;
        }

        return result;
    }
}
