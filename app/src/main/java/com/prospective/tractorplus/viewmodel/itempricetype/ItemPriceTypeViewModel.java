package com.prospective.tractorplus.viewmodel.itempricetype;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.prospective.tractorplus.repository.itempricetype.ItemPriceTypeRepository;
import com.prospective.tractorplus.utils.AbsentLiveData;
import com.prospective.tractorplus.utils.Utils;
import com.prospective.tractorplus.viewmodel.common.PSViewModel;
import com.prospective.tractorplus.viewobject.ItemPriceType;
import com.prospective.tractorplus.viewobject.common.Resource;
import com.prospective.tractorplus.viewobject.holder.CategoryParameterHolder;
import com.prospective.tractorplus.viewobject.holder.ItemParameterHolder;

import java.util.List;

import javax.inject.Inject;

public class ItemPriceTypeViewModel extends PSViewModel {


    //region Variables

    private final LiveData<Resource<List<ItemPriceType>>> itemTypeListData;
    private MutableLiveData<ItemPriceTypeViewModel.TmpDataHolder> itemTypeListObj = new MutableLiveData<>();

    private final LiveData<Resource<Boolean>> nextPageLoadingStateData;
    private MutableLiveData<ItemPriceTypeViewModel.TmpDataHolder> nextPageLoadingStateObj = new MutableLiveData<>();

    public ItemParameterHolder productParameterHolder = new ItemParameterHolder();

    public CategoryParameterHolder categoryParameterHolder = new CategoryParameterHolder();

    //endregion

    //region Constructors

    @Inject
    ItemPriceTypeViewModel(ItemPriceTypeRepository repository) {

        Utils.psLog("ItemPriceTypeViewModel");

        itemTypeListData = Transformations.switchMap(itemTypeListObj, obj -> {
            if (obj == null) {
                return AbsentLiveData.create();
            }

            Utils.psLog("ItemPriceTypeViewModel : categories");
            return repository.getAllItemPriceTypeList(obj.limit, obj.offset);
        });

        nextPageLoadingStateData = Transformations.switchMap(nextPageLoadingStateObj, new Function<TmpDataHolder, LiveData<Resource<Boolean>>>() {
            @Override
            public LiveData<Resource<Boolean>> apply(TmpDataHolder obj) {
                if (obj == null) {
                    return AbsentLiveData.create();
                }

                Utils.psLog("Category List.");
                return repository.getNextPageItemPriceList(obj.limit, obj.offset);
            }
        });

    }

    //endregion

    public void setItemPriceTypeListObj(String limit, String offset) {
        if (!isLoading) {
            TmpDataHolder tmpDataHolder = new TmpDataHolder();
            tmpDataHolder.offset = offset;
            tmpDataHolder.limit = limit;
            itemTypeListObj.setValue(tmpDataHolder);

            // start loading
            setLoadingState(true);
        }
    }

    public LiveData<Resource<List<ItemPriceType>>> getItemPriceTypeListData() {
        return itemTypeListData;
    }

    //Get Latest Category Next Page
    public void setNextPageLoadingStateObj(String limit, String offset) {

        if (!isLoading) {
            TmpDataHolder tmpDataHolder = new TmpDataHolder();
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
