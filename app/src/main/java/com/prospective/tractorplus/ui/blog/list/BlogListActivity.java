package com.prospective.tractorplus.ui.blog.list;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.databinding.DataBindingUtil;

import com.prospective.tractorplus.Config;
import com.prospective.tractorplus.R;
import com.prospective.tractorplus.databinding.ActivityBlogListBinding;
import com.prospective.tractorplus.ui.common.PSAppCompactActivity;
import com.prospective.tractorplus.utils.Constants;
import com.prospective.tractorplus.utils.MyContextWrapper;

public class BlogListActivity extends PSAppCompactActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityBlogListBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_blog_list);

        initUI(binding);

    }

    @Override
    protected void attachBaseContext(Context newBase) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(newBase);
        String CURRENT_LANG_CODE = preferences.getString(Constants.LANGUAGE_CODE, Config.DEFAULT_LANGUAGE);
        String CURRENT_LANG_COUNTRY_CODE = preferences.getString(Constants.LANGUAGE_COUNTRY_CODE, Config.DEFAULT_LANGUAGE_COUNTRY_CODE);

        super.attachBaseContext(MyContextWrapper.wrap(newBase, CURRENT_LANG_CODE, CURRENT_LANG_COUNTRY_CODE, true));
    }

    private void initUI(ActivityBlogListBinding binding) {

        // Toolbar
        initToolbar(binding.toolbar, getResources().getString(R.string.blog_list__title));

        // setup Fragment

        setupFragment(new BlogListFragment());

    }
}
