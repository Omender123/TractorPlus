package com.example.tractorplus.repository.itemcurrency;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.tractorplus.AppExecutors;
import com.example.tractorplus.Config;
import com.example.tractorplus.api.ApiResponse;
import com.example.tractorplus.api.PSApiService;
import com.example.tractorplus.db.ItemCurrencyDao;
import com.example.tractorplus.db.PSCoreDb;
import com.example.tractorplus.repository.common.NetworkBoundResource;
import com.example.tractorplus.repository.common.PSRepository;
import com.example.tractorplus.utils.Utils;
import com.example.tractorplus.viewobject.ItemCurrency;
import com.example.tractorplus.viewobject.common.Resource;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ItemCurrencyTypeRepository extends PSRepository {
    private ItemCurrencyDao itemCurrencyDao;

    @Inject
    ItemCurrencyTypeRepository(PSApiService psApiService, AppExecutors appExecutors, PSCoreDb db, ItemCurrencyDao itemCurrencyDao) {

        super(psApiService, appExecutors, db);
        this.itemCurrencyDao = itemCurrencyDao;
    }

    public LiveData<Resource<List<ItemCurrency>>> getAllItemCurrencyList(String limit, String offset) {

        return new NetworkBoundResource<List<ItemCurrency>, List<ItemCurrency>>(appExecutors) {

            @Override
            protected void saveCallResult(@NonNull List<ItemCurrency> itemTypeList) {
                Utils.psLog("SaveCallResult of getAllCategoriesWithUserId");

                try {
                    db.runInTransaction(() -> {
                        itemCurrencyDao.deleteAllItemCurrency();

                        itemCurrencyDao.insertAll(itemTypeList);
                    });
                } catch (Exception ex) {
                    Utils.psErrorLog("Error at ", ex);
                }
            }


            @Override
            protected boolean shouldFetch(@Nullable List<ItemCurrency> data) {

                return connectivity.isConnected();
            }

            @NonNull
            @Override
            protected LiveData<List<ItemCurrency>> loadFromDb() {

                Utils.psLog("Load From Db (All Categories)");

                return itemCurrencyDao.getAllItemCurrency();

            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<List<ItemCurrency>>> createCall() {
                Utils.psLog("Call Get All Categories webservice.");

                return psApiService.getItemCurrencyTypeList(Config.API_KEY, limit, offset);
            }

            @Override
            protected void onFetchFailed(int code, String message) {
                Utils.psLog("Fetch Failed of About Us");
                if (code == Config.ERROR_CODE_10001) {
                    try {
                        appExecutors.diskIO().execute(() -> db.runInTransaction(() -> db.itemCurrencyDao().deleteAllItemCurrency()));

                    } catch (Exception ex) {
                        Utils.psErrorLog("Error at ", ex);
                    }
                }
            }

        }.asLiveData();
    }

    public LiveData<Resource<Boolean>> getNextPageItemCurrencyList( String limit, String offset) {

        final MediatorLiveData<Resource<Boolean>> statusLiveData = new MediatorLiveData<>();
        LiveData<ApiResponse<List<ItemCurrency>>> apiResponse = psApiService.getItemCurrencyTypeList(Config.API_KEY, limit, offset);

        statusLiveData.addSource(apiResponse, response -> {

            statusLiveData.removeSource(apiResponse);

            //noinspection Constant Conditions
            if (response.isSuccessful()) {

                appExecutors.diskIO().execute(() -> {

                    try {
                        db.runInTransaction(() -> {
                            if (apiResponse.getValue() != null) {

                                itemCurrencyDao.insertAll(apiResponse.getValue().body);
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