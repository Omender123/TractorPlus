package com.prospective.tractorplus.repository.city;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.prospective.tractorplus.AppExecutors;
import com.prospective.tractorplus.Config;
import com.prospective.tractorplus.api.ApiResponse;
import com.prospective.tractorplus.api.PSApiService;
import com.prospective.tractorplus.db.PSCoreDb;
import com.prospective.tractorplus.repository.common.NetworkBoundResource;
import com.prospective.tractorplus.repository.common.PSRepository;
import com.prospective.tractorplus.utils.Utils;
import com.prospective.tractorplus.viewobject.City;
import com.prospective.tractorplus.viewobject.CityMap;
import com.prospective.tractorplus.viewobject.common.Resource;
import com.prospective.tractorplus.viewobject.holder.CityParameterHolder;

import java.util.List;

import javax.inject.Inject;

public class CityRepository extends PSRepository {

    @Inject
    protected SharedPreferences pref;

    @Inject
    public CityRepository(PSApiService psApiService, AppExecutors appExecutors, PSCoreDb db) {
        super(psApiService, appExecutors, db);

        Utils.psLog("Inside CityRepository");
    }


    public LiveData<Resource<List<City>>> getCityListByValue(String limit, String offset, CityParameterHolder parameterHolder) {

        return new NetworkBoundResource<List<City>, List<City>>(appExecutors) {

            @Override
            protected void saveCallResult(@NonNull List<City> itemList) {
                Utils.psLog("SaveCallResult of getCityList.");

                try {
                    db.runInTransaction(() -> {
                        String mapKey = parameterHolder.getCityMapKey();

                        db.cityMapDao().deleteByMapKey(mapKey);

                        db.cityDao().insertAll(itemList);

                        String dateTime = Utils.getDateTime();

                        for (int i = 0; i < itemList.size(); i++) {
                            db.cityMapDao().insert(new CityMap(mapKey + itemList.get(i).id, mapKey, itemList.get(i).id, i + 1, dateTime));
                        }
                    });
                } catch (Exception ex) {
                    Utils.psErrorLog("Error at getCityListByValue", ex);
                }
            }

            @Override
            protected boolean shouldFetch(@Nullable List<City> data) {

                // Recent news always load from server
                return connectivity.isConnected();
            }

            @NonNull
            @Override
            protected LiveData<List<City>> loadFromDb() {
                Utils.psLog("getCityList From Db");

                String mapKey = parameterHolder.getCityMapKey();

                return db.cityDao().getCityListByMapKey(mapKey);

            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<List<City>>> createCall() {
                Utils.psLog("Call API Service to getCityList.");

                return psApiService.searchCity(Config.API_KEY, limit, offset, parameterHolder.id, parameterHolder.keyword, parameterHolder.isFeatured, parameterHolder.orderBy, parameterHolder.orderType);

            }

            @Override
            protected void onFetchFailed(int code, String message) {
                Utils.psLog("Fetch Failed (getCityList) : " + message);

                if (code == Config.ERROR_CODE_10001) {
                    try {
                        appExecutors.diskIO().execute(() -> {
                            String mapKey = parameterHolder.getCityMapKey();

                            db.cityMapDao().deleteByMapKey(mapKey);
                        });

                    } catch (Exception ex) {
                        Utils.psErrorLog("Error at ", ex);
                    }
                }
            }
        }.asLiveData();
    }

    public LiveData<Resource<Boolean>> getNextPageCityList(String limit, String offset, CityParameterHolder parameterHolder) {

        final MediatorLiveData<Resource<Boolean>> statusLiveData = new MediatorLiveData<>();

        LiveData<ApiResponse<List<City>>> apiResponse = psApiService.searchCity(Config.API_KEY, limit, offset, parameterHolder.id, parameterHolder.keyword, parameterHolder.isFeatured, parameterHolder.orderBy, parameterHolder.orderType);

        statusLiveData.addSource(apiResponse, response -> {

            statusLiveData.removeSource(apiResponse);

            //noinspection Constant Conditions
            if (response.isSuccessful()) {

                appExecutors.diskIO().execute(() -> {

                    if (response.body != null) {

                        try {
                            db.runInTransaction(() -> {
                                db.cityDao().insertAll(response.body);

                                int finalIndex = db.cityMapDao().getMaxSortingByValue(parameterHolder.getCityMapKey());

                                int startIndex = finalIndex + 1;

                                String mapKey = parameterHolder.getCityMapKey();
                                String dateTime = Utils.getDateTime();

                                for (int i = 0; i < response.body.size(); i++) {
                                    db.cityMapDao().insert(new CityMap(mapKey + response.body.get(i).id, mapKey, response.body.get(i).id, startIndex + i, dateTime));
                                }
                            });
                        } catch (Exception ex) {
                            Utils.psErrorLog("Error at getNextPageCityList", ex);
                        }

                        statusLiveData.postValue(Resource.success(true));

                    } else {
                        statusLiveData.postValue(Resource.error(response.errorMessage, null));
                    }
                });

            } else {
                statusLiveData.postValue(Resource.error(response.errorMessage, null));
            }
        });

        return statusLiveData;
    }
}
