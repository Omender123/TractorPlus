package com.prospective.tractorplus.repository.image;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.prospective.tractorplus.AppExecutors;
import com.prospective.tractorplus.Config;
import com.prospective.tractorplus.api.ApiResponse;
import com.prospective.tractorplus.api.PSApiService;
import com.prospective.tractorplus.db.ImageDao;
import com.prospective.tractorplus.db.PSCoreDb;
import com.prospective.tractorplus.repository.common.NetworkBoundResource;
import com.prospective.tractorplus.repository.common.PSRepository;
import com.prospective.tractorplus.utils.Utils;
import com.prospective.tractorplus.viewobject.ApiStatus;
import com.prospective.tractorplus.viewobject.Image;
import com.prospective.tractorplus.viewobject.common.Resource;
import com.prospective.tractorplus.viewobject.holder.ItemParameterHolder;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Response;

/**
 * Created by Panacea-Soft on 12/8/17.
 * Contact Email : teamps.is.cool@gmail.com
 */

@Singleton
public class ImageRepository extends PSRepository {


    //region Variables

    private final ImageDao imageDao;

    //endregion


    //region Constructor

    @Inject
    ImageRepository(PSApiService psApiService, AppExecutors appExecutors, PSCoreDb db, ImageDao imageDao) {
        super(psApiService, appExecutors, db);

        this.imageDao = imageDao;
    }

    //endregion


    //region News Repository Functions for ViewModel

    /**
     * Load Image List
     *
     * @param imgType     Image Type
     * @param imgParentId Image Parent Id
     * @return Image List filter by news id
     */
    public LiveData<Resource<List<Image>>> getImageList(String imgType, String imgParentId) {

        String functionKey = "getImageList" + imgParentId;

        return new NetworkBoundResource<List<Image>, List<Image>>(appExecutors) {

            @Override
            protected void saveCallResult(@NonNull List<Image> item) {
                Utils.psLog("SaveCallResult of getImageList.");

//                db.beginTransaction();
//                try {
//                    imageDao.deleteByImageIdAndType(imgParentId, imgType);
//
//                    imageDao.insertAll(item);
//
//                    db.setTransactionSuccessful();
//                } catch (Exception e) {
//                    Utils.psErrorLog("Error", e);
//                } finally {
//                    db.endTransaction();
//                }

                try {
                    db.runInTransaction(() -> {
                        imageDao.deleteByImageIdAndType(imgParentId, imgType);

                        imageDao.insertAll(item);
                    });
                } catch (Exception ex) {
                    Utils.psErrorLog("Error at ", ex);
                }
            }

            @Override
            protected boolean shouldFetch(@Nullable List<Image> data) {
                return connectivity.isConnected();
            }

            @NonNull
            @Override
            protected LiveData<List<Image>> loadFromDb() {
                Utils.psLog("Load image list from db");
                return imageDao.getByImageIdAndType(imgParentId, imgType);
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<List<Image>>> createCall() {
                Utils.psLog("Call API webservice to get image list." + psApiService.getImageList(Config.API_KEY, imgParentId, imgType));
                return psApiService.getImageList(Config.API_KEY, imgParentId, imgType);
            }

            @Override
            protected void onFetchFailed(int code, String message) {
                Utils.psLog("Fetch Failed of getting image list.");
                rateLimiter.reset(functionKey);

                if (code == Config.ERROR_CODE_10001) {
                    try {
                        appExecutors.diskIO().execute(() -> db.runInTransaction(() -> db.imageDao().deleteByImageIdAndType(imgParentId, imgType)));

                    } catch (Exception ex) {
                        Utils.psErrorLog("Error at ", ex);
                    }
                }
            }

        }.asLiveData();
    }
    //endregion

    //region delete image post

    public LiveData<Resource<Boolean>> deletePostImage(String limit, String offset, String loginUserId, String imageId) {

        final MutableLiveData<Resource<Boolean>> statusLiveData = new MutableLiveData<>();

        appExecutors.networkIO().execute(() -> {

            Response<ApiStatus> response;

            try {
                response = psApiService.deleteImage(
                        Config.API_KEY, limit, offset, loginUserId, imageId).execute();

                ApiResponse<ApiStatus> apiResponse = new ApiResponse<>(response);

                if (apiResponse.isSuccessful()) {

                    try {
                        db.runInTransaction(() -> {
                            imageDao.deleteImageById(imageId);

                            ItemParameterHolder holder = new ItemParameterHolder();
                            holder.userId = loginUserId;

                            String key = holder.getItemMapKey();
                            db.itemMapDao().deleteByMapKeyAndImageId(key, imageId);

                        });
                    } catch (Exception ex) {
                        Utils.psErrorLog("Error at ", ex);
                    }

                    statusLiveData.postValue(Resource.success(true));

                } else {
                    statusLiveData.postValue(Resource.error(apiResponse.errorMessage, false));
                }

            } catch (IOException e) {
                statusLiveData.postValue(Resource.error(e.getMessage(), false));
            }

        });

        return statusLiveData;
    }

    //endregion


}
