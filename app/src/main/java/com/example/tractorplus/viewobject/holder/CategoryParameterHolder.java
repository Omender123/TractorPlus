package com.example.tractorplus.viewobject.holder;

import com.example.tractorplus.utils.Constants;

public class CategoryParameterHolder {

    public String order_by, order_type, cityId;

    public CategoryParameterHolder() {

        this.order_by = Constants.FILTERING_ADDED_DATE;
        this.order_type = Constants.FILTERING_DESC;
        this.cityId = "";

    }

    public CategoryParameterHolder getTrendingCategories()
    {
        this.cityId = "";
        this.order_by = Constants.FILTERING_TRENDING;
        this.order_type = Constants.FILTERING_DESC;

        return this;
    }

    public String getCategoryMapKey() {
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
