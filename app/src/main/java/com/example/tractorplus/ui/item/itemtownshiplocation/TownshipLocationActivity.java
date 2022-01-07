package com.example.tractorplus.ui.item.itemtownshiplocation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.example.tractorplus.Config;
import com.example.tractorplus.R;
import com.example.tractorplus.databinding.ActivityTownshipLocationBinding;
import com.example.tractorplus.ui.common.PSAppCompactActivity;
import com.example.tractorplus.utils.Constants;
import com.example.tractorplus.utils.MyContextWrapper;
import com.example.tractorplus.utils.Utils;

public class TownshipLocationActivity extends PSAppCompactActivity {

    public String itemId;
    public String cityId;
    //region Override Methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityTownshipLocationBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_township_location);

        // Init all UI
        initUI(binding);

    }

    @Override
    protected void attachBaseContext(Context newBase) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(newBase);
        String CURRENT_LANG_CODE = preferences.getString(Constants.LANGUAGE_CODE, Config.DEFAULT_LANGUAGE);
        String CURRENT_LANG_COUNTRY_CODE = preferences.getString(Constants.LANGUAGE_COUNTRY_CODE, Config.DEFAULT_LANGUAGE_COUNTRY_CODE);

        super.attachBaseContext(MyContextWrapper.wrap(newBase, CURRENT_LANG_CODE, CURRENT_LANG_COUNTRY_CODE, true));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }

    }
    //endregion


    //region Private Methods

    private void initUI(ActivityTownshipLocationBinding binding) {

        if (getIntent().getExtras() != null) {
          itemId = getIntent().getExtras().getString(Constants.ITEM_ID);
            Utils.psLog(itemId);
        }

        setupFragment(new ItemTownshipLocationFragment());

    }

}