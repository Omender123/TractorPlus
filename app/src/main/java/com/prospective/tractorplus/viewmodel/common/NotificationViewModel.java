package com.prospective.tractorplus.viewmodel.common;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.prospective.tractorplus.repository.aboutus.AboutUsRepository;
import com.prospective.tractorplus.ui.common.BackgroundTaskHandler;
import com.prospective.tractorplus.ui.common.NotificationTaskHandler;
import com.prospective.tractorplus.utils.Utils;

import javax.inject.Inject;

/**
 * Created by Panacea-Soft on 1/4/18.
 * Contact Email : teamps.is.cool@gmail.com
 */

public class NotificationViewModel extends ViewModel {

    private final NotificationTaskHandler backgroundTaskHandler;

    public boolean pushNotificationSetting = false;
    public boolean isLoading = false;

    @Inject
    NotificationViewModel(AboutUsRepository repository) {
        Utils.psLog("Inside NewsViewModel");

        backgroundTaskHandler = new NotificationTaskHandler(repository);
    }

    public void registerNotification(Context context, String platform, String token, String loginUserId) {

        if(token == null || platform == null) return;

        if(platform.equals("")) return;

        backgroundTaskHandler.registerNotification(context, platform, token, loginUserId);
    }

    public void unregisterNotification(Context context, String platform, String token, String loginUserId) {

        if(token == null || platform == null) return;

        if(platform.equals("")) return;

        backgroundTaskHandler.unregisterNotification(context, platform, token, loginUserId);
    }

    public LiveData<BackgroundTaskHandler.LoadingState> getLoadingStatus() {
        return backgroundTaskHandler.getLoadingState();
    }



}
