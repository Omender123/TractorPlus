package com.prospective.tractorplus.ui.item.itemtype;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.databinding.DataBindingUtil;

import com.prospective.tractorplus.Config;
import com.prospective.tractorplus.R;
import com.prospective.tractorplus.databinding.ActivitySearchViewBinding;
import com.prospective.tractorplus.ui.common.PSAppCompactActivity;
import com.prospective.tractorplus.ui.item.itemcondition.ItemConditionFragment;
import com.prospective.tractorplus.ui.item.itemcurrency.ItemCurrencyTypeFragment;
import com.prospective.tractorplus.ui.item.itemdealoption.ItemDealOptionTypeFragment;
import com.prospective.tractorplus.ui.item.itemlocation.ItemEntryLocationFragment;
import com.prospective.tractorplus.ui.item.itempricetype.ItemPriceTypeFragment;
import com.prospective.tractorplus.ui.item.itemtownshiplocation.ItemTownshipLocationFragment;
import com.prospective.tractorplus.utils.Constants;
import com.prospective.tractorplus.utils.MyContextWrapper;

public class SearchViewActivity extends PSAppCompactActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivitySearchViewBinding databinding = DataBindingUtil.setContentView(this, R.layout.activity_search_view);

        initUI(databinding);

    }

    @Override
    protected void attachBaseContext(Context newBase) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(newBase);
        String CURRENT_LANG_CODE = preferences.getString(Constants.LANGUAGE_CODE, Config.DEFAULT_LANGUAGE);
        String CURRENT_LANG_COUNTRY_CODE = preferences.getString(Constants.LANGUAGE_COUNTRY_CODE, Config.DEFAULT_LANGUAGE_COUNTRY_CODE);

        super.attachBaseContext(MyContextWrapper.wrap(newBase, CURRENT_LANG_CODE, CURRENT_LANG_COUNTRY_CODE, true));
    }

    protected void initUI(ActivitySearchViewBinding binding) {
        Intent intent = getIntent();

        String fragName = intent.getStringExtra(Constants.ITEM_TYPE_FLAG);

        switch (fragName) {
            case Constants.ITEM_TYPE:
                setupFragment(new ItemTypeFragment());
                initToolbar(binding.toolbar, getResources().getString(R.string.Feature_UI__search_alert_type_title));
                break;
            case Constants.ITEM_PRICE_TYPE:
                setupFragment(new ItemPriceTypeFragment());
                initToolbar(binding.toolbar, getResources().getString(R.string.Feature_UI__search_alert_price_type_title));
                break;
            case Constants.ITEM_CURRENCY_TYPE:
                setupFragment(new ItemCurrencyTypeFragment());
                initToolbar(binding.toolbar, getResources().getString(R.string.Feature_UI__search_alert_currency_title));
                break;
            case Constants.ITEM_DEAL_OPTION_TYPE:
                setupFragment(new ItemDealOptionTypeFragment());
                initToolbar(binding.toolbar, getResources().getString(R.string.Feature_UI__search_alert_deal_option_title));
                break;
            case Constants.ITEM_LOCATION_TYPE:
                setupFragment(new ItemEntryLocationFragment());
                initToolbar(binding.toolbar, getResources().getString(R.string.Feature_UI__search_alert_location_title));
                break;
            case Constants.ITEM_TOWNSHIP_TYPE:
                setupFragment(new ItemTownshipLocationFragment());
                initToolbar(binding.toolbar, getResources().getString(R.string.Feature_UI__search_alert_township_location_title));
                break;
            case Constants.ITEM_CONDITION_TYPE:
                setupFragment(new ItemConditionFragment());
                initToolbar(binding.toolbar, getResources().getString(R.string.Feature_UI__search_alert_condition_title));
                break;
        }

    }
}