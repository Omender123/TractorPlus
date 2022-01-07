package com.example.tractorplus.repository.apploading;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tractorplus.AppExecutors;
import com.example.tractorplus.Config;
import com.example.tractorplus.api.ApiResponse;
import com.example.tractorplus.api.PSApiService;
import com.example.tractorplus.db.PSCoreDb;
import com.example.tractorplus.repository.common.PSRepository;
import com.example.tractorplus.utils.Constants;
import com.example.tractorplus.utils.Utils;
import com.example.tractorplus.viewobject.DeletedObject;
import com.example.tractorplus.viewobject.PSAppInfo;
import com.example.tractorplus.viewobject.common.Resource;

import java.io.IOException;

import javax.inject.Inject;

import retrofit2.Response;

public class AppLoadingRepository extends PSRepository {

    @Inject
    AppLoadingRepository(PSApiService psApiService, AppExecutors appExecutors, PSCoreDb db) {
        super(psApiService, appExecutors, db);
    }

    public LiveData<Resource<PSAppInfo>> deleteTheSpecificObjects(String startDate, String endDate,String user_id) {

        final MutableLiveData<Resource<PSAppInfo>> statusLiveData = new MutableLiveData<>();

        appExecutors.networkIO().execute(() -> {

            Response<PSAppInfo> response;

            try {
                response = psApiService.getDeletedHistory(Config.API_KEY, startDate, endDate,Utils.checkUserId(user_id)).execute();

                ApiResponse<PSAppInfo> apiResponse = new ApiResponse<>(response);

                if (apiResponse.isSuccessful()) {

                    try {
                        db.runInTransaction(() -> {
                            if (apiResponse.body != null) {

                                if (apiResponse.body.deletedObjects.size() > 0) {
                                    for (DeletedObject deletedObject : apiResponse.body.deletedObjects) {
                                        switch (deletedObject.typeName) {
//                                        case Constants.APPINFO_NAME_SHOP:
//                                            db.shopDao().deleteShopById(deletedObject.id);
//                                            db.aboutUsDao().deleteById(deletedObject.id);

//                                            break;
                                            case Constants.APPINFO_NAME_ITEM:
//                                            db.productDao().deleteProductById(deletedObject.id);
//                                            db.historyDao().deleteHistoryProductById(deletedObject.id);
//                                            db.aboutUsDao().deleteById(deletedObject.id);

                                                break;
                                            case Constants.APPINFO_NAME_CATEGORY:
//                                            db.categoryDao().deleteCategoryById(deletedObject.id);
//                                            db.aboutUsDao().deleteById(deletedObject.id);
                                                break;
                                        }
                                    }
                                }
                            }
                        });
                    } catch (Exception ex) {
                        Utils.psErrorLog("Error at ", ex);
                    }

                    statusLiveData.postValue(Resource.success(apiResponse.body));

                }


                else {
                    statusLiveData.postValue(Resource.error(apiResponse.errorMessage, null));
                }

            } catch (IOException e) {
                statusLiveData.postValue(Resource.error(e.getMessage(), null));
            }

        });

        return statusLiveData;
    }
}

