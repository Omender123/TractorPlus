package com.prospective.tractorplus.viewmodel.image;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.prospective.tractorplus.repository.image.ImageRepository;
import com.prospective.tractorplus.utils.AbsentLiveData;
import com.prospective.tractorplus.utils.Constants;
import com.prospective.tractorplus.viewmodel.common.PSViewModel;
import com.prospective.tractorplus.viewobject.Image;
import com.prospective.tractorplus.viewobject.common.Resource;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by Panacea-Soft on 12/8/17.
 * Contact Email : teamps.is.cool@gmail.com
 */


public class ImageViewModel extends PSViewModel {


    //region Variables

    // Get Image Video List
    private final LiveData<Resource<List<Image>>> imageListLiveData;
    private MutableLiveData<TmpDataHolder> imageParentObj = new MutableLiveData<>();

    private final LiveData<Resource<Boolean>> deleteImageData;
    private final MutableLiveData<DeleteImageTmpDataHolder> deleteImageObj = new MutableLiveData<>();

    public String id;
    public List<Image> newsImageList;
    public String imgId;
    public String imgType;
    public String CAMERA_TYPE = Constants.EMPTY_STRING;

    //endregion


    //region Constructors

    @Inject
    ImageViewModel(ImageRepository repository) {

        imageListLiveData = Transformations.switchMap(imageParentObj, obj -> {
            if (obj == null) {
                return AbsentLiveData.create();
            }
            return repository.getImageList(obj.imgType, obj.imageParentId);
        });

        deleteImageData = Transformations.switchMap(deleteImageObj, obj -> {

            if (obj == null) {
                return AbsentLiveData.create();
            }

            return repository.deletePostImage(obj.limit, obj.offset, obj.loginUserId, obj.imageId);

        });

    }

    //endregion


    //region Methods

    public void setImageParentId(String imageType, String imageParentId) {
        TmpDataHolder tmpDataHolder = new TmpDataHolder(imageType, imageParentId);
        this.imageParentObj.setValue(tmpDataHolder);
    }

    public LiveData<Resource<List<Image>>> getImageListLiveData() {
        return imageListLiveData;
    }

    //delete image

    public void setDeleteImageObj(String limit, String offset, String loginUserId, String imageId) {

        DeleteImageTmpDataHolder deleteImageTmpDataHolder = new DeleteImageTmpDataHolder(limit, offset, loginUserId, imageId);

        this.deleteImageObj.setValue(deleteImageTmpDataHolder);

    }

    public LiveData<Resource<Boolean>> getDeleteImageStatus() {
        return deleteImageData;
    }

    //endregion


    class TmpDataHolder {
        String imgType ;
        String imageParentId ;

        public TmpDataHolder(String imgType, String imageParentId) {
            this.imgType = imgType;
            this.imageParentId = imageParentId;
        }
    }

    class DeleteImageTmpDataHolder {

       String limit, offset, loginUserId, imageId;

        private DeleteImageTmpDataHolder(String limit, String offset, String loginUserId, String imageId) {
            this.limit = limit;
            this.offset = offset;
            this.loginUserId = loginUserId;
            this.imageId = imageId;

        }
    }

    //endregion

}
