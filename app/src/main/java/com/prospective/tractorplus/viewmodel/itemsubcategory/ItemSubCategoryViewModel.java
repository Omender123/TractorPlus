package com.prospective.tractorplus.viewmodel.itemsubcategory;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.prospective.tractorplus.Config;
import com.prospective.tractorplus.repository.itemsubcategory.ItemSubCategoryRepository;
import com.prospective.tractorplus.utils.AbsentLiveData;
import com.prospective.tractorplus.utils.Utils;
import com.prospective.tractorplus.viewmodel.common.PSViewModel;
import com.prospective.tractorplus.viewobject.ItemSubCategory;
import com.prospective.tractorplus.viewobject.common.Resource;
import com.prospective.tractorplus.viewobject.holder.SubCategoryParameterHolder;

import java.util.List;

import javax.inject.Inject;

public class ItemSubCategoryViewModel extends PSViewModel {

    private LiveData<Resource<List<ItemSubCategory>>> allSubCategoryListData;
    private MutableLiveData<TmpDataHolder> allSubCategoryListObj = new MutableLiveData<>();

    private LiveData<Resource<List<ItemSubCategory>>> subCategoryListData;
    private MutableLiveData<TmpDataHolder> subCategoryListObj = new MutableLiveData<>();

    private LiveData<Resource<Boolean>> nextPageLoadingStateData;
    private MutableLiveData<TmpDataHolder> nextPageLoadingStateObj = new MutableLiveData<>();

    private final LiveData<Resource<List<ItemSubCategory>>> subCategoryListByCatIdData;
    private MutableLiveData<ListByCatIdTmpDataHolder> subCategoryListByCatIdObj = new MutableLiveData<>();

    private final LiveData<Resource<Boolean>> nextPageSubCategoryListByCatIdData;
    private MutableLiveData<ListByCatIdTmpDataHolder> nextPageSubCategoryListByCatIdObj = new MutableLiveData<>();

    public SubCategoryParameterHolder subCategoryParameterHolder = new SubCategoryParameterHolder();

    public String catId = "";

    @Inject
    ItemSubCategoryViewModel(ItemSubCategoryRepository repository) {
        Utils.psLog("Inside SubCategoryViewModel");

        allSubCategoryListData = Transformations.switchMap(allSubCategoryListObj, obj -> {
            if (obj == null) {
                return AbsentLiveData.create();
            }
            Utils.psLog("allSubCategoryListData");
            return repository.getAllItemSubCategoryList(Config.API_KEY);
        });

        subCategoryListData = Transformations.switchMap(subCategoryListObj, obj -> {
            if (obj == null) {
                return AbsentLiveData.create();
            }
            Utils.psLog("subCategoryListData");
            return repository.getSubCategoryList(Config.API_KEY, obj.loginUserId, obj.catId, obj.limit, obj.offset, obj.parameterHolder);
        });

        nextPageLoadingStateData = Transformations.switchMap(nextPageLoadingStateObj, obj -> {
            if (obj == null) {
                return AbsentLiveData.create();
            }
            Utils.psLog("nextPageLoadingStateData");
            return repository.getNextPageSubCategory(obj.loginUserId, obj.catId, obj.limit, obj.offset, obj.parameterHolder);
        });

        subCategoryListByCatIdData = Transformations.switchMap(subCategoryListByCatIdObj, obj -> {
            if (obj == null) {
                return AbsentLiveData.create();
            }

            Utils.psLog("ItemCategoryViewModel : categories");
            return repository.getSubCategoriesWithCatId(obj.loginUserId, obj.catId, obj.offset, obj.limit, obj.parameterHolder);
        });

        nextPageSubCategoryListByCatIdData = Transformations.switchMap(nextPageSubCategoryListByCatIdObj, obj -> {
            if (obj == null) {
                return AbsentLiveData.create();
            }

            Utils.psLog("Category List.");
            return repository.getNextPageSubCategoriesWithCatId(obj.limit, obj.offset, obj.catId, obj.loginUserId, obj.parameterHolder);
        });
    }

    //list by cat id
    public void setSubCategoryListByCatIdObj(String loginUserId, String catId, String limit, String offset, SubCategoryParameterHolder parameterHolder){

        ListByCatIdTmpDataHolder tmpDataHolder = new ListByCatIdTmpDataHolder(loginUserId, catId, limit, offset, parameterHolder);

        subCategoryListByCatIdObj.setValue(tmpDataHolder);
    }

    public LiveData<Resource<List<ItemSubCategory>>> getSubCategoryListByCatIdData()
    {
        return subCategoryListByCatIdData;
    }

    public void setNextPageSubCategoryListByCatIdObj( String loginUserId, String catId, String limit, String offset, SubCategoryParameterHolder parameterHolder)
    {
        ListByCatIdTmpDataHolder tmpDataHolder = new ListByCatIdTmpDataHolder(catId, loginUserId, limit, offset, parameterHolder);

        nextPageSubCategoryListByCatIdObj.setValue(tmpDataHolder);
    }

    public LiveData<Resource<Boolean>> getNextPageSubCategoryListByCatIdData() {
        return nextPageSubCategoryListByCatIdData;
    }
    //endregion

    public void setAllSubCategoryListObj() {
        if (!isLoading) {
            TmpDataHolder tmpDataHolder = new TmpDataHolder();
            allSubCategoryListObj.setValue(tmpDataHolder);

            // start loading
            setLoadingState(true);
        }
    }

    public LiveData<Resource<List<ItemSubCategory>>> getAllSubCategoryListData() {
        return allSubCategoryListData;
    }


    public void setSubCategoryListData(String loginUserId, String catId, String limit, String offset, SubCategoryParameterHolder parameterHolder) {
        if (!isLoading) {
            TmpDataHolder tmpDataHolder = new TmpDataHolder();
            tmpDataHolder.loginUserId = loginUserId;
            tmpDataHolder.catId = catId;
            tmpDataHolder.limit = limit;
            tmpDataHolder.offset = offset;
            tmpDataHolder.parameterHolder = parameterHolder;
            subCategoryListObj.setValue(tmpDataHolder);

            // start loading
            setLoadingState(true);
        }
    }

    public LiveData<Resource<List<ItemSubCategory>>> getSubCategoryListData() {
        return subCategoryListData;
    }

    public void setNextPageLoadingStateObj(String loginUserId, String catId, String limit, String offset, SubCategoryParameterHolder parameterHolder) {

        if (!isLoading) {
            TmpDataHolder tmpDataHolder = new TmpDataHolder();
            tmpDataHolder.loginUserId = loginUserId;
            tmpDataHolder.catId = catId;
            tmpDataHolder.limit = limit;
            tmpDataHolder.offset = offset;
            tmpDataHolder.parameterHolder = parameterHolder;
            nextPageLoadingStateObj.setValue(tmpDataHolder);

            // start loading
            setLoadingState(true);
        }
    }

    public LiveData<Resource<Boolean>> getNextPageLoadingStateData() {
        return nextPageLoadingStateData;
    }


    class TmpDataHolder {
        public String loginUserId = "";
        public String catId = "";
        public String limit = "";
        public String offset = "";
        public SubCategoryParameterHolder parameterHolder;
        public Boolean isConnected = false;


    }

    class ListByCatIdTmpDataHolder {
        private String limit, offset, catId, loginUserId;
        private SubCategoryParameterHolder parameterHolder;

        public ListByCatIdTmpDataHolder(String loginUserId, String catId, String limit, String offset, SubCategoryParameterHolder parameterHolder) {
            this.loginUserId = loginUserId;
            this.catId = catId;
            this.limit = limit;
            this.offset = offset;
            this.parameterHolder = parameterHolder;
        }
    }

}
