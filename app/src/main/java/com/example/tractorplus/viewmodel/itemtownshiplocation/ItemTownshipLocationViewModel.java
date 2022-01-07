package com.example.tractorplus.viewmodel.itemtownshiplocation;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.tractorplus.repository.itemtownshiplocation.ItemTownshipLocationRepository;
import com.example.tractorplus.utils.AbsentLiveData;
import com.example.tractorplus.utils.Constants;
import com.example.tractorplus.utils.Utils;
import com.example.tractorplus.viewmodel.common.PSViewModel;
import com.example.tractorplus.viewobject.ItemTownshipLocation;
import com.example.tractorplus.viewobject.common.Resource;
import com.example.tractorplus.viewobject.holder.CategoryParameterHolder;

import java.util.List;

import javax.inject.Inject;

public class ItemTownshipLocationViewModel extends PSViewModel {


    //region Variables

    private final LiveData<Resource<List<ItemTownshipLocation>>> itemTypeListData;
    private MutableLiveData<ItemTownshipLocationViewModel.TmpDataHolder> itemTypeListObj = new MutableLiveData<>();

    private final LiveData<Resource<Boolean>> nextPageLoadingStateData;
    private MutableLiveData<ItemTownshipLocationViewModel.TmpDataHolder> nextPageLoadingStateObj = new MutableLiveData<>();

    public CategoryParameterHolder categoryParameterHolder = new CategoryParameterHolder();

    public String keyword, orderType = Constants.SEARCH_CITY_DESC, orderBy = Constants.SEARCH_CITY_DEFAULT_ORDERING;

    public String cityId = "";
    public String cityName = "";
    public String townshipId = "";
    public String townshipName = "";
    public String lat = "";
    public String lng = "";


    //endregion

    //region Constructors

    @Inject
    ItemTownshipLocationViewModel(ItemTownshipLocationRepository repository) {

        Utils.psLog("ItemLocationViewModel");

        itemTypeListData = Transformations.switchMap(itemTypeListObj, obj -> {
            if (obj == null) {
                return AbsentLiveData.create();
            }

            Utils.psLog("ItemLocationViewModel : categories");
            return repository.getItemTownshipLocationList(obj.limit, obj.offset,obj.loginUserId,obj.keyword,obj.orderBy,obj.orderType,obj.cityId);
        });

        nextPageLoadingStateData = Transformations.switchMap(nextPageLoadingStateObj, obj -> {
            if (obj == null) {
                return AbsentLiveData.create();
            }

            Utils.psLog("Category List.");
            return repository.getNextPageTownshipLocationList(obj.limit, obj.offset,obj.loginUserId,obj.keyword,obj.orderBy,obj.orderType,obj.cityId);
        });

    }

    //endregion

    public void setItemLocationListObj(String limit, String offset,String loginUserId,String keyword,String orderBy,String orderType,String cityId) {
        if (!isLoading) {
            TmpDataHolder tmpDataHolder = new TmpDataHolder();
            tmpDataHolder.offset = offset;
            tmpDataHolder.limit = limit;
            tmpDataHolder.loginUserId = loginUserId;
            tmpDataHolder.keyword = keyword;
            tmpDataHolder.orderBy = orderBy;
            tmpDataHolder.orderType = orderType;
            tmpDataHolder.cityId = cityId;
            itemTypeListObj.setValue(tmpDataHolder);

            // start loading
            setLoadingState(true);
        }
    }


    public LiveData<Resource<List<ItemTownshipLocation>>> getItemLocationListData() {
        return itemTypeListData;
    }



    public void setNextPageLoadingStateObj(String limit, String offset, String cityId) {

        if (!isLoading) {
            ItemTownshipLocationViewModel.TmpDataHolder tmpDataHolder = new ItemTownshipLocationViewModel.TmpDataHolder();
            tmpDataHolder.offset = offset;
            tmpDataHolder.limit = limit;
            tmpDataHolder.cityId = cityId;
            nextPageLoadingStateObj.setValue(tmpDataHolder);

            // start loading
            setLoadingState(true);
        }
    }

    public LiveData<Resource<Boolean>> getNextPageLoadingStateData() {
        return nextPageLoadingStateData;
    }

    class TmpDataHolder {
        public String limit = "";
        public String offset = "";
        public String loginUserId="";
        public String keyword="";
        public String orderBy="";
        public String orderType="";
        public String cityId = "";
    }
}
