package com.prospective.tractorplus.repository.itemsubcategory;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.prospective.tractorplus.AppExecutors;
import com.prospective.tractorplus.Config;
import com.prospective.tractorplus.api.ApiResponse;
import com.prospective.tractorplus.api.PSApiService;
import com.prospective.tractorplus.db.ItemSubCategoryDao;
import com.prospective.tractorplus.db.PSCoreDb;
import com.prospective.tractorplus.repository.common.NetworkBoundResource;
import com.prospective.tractorplus.repository.common.PSRepository;
import com.prospective.tractorplus.utils.Utils;
import com.prospective.tractorplus.viewobject.ItemSubCategory;
import com.prospective.tractorplus.viewobject.common.Resource;
import com.prospective.tractorplus.viewobject.holder.SubCategoryParameterHolder;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ItemSubCategoryRepository extends PSRepository {

    private final ItemSubCategoryDao itemSubCategoryDao;

    @Inject
    ItemSubCategoryRepository(PSApiService psApiService, AppExecutors appExecutors, PSCoreDb db, ItemSubCategoryDao subCategoryDao) {
        super(psApiService, appExecutors, db);

        Utils.psLog("Inside SubCategoryRepository");

        this.itemSubCategoryDao = subCategoryDao;
    }

    public LiveData<Resource<List<ItemSubCategory>>> getAllItemSubCategoryList(String apiKey) {
        return new NetworkBoundResource<List<ItemSubCategory>, List<ItemSubCategory>>(appExecutors) {
            @Override
            protected void saveCallResult(@NonNull List<ItemSubCategory> itemList) {

                Utils.psLog("SaveCallResult of getAllSubCategoryList.");

//                try {
//                    db.beginTransaction();
//
//                    itemSubCategoryDao.deleteAllSubCategory();
//
//                    itemSubCategoryDao.insertAll(itemList);
//
//                    for (ItemSubCategory item : itemList) {
//                        itemSubCategoryDao.insert(new ItemSubCategory(item.id, item.name, item.status, item.addedDate, item.addedUserId, item.updatedDate, item.cityId, item.catId, item.deletedFlag, item.updatedUserId, item.updatedFlag, item.addedDateStr, item.defaultPhoto, item.defaultIcon));
//                    }
//
//                    db.setTransactionSuccessful();
//                } catch (Exception e) {
//                    Utils.psErrorLog("Error in doing transaction of getAllSubCategoryList.", e);
//                } finally {
//                    db.endTransaction();
//                }

                try {
                    db.runInTransaction(() -> {

                        itemSubCategoryDao.deleteAllSubCategory();

                        itemSubCategoryDao.insertAll(itemList);

                        for (ItemSubCategory item : itemList) {
                            itemSubCategoryDao.insert(new ItemSubCategory(item.id, item.name, item.status, item.addedDate, item.addedUserId, item.updatedDate, item.cityId, item.catId, item.deletedFlag, item.updatedUserId, item.updatedFlag, item.addedDateStr, item.defaultPhoto, item.defaultIcon));
                        }
                    });
                } catch (Exception ex) {
                    Utils.psErrorLog("Error at ", ex);
                }
            }

            @Override
            protected boolean shouldFetch(@Nullable List<ItemSubCategory> data) {
                return connectivity.isConnected();
            }

            @NonNull
            @Override
            protected LiveData<List<ItemSubCategory>> loadFromDb() {
                return itemSubCategoryDao.getAllSubCategory();
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<List<ItemSubCategory>>> createCall() {
                return psApiService.getAllSubCategoryList(apiKey);
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

    public LiveData<Resource<List<ItemSubCategory>>> getSubCategoryList(String apiKey, String loginUserId, String catId, String limit, String offset, SubCategoryParameterHolder subCategoryParameterHolder) {
        return new NetworkBoundResource<List<ItemSubCategory>, List<ItemSubCategory>>(appExecutors) {
            @Override
            protected void saveCallResult(@NonNull List<ItemSubCategory> itemList) {

                Utils.psLog("SaveCallResult of getSubCategoryList.");

                try {
                    db.runInTransaction(() -> {

                        String mapKey = subCategoryParameterHolder.getSubCategoryMapKey();

                        db.itemSubCategoryDao().deleteByMapKey(mapKey);

//                        itemSubCategoryDao.deleteAllSubCategory();

                        itemSubCategoryDao.insertAll(itemList);

                        for (ItemSubCategory item : itemList) {
                            itemSubCategoryDao.insert(new ItemSubCategory(item.id, item.name, item.status, item.addedDate, item.addedUserId, item.updatedDate, item.cityId, item.catId, item.deletedFlag, item.updatedUserId, item.updatedFlag, item.addedDateStr, item.defaultPhoto, item.defaultIcon));
                        }
                    });
                } catch (Exception ex) {
                    Utils.psErrorLog("Error at ", ex);
                }
            }

            @Override
            protected boolean shouldFetch(@Nullable List<ItemSubCategory> data) {
                return connectivity.isConnected();
            }

            @NonNull
            @Override
            protected LiveData<List<ItemSubCategory>> loadFromDb() {
                return itemSubCategoryDao.getSubCategoryList(catId);
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<List<ItemSubCategory>>> createCall() {
                return psApiService.getSubCategoryListWithCatId(apiKey, loginUserId, catId, limit, offset, subCategoryParameterHolder.order_by, subCategoryParameterHolder.order_type);
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

    public LiveData<Resource<Boolean>> getNextPageSubCategory(String loginUserId, String catId, String limit, String offset, SubCategoryParameterHolder subCategoryParameterHolder) {
        final MediatorLiveData<Resource<Boolean>> statusLiveData = new MediatorLiveData<>();
        LiveData<ApiResponse<List<ItemSubCategory>>> apiResponse = psApiService.getSubCategoryListWithCatId(Config.API_KEY,  loginUserId, catId, limit, offset, subCategoryParameterHolder.order_by, subCategoryParameterHolder.order_type);

        statusLiveData.addSource(apiResponse, response -> {

            statusLiveData.removeSource(apiResponse);

            //noinspection Constant Conditions
            if (response.isSuccessful()) {

                appExecutors.diskIO().execute(() -> {

                    try {
                        db.runInTransaction(() -> {
                            if (response.body != null) {
                                for (ItemSubCategory item : response.body) {
                                    itemSubCategoryDao.insert(new ItemSubCategory(item.id, item.name, item.status, item.addedDate, item.addedUserId, item.updatedDate, item.cityId, item.catId, item.deletedFlag, item.updatedUserId, item.updatedFlag, item.addedDateStr, item.defaultPhoto, item.defaultIcon));
                                }

                                db.itemSubCategoryDao().insertAll(response.body);
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


    public LiveData<Resource<List<ItemSubCategory>>> getSubCategoriesWithCatId(String loginUserId, String catId, String limit, String offset, SubCategoryParameterHolder subCategoryParameterHolder) {
        return new NetworkBoundResource<List<ItemSubCategory>, List<ItemSubCategory>>(appExecutors) {
            @Override
            protected void saveCallResult(@NonNull List<ItemSubCategory> itemList) {

                Utils.psLog("SaveCallResult of Sub Category.");

                try {
                    db.runInTransaction(() -> {

                        String mapKey = subCategoryParameterHolder.getSubCategoryMapKey();

                        db.itemSubCategoryDao().deleteByMapKey(mapKey);

                        itemSubCategoryDao.insertAll(itemList);

                        for (ItemSubCategory item : itemList) {
                            itemSubCategoryDao.insert(new ItemSubCategory(item.id, item.name, item.status, item.addedDate, item.addedUserId, item.updatedDate, item.cityId, item.catId, item.deletedFlag, item.updatedUserId, item.updatedFlag, item.addedDateStr, item.defaultPhoto, item.defaultIcon));
                        }
                    });
                } catch (Exception ex) {
                    Utils.psErrorLog("Error at ", ex);
                }
            }

            @Override
            protected boolean shouldFetch(@Nullable List<ItemSubCategory> data) {
                return connectivity.isConnected();
            }

            @NonNull
            @Override
            protected LiveData<List<ItemSubCategory>> loadFromDb() {
                return itemSubCategoryDao.getSubCategoryList(catId);
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<List<ItemSubCategory>>> createCall() {
                return psApiService.getSubCategoryListWithCatId(Config.API_KEY, loginUserId, catId, limit, offset, subCategoryParameterHolder.order_by, subCategoryParameterHolder.order_type);
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

    public LiveData<Resource<Boolean>> getNextPageSubCategoriesWithCatId(String limit, String offset, String catId, String loginUserId, SubCategoryParameterHolder subCategoryParameterHolder) {
        final MediatorLiveData<Resource<Boolean>> statusLiveData = new MediatorLiveData<>();
        LiveData<ApiResponse<List<ItemSubCategory>>> apiResponse = psApiService.getSubCategoryListWithCatId(Config.API_KEY, catId, limit, offset, loginUserId, subCategoryParameterHolder.order_by, subCategoryParameterHolder.order_type);

        statusLiveData.addSource(apiResponse, response -> {

            statusLiveData.removeSource(apiResponse);

            //noinspection Constant Conditions
            if (response.isSuccessful()) {

                appExecutors.diskIO().execute(() -> {

                    try {
                        db.runInTransaction(() -> {
                            if (response.body != null) {
                                for (ItemSubCategory item : response.body) {
                                    itemSubCategoryDao.insert(new ItemSubCategory(item.id, item.name, item.status, item.addedDate, item.addedUserId, item.updatedDate, item.cityId, item.catId, item.deletedFlag, item.updatedUserId, item.updatedFlag, item.addedDateStr, item.defaultPhoto, item.defaultIcon));
                                }

                                itemSubCategoryDao.insertAll(response.body);
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
