package com.example.tractorplus.repository.itemdealoption;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.tractorplus.AppExecutors;
import com.example.tractorplus.Config;
import com.example.tractorplus.api.ApiResponse;
import com.example.tractorplus.api.PSApiService;
import com.example.tractorplus.db.ItemDealOptionDao;
import com.example.tractorplus.db.PSCoreDb;
import com.example.tractorplus.repository.common.NetworkBoundResource;
import com.example.tractorplus.repository.common.PSRepository;
import com.example.tractorplus.utils.Utils;
import com.example.tractorplus.viewobject.ItemDealOption;
import com.example.tractorplus.viewobject.common.Resource;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ItemDealOptionRepository extends PSRepository {
    private ItemDealOptionDao itemDealOptionDao;

    @Inject
    ItemDealOptionRepository(PSApiService psApiService, AppExecutors appExecutors, PSCoreDb db, ItemDealOptionDao itemDealOptionDao) {

        super(psApiService, appExecutors, db);
        this.itemDealOptionDao = itemDealOptionDao;
    }

    public LiveData<Resource<List<ItemDealOption>>> getAllItemDealOptionList(String limit, String offset) {

        return new NetworkBoundResource<List<ItemDealOption>, List<ItemDealOption>>(appExecutors) {

            @Override
            protected void saveCallResult(@NonNull List<ItemDealOption> itemTypeList) {
                Utils.psLog("SaveCallResult of getAllCategoriesWithUserId");

                try {
                    db.runInTransaction(() -> {
                        itemDealOptionDao.deleteAllItemDealOption();

                        itemDealOptionDao.insertAll(itemTypeList);
                    });
                } catch (Exception ex) {
                    Utils.psErrorLog("Error at ", ex);
                }
            }


            @Override
            protected boolean shouldFetch(@Nullable List<ItemDealOption> data) {

                return connectivity.isConnected();
            }

            @NonNull
            @Override
            protected LiveData<List<ItemDealOption>> loadFromDb() {

                Utils.psLog("Load From Db (All Categories)");

                return itemDealOptionDao.getAllItemDealOption();

            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<List<ItemDealOption>>> createCall() {
                Utils.psLog("Call Get All Categories webservice.");

                return psApiService.getItemDealOptionList(Config.API_KEY, limit, offset);
            }

            @Override
            protected void onFetchFailed(int code, String message) {
                Utils.psLog("Fetch Failed of About Us");

                if (code == Config.ERROR_CODE_10001) {
                    try {
                        appExecutors.diskIO().execute(() -> db.runInTransaction(() -> db.itemDealOptionDao().deleteAllItemDealOption()));

                    } catch (Exception ex) {
                        Utils.psErrorLog("Error at ", ex);
                    }
                }
            }

        }.asLiveData();
    }

    public LiveData<Resource<Boolean>> getNextPageItemDealOptionList( String limit, String offset) {

        final MediatorLiveData<Resource<Boolean>> statusLiveData = new MediatorLiveData<>();
        LiveData<ApiResponse<List<ItemDealOption>>> apiResponse = psApiService.getItemDealOptionList(Config.API_KEY, limit, offset);

        statusLiveData.addSource(apiResponse, response -> {

            statusLiveData.removeSource(apiResponse);

            //noinspection Constant Conditions
            if (response.isSuccessful()) {

                appExecutors.diskIO().execute(() -> {

                    try {
                        db.runInTransaction(() -> {
                            if (apiResponse.getValue() != null) {

                                itemDealOptionDao.insertAll(apiResponse.getValue().body);
                            }
                        });
                    } catch (Exception ex) {
                        Utils.psErrorLog("Error at ", ex);
                    }

                    statusLiveData.postValue(Resource.success(true));

                });
            } else {
                statusLiveData.postValue(Resource.error(response.errorMessage, null));
            }

        });

        return statusLiveData;

    }

}