package com.prospective.tractorplus.ui.item.detail;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.prospective.tractorplus.Config;
import com.prospective.tractorplus.R;
import com.prospective.tractorplus.databinding.ActivityItemBinding;
import com.prospective.tractorplus.ui.common.PSAppCompactActivity;
import com.prospective.tractorplus.utils.Constants;
import com.prospective.tractorplus.utils.MyContextWrapper;


public class ItemActivity extends PSAppCompactActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityItemBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_item);

        initUI(binding);

    }

    @Override
    protected void attachBaseContext(Context newBase) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(newBase);
        String CURRENT_LANG_CODE = preferences.getString(Constants.LANGUAGE_CODE, Config.DEFAULT_LANGUAGE);
        String CURRENT_LANG_COUNTRY_CODE = preferences.getString(Constants.LANGUAGE_COUNTRY_CODE, Config.DEFAULT_LANGUAGE_COUNTRY_CODE);

        super.attachBaseContext(MyContextWrapper.wrap(newBase, CURRENT_LANG_CODE, CURRENT_LANG_COUNTRY_CODE, true));
    }

    private void initUI(ActivityItemBinding binding) {

//        String title = getIntent().getStringExtra(Constants.ITEM_NAME);
//
//        initToolbar(binding.toolbar, title);

        // setup Fragment
        setupFragment(new ItemFragment());

    }

    //for google login on activity result
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if(fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }

    }
}
