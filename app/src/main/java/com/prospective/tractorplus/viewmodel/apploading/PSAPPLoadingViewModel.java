package com.prospective.tractorplus.viewmodel.apploading;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.prospective.tractorplus.repository.apploading.AppLoadingRepository;
import com.prospective.tractorplus.utils.AbsentLiveData;
import com.prospective.tractorplus.utils.Utils;
import com.prospective.tractorplus.viewmodel.common.PSViewModel;
import com.prospective.tractorplus.viewobject.PSAppInfo;
import com.prospective.tractorplus.viewobject.common.Resource;

import javax.inject.Inject;

public class PSAPPLoadingViewModel extends PSViewModel {

    private final LiveData<Resource<PSAppInfo>> deleteHistoryData;
    private MutableLiveData<TmpDataHolder> deleteHistoryObj = new MutableLiveData<>();
    public PSAppInfo psAppInfo;
    public String stripePublishableKey;
    public String payStackKey;
    public String currencyShortForm;
    public String defaultCurrencyId;
    public String defaultCurrency;
    public String inAppPurchasedPrdIdAndroid;
    public String appSettingLat;
    public String appSettingLng;

    @Inject
    PSAPPLoadingViewModel(AppLoadingRepository repository) {
        deleteHistoryData = Transformations.switchMap(deleteHistoryObj, obj -> {
            if (obj == null) {
                return AbsentLiveData.create();
            }
            Utils.psLog("PSAppInfoViewModel");
            return repository.deleteTheSpecificObjects(obj.startDate, obj.endDate,obj.user_id);
        });
    }

    public void setDeleteHistoryObj(String startDate, String endDate,String user_id) {

        TmpDataHolder tmpDataHolder = new TmpDataHolder(startDate, endDate,user_id);

        this.deleteHistoryObj.setValue(tmpDataHolder);
    }

    public LiveData<Resource<PSAppInfo>> getDeleteHistoryData() {
        return deleteHistoryData;
    }

    class TmpDataHolder {
        String startDate, endDate, user_id;

        private TmpDataHolder(String startDate, String endDate,String user_id) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.user_id=user_id;
        }
    }

}

