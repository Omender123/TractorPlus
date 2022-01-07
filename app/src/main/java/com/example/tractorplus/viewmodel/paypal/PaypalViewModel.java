package com.example.tractorplus.viewmodel.paypal;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.example.tractorplus.repository.paypal.PaypalRepository;
import com.example.tractorplus.utils.AbsentLiveData;
import com.example.tractorplus.utils.Utils;
import com.example.tractorplus.viewmodel.common.PSViewModel;
import com.example.tractorplus.viewobject.common.Resource;

import javax.inject.Inject;

public class PaypalViewModel extends PSViewModel {

    private final LiveData<Resource<Boolean>> paypalTokenData;
    private MutableLiveData<Boolean> paypalTokenObj = new MutableLiveData<>();


    @Inject
    PaypalViewModel(PaypalRepository repository) {
        paypalTokenData = Transformations.switchMap(paypalTokenObj, obj -> {
            if (obj == null) {
                return AbsentLiveData.create();
            }
            Utils.psLog("paypalTokenData");
            return repository.getPaypalToken();
        });
    }

    public void setPaypalTokenObj() {
        this.paypalTokenObj.setValue(true);
    }

    public LiveData<Resource<Boolean>> getPaypalTokenData() {
        return paypalTokenData;
    }

}