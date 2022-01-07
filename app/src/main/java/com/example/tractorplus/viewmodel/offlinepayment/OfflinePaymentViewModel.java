package com.example.tractorplus.viewmodel.offlinepayment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.example.tractorplus.Config;
import com.example.tractorplus.repository.offlinepayment.OfflinePaymentRepository;
import com.example.tractorplus.utils.AbsentLiveData;
import com.example.tractorplus.utils.Utils;
import com.example.tractorplus.viewmodel.common.PSViewModel;
import com.example.tractorplus.viewobject.OfflinePaymentMethodHeader;
import com.example.tractorplus.viewobject.common.Resource;


import javax.inject.Inject;

public class OfflinePaymentViewModel extends PSViewModel {

    //region Variables

    private final LiveData<Resource<OfflinePaymentMethodHeader>> offlinePaymentHeaderData;
    private MutableLiveData<TmpDataHolder> offlinePaymetHeadertObj = new MutableLiveData<>();

    //endregion

    //region Constructors

    @Inject
    OfflinePaymentViewModel(OfflinePaymentRepository repository) {

        Utils.psLog("OfflinePaymentViewModel");

        offlinePaymentHeaderData = Transformations.switchMap(offlinePaymetHeadertObj, obj -> {
            if (obj == null) {
                return AbsentLiveData.create();
            }
            return repository.getOfflinePaymentHeaderList(Config.API_KEY, obj.limit, obj.offset);
        });


    }

    public void setOfflinePaymentHeaderListObj( String limit, String offset) {
        if (!isLoading) {
            TmpDataHolder tmpDataHolder = new TmpDataHolder();
            tmpDataHolder.limit = limit;
            tmpDataHolder.offset = offset;
            offlinePaymetHeadertObj.setValue(tmpDataHolder);

            // start loading
            setLoadingState(true);
        }
    }

    public LiveData<Resource<OfflinePaymentMethodHeader>> getOfflinePaymentHeaderData() {
        return offlinePaymentHeaderData;
    }

    class TmpDataHolder {
        public String limit = "";
        public String offset = "";
    }
}
