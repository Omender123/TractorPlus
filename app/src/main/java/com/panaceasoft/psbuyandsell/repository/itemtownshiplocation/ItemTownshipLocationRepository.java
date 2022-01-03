package com.panaceasoft.psbuyandsell.repository.itemtownshiplocation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.panaceasoft.psbuyandsell.AppExecutors;
import com.panaceasoft.psbuyandsell.Config;
import com.panaceasoft.psbuyandsell.api.ApiResponse;
import com.panaceasoft.psbuyandsell.api.PSApiService;
import com.panaceasoft.psbuyandsell.db.ItemTownshipLocationDao;
import com.panaceasoft.psbuyandsell.db.PSCoreDb;
import com.panaceasoft.psbuyandsell.repository.common.NetworkBoundResource;
import com.panaceasoft.psbuyandsell.repository.common.PSRepository;
import com.panaceasoft.psbuyandsell.utils.Utils;
import com.panaceasoft.psbuyandsell.viewobject.ItemTownshipLocation;
import com.panaceasoft.psbuyandsell.viewobject.common.Resource;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ItemTownshipLocationRepository extends PSRepository {
    private ItemTownshipLocationDao itemTownshipLocationDao;


    @Inject
    ItemTownshipLocationRepository(PSApiService psApiService, AppExecutors appExecutors, PSCoreDb db, ItemTownshipLocationDao itemTownshipLocationDao) {

        super(psApiService, appExecutors, db);
        this.itemTownshipLocationDao = itemTownshipLocationDao;
    }


    public LiveData<Resource<List<ItemTownshipLocation>>> getItemTownshipLocationList(String limit, String offset,String loginUserId,String keyword,String orderBy,String orderType,String cityId) {
        return new NetworkBoundResource<List<ItemTownshipLocation>, List<ItemTownshipLocation>>(appExecutors) {
            @Override
            protected void saveCallResult(@NonNull List<ItemTownshipLocation> itemList) {

                Utils.psLog("SaveCallResult of getSubCategoryList.");

                try {
                    db.runInTransaction(() -> {
                        itemTownshipLocationDao.deleteAllItemLocation();

                        itemTownshipLocationDao.insertAll(itemList);

//                        for (ItemTownshipLocation location : itemList) {
//                            itemTownshipLocationDao.insert(new ItemTownshipLocation(location.id, location.cityId, location.townshipName, location.ordering, location.lat, location.lng, location.status, location.addedDate, location.addedDateStr));
//                        }
                    });
                } catch (Exception ex) {
                    Utils.psErrorLog("Error at ", ex);
                }
            }

            @Override
            protected boolean shouldFetch(@Nullable List<ItemTownshipLocation> data) {
                return connectivity.isConnected();
            }

            @NonNull
            @Override
            protected LiveData<List<ItemTownshipLocation>> loadFromDb() {
                return itemTownshipLocationDao.getSubCityList(cityId);

            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<List<ItemTownshipLocation>>> createCall() {
                return psApiService.getItemTownshipLocationList(Config.API_KEY, limit,offset,Utils.checkUserId(loginUserId),keyword,orderBy,orderType,cityId);
            }

            @Override
            protected void onFetchFailed(int code, String message) {
                Utils.psLog("Fetch Failed of " + message);

                if (code == Config.ERROR_CODE_10001) {
                    try {
                        appExecutors.diskIO().execute(() -> db.runInTransaction(() -> db.itemSubCategoryDao().deleteAllSubCategory()));

                    } catch (Exception ex) {
                        Utils.psErrorLog("Error at ", ex);
                    }
                }
            }

        }.asLiveData();
    }

    public LiveData<Resource<Boolean>> getNextPageTownshipLocationList(String limit, String offset,String loginUserId,String keyword,String orderBy,String orderType,String cityId) {

        final MediatorLiveData<Resource<Boolean>> statusLiveData = new MediatorLiveData<>();
        LiveData<ApiResponse<List<ItemTownshipLocation>>> apiResponse = psApiService.getItemTownshipLocationList(Config.API_KEY, limit,offset,Utils.checkUserId(loginUserId),keyword,orderBy,orderType,cityId);

        statusLiveData.addSource(apiResponse, response -> {

            statusLiveData.removeSource(apiResponse);

            //noinspection Constant Conditions
            if (response.isSuccessful()) {

                appExecutors.diskIO().execute(() -> {

                    try {
                        db.runInTransaction(() -> {
                            if (apiResponse.getValue() != null) {

                                itemTownshipLocationDao.insertAll(apiResponse.getValue().body);
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