package com.example.tractorplus.ui.user.editphone;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.example.tractorplus.Config;
import com.example.tractorplus.R;
import com.example.tractorplus.databinding.ActivityPhoneEditBinding;
import com.example.tractorplus.ui.common.PSAppCompactActivity;
import com.example.tractorplus.utils.Constants;
import com.example.tractorplus.utils.MyContextWrapper;

public class EditPhoneActivity extends PSAppCompactActivity {


    //region Override Methods

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityPhoneEditBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_phone_edit);

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

    //endregion


    //region Private Methods

    private void initUI(ActivityPhoneEditBinding binding) {

        // Toolbar
        initToolbar(binding.toolbar, getResources().getString(R.string.profile_edit__phone_no_title));

        // setup Fragment
        setupFragment(new EditPhoneFragment());
        // Or you can call like this
        //setupFragment(new NewsListFragment(), R.id.content_frame);

    }

    //endregion

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