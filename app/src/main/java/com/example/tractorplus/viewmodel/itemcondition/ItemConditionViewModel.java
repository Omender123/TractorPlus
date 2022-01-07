package com.example.tractorplus.viewmodel.itemcondition;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.tractorplus.repository.itemcondition.ItemConditionRepository;
import com.example.tractorplus.utils.AbsentLiveData;
import com.example.tractorplus.utils.Utils;
import com.example.tractorplus.viewmodel.common.PSViewModel;
import com.example.tractorplus.viewobject.ItemCondition;
import com.example.tractorplus.viewobject.common.Resource;
import com.example.tractorplus.viewobject.holder.CategoryParameterHolder;

import java.util.List;

import javax.inject.Inject;

public class ItemConditionViewModel extends PSViewModel {


    //region Variables

    private final LiveData<Resource<List<ItemCondition>>> itemTypeListData;
    private MutableLiveData<ItemConditionViewModel.TmpDataHolder> itemTypeListObj = new MutableLiveData<>();

    private final LiveData<Resource<Boolean>> nextPageLoadingStateData;
    private MutableLiveData<ItemConditionViewModel.TmpDataHolder> nextPageLoadingStateObj = new MutableLiveData<>();


    public CategoryParameterHolder categoryParameterHolder = new CategoryParameterHolder();

    //endregion

    //region Constructors

    @Inject
    ItemConditionViewModel(ItemConditionRepository repository) {

        Utils.psLog("ItemConditionViewModel");

        itemTypeListData = Transformations.switchMap(itemTypeListObj, obj -> {
            if (obj == null) {
                return AbsentLiveData.create();
            }

            Utils.psLog("ItemConditionViewModel : categories");
            return repository.getAllItemConditionList(obj.limit, obj.offset);
        });

        nextPageLoadingStateData = Transformations.switchMap(nextPageLoadingStateObj, obj -> {
            if (obj == null) {
                return AbsentLiveData.create();
            }

            Utils.psLog("Category List.");
            return repository.getNextPageItemConditionList(obj.limit, obj.offset);
        });


    }

    //endregion

    public void setItemConditionListObj(String limit, String offset) {
        if (!isLoading) {
            TmpDataHolder tmpDataHolder = new TmpDataHolder();
            tmpDataHolder.offset = offset;
            tmpDataHolder.limit = limit;
            itemTypeListObj.setValue(tmpDataHolder);

            // start loading
            setLoadingState(true);
        }
    }

    public LiveData<Resource<List<ItemCondition>>> getItemConditionListData() {
        return itemTypeListData;
    }

    public void setNextPageLoadingStateObj(String limit, String offset) {

        if (!isLoading) {
            ItemConditionViewModel.TmpDataHolder tmpDataHolder = new ItemConditionViewModel.TmpDataHolder();
            tmpDataHolder.offset = offset;
            tmpDataHolder.limit = limit;
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
        public String cityId = "";
    }
}
