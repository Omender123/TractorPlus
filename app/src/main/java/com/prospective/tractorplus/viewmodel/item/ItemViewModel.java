package com.prospective.tractorplus.viewmodel.item;

import android.content.ContentResolver;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.prospective.tractorplus.Config;
import com.prospective.tractorplus.repository.item.ItemRepository;
import com.prospective.tractorplus.utils.AbsentLiveData;
import com.prospective.tractorplus.utils.Constants;
import com.prospective.tractorplus.utils.Utils;
import com.prospective.tractorplus.viewmodel.common.PSViewModel;
import com.prospective.tractorplus.viewobject.Image;
import com.prospective.tractorplus.viewobject.Item;
import com.prospective.tractorplus.viewobject.common.Resource;
import com.prospective.tractorplus.viewobject.holder.ItemParameterHolder;

import java.util.List;

import javax.inject.Inject;

public class ItemViewModel extends PSViewModel {

    private final LiveData<Resource<List<Item>>> itemListByKeyData;
    private final MutableLiveData<ItemViewModel.ItemTmpDataHolder> itemListByKeyObj = new MutableLiveData<>();

    private final LiveData<List<Item>> itemListFromDbByKeyData;
    private final MutableLiveData<ItemViewModel.ItemTmpDataHolder> itemListFromDbByKeyObj = new MutableLiveData<>();

    private final LiveData<Resource<Item>> productDetailListData;
    private MutableLiveData<ItemViewModel.TmpDataHolder> productDetailObj = new MutableLiveData<>();

    private final LiveData<Resource<Item>> markAsSoldOutItemData;
    private MutableLiveData<ItemViewModel.TmpDataHolder> markAsSoldOutItemObj = new MutableLiveData<>();

    private final LiveData<Item> productDetailFromDBByIdData;
    private MutableLiveData<ItemViewModel.TmpDataHolder> productDetailFromDBByIdObj = new MutableLiveData<>();

    private final LiveData<Resource<Boolean>> nextPageItemListByKeyData;
    private final MutableLiveData<ItemViewModel.ItemTmpDataHolder> nextPageItemListByKeyObj = new MutableLiveData<>();

    private LiveData<Resource<Image>> uploadItemImageData;
    private MutableLiveData<UploadItemImageTmpDataHolder> uploadItemImageObj = new MutableLiveData<>();

    private LiveData<Resource<Item>> uploadItemData;
    private MutableLiveData<UploadItemTmpDataHolder> uploadItemObj = new MutableLiveData<>();

    private final LiveData<Resource<Boolean>> reportItemStatusData;
    private final MutableLiveData<ItemViewModel.ReportItemTmpDataHolder> reportItemStatusObj = new MutableLiveData<>();

    private final LiveData<Resource<Boolean>> blockUserStatusData;
    private final MutableLiveData<ItemViewModel.BlockUserTmpDataHolder> blockUserStatusObj = new MutableLiveData<>();

    private final LiveData<Resource<Boolean>> deleteItemData;
    private final MutableLiveData<ItemViewModel.DeleteItemTmpDataHolder> deleteItemObj = new MutableLiveData<>();

    public ItemParameterHolder holder = new ItemParameterHolder();
    public String latValue = "", lngValue = "";
    public Item itemContainer;

    public String itemDescription = "";
    public String itemId = "";
    public String addedUserId = "";
    public String cityId = "";
    public String townshipId = "";
    public String historyFlag = "";
    public String customImageUrl;
    public String locationId = "";
    public String locationName = "";
    public String locationTownshipId = "";
    public String locationTownshipName = "";
    public String otherUserId = "";
    public String otherUserName = "";
    public String itemImage = "";
    public String dynamiclink = "";
    public String is_sold_out = "";
    public String mapLat = "";
    public String mapLng = "";
    public String followerCount = "";


    public Item currentItem;

    public String firstImagePath = Constants.EMPTY_STRING, secImagePath = Constants.EMPTY_STRING,
            thirdImagePath = Constants.EMPTY_STRING, fourthImagePath = Constants.EMPTY_STRING, fifthImagePath = Constants.EMPTY_STRING;

    public Uri firstImageUri = null, secImageUri = null, thirdImageUri = null, fourthImageUri = null, fifthImageUri = null;

    public String userId = "";


    @Inject
    ItemViewModel(ItemRepository repository) {

        itemListFromDbByKeyData = Transformations.switchMap(itemListFromDbByKeyObj, obj -> {

            if (obj == null) {
                return AbsentLiveData.create();
            }

            return repository.getItemListFromDbByKey(obj.loginUserId, obj.limit, obj.offset, obj.itemParameterHolder);

        });

        itemListByKeyData = Transformations.switchMap(itemListByKeyObj, obj -> {

            if (obj == null) {
                return AbsentLiveData.create();
            }

            return repository.getItemListByKey(obj.loginUserId, obj.limit, obj.offset, obj.itemParameterHolder);

        });

        nextPageItemListByKeyData = Transformations.switchMap(nextPageItemListByKeyObj, obj -> {

            if (obj == null) {
                return AbsentLiveData.create();
            }

            return repository.getNextPageProductListByKey(obj.itemParameterHolder, obj.loginUserId, obj.limit, obj.offset);

        });

        reportItemStatusData = Transformations.switchMap(reportItemStatusObj, obj -> {

            if (obj == null) {
                return AbsentLiveData.create();
            }

            return repository.reportItem(obj.itemId, obj.reportUserId);

        });

        blockUserStatusData = Transformations.switchMap(blockUserStatusObj, obj -> {

            if (obj == null) {
                return AbsentLiveData.create();
            }

            return repository.blockUser(obj.loginUserId, obj.addedUserId);

        });

        deleteItemData = Transformations.switchMap(deleteItemObj, obj -> {

            if (obj == null) {
                return AbsentLiveData.create();
            }

            return repository.deletePostItem(obj.itemId, obj.loginUserId);

        });

        //  item detail List
        productDetailListData = Transformations.switchMap(productDetailObj, obj -> {
            if (obj == null) {
                return AbsentLiveData.create();
            }
            Utils.psLog("product detail List.");
            return repository.getItemDetail(Config.API_KEY, obj.itemId, obj.historyFlag, obj.userId);
        });

        //  item mark as sold out
        markAsSoldOutItemData = Transformations.switchMap(markAsSoldOutItemObj, obj -> {
            if (obj == null) {
                return AbsentLiveData.create();
            }
            Utils.psLog("product detail List.");
            return repository.markSoldOutItem(Config.API_KEY, obj.itemId, obj.userId);
        });

        productDetailFromDBByIdData = Transformations.switchMap(productDetailFromDBByIdObj, obj -> {
            if (obj == null) {
                return AbsentLiveData.create();
            }
            Utils.psLog("product detail List.");
            return repository.getItemDetailFromDBById(obj.itemId);
        });

        uploadItemImageData = Transformations.switchMap(uploadItemImageObj, obj -> {
            if (obj == null) {
                return AbsentLiveData.create();
            }

            return repository.uploadItemImage(obj.filePath, obj.uri, obj.imageId, obj.itemId, obj.ordering, obj.contentResolver);

        });

        uploadItemData = Transformations.switchMap(uploadItemObj, obj -> {
            if (obj == null) {
                return AbsentLiveData.create();
            }

            return repository.uploadItem(obj.catId, obj.subCatId, obj.itemTypeId, obj.itemCurrencyId, obj.locationId, obj.locationTownshipId,
                    obj.description, obj.price, obj.brand, obj.businessMode, obj.isSoldOut, obj.title, obj.address, obj.itemId, obj.userId,obj.DateOfRegistration,
                    obj.Date_of_manufacturing,obj.HP,obj.Gear_system,obj.Hours_used,obj.Hands_changed_drop_down);


        });
    }


    //item image upload

    public void setUploadItemImageObj(String filePath, Uri uri, String itemId, String imageId, String ordering, ContentResolver contentResolver) {
        UploadItemImageTmpDataHolder tmpDataHolder = new UploadItemImageTmpDataHolder(filePath, uri, itemId, imageId, ordering, contentResolver);

        this.uploadItemImageObj.setValue(tmpDataHolder);
    }

    public LiveData<Resource<Image>> getUploadItemImageData() {
        return uploadItemImageData;
    }

    //region getItemList

    //item upload

    public void setUploadItemObj(String catId, String subCatId, String itemTypeId, String itemCurrencyId, String locationId, String locationTownshipId,
                                 String description, String price, String brand, String businessMode,
                                 String isSoldOut, String title, String address, String itemId, String userId,String DateOfRegistration, String Date_of_manufacturing, String HP, String Gear_system, String Hours_used, String Hands_changed_drop_down) {
        UploadItemTmpDataHolder tmpDataHolder = new UploadItemTmpDataHolder(catId, subCatId, itemTypeId, itemCurrencyId, locationId, locationTownshipId,
                description, price, brand, businessMode,
                isSoldOut, title, address, itemId, userId,DateOfRegistration,Date_of_manufacturing,HP,Gear_system,Hours_used,Hands_changed_drop_down);

        this.uploadItemObj.setValue(tmpDataHolder);
    }

    public LiveData<Resource<Item>> getUploadItemData() {
        return uploadItemData;
    }

    //endregion

    public void setItemListFromDbByKeyObj(String loginUserId, String limit, String offset, ItemParameterHolder parameterHolder) {

        ItemTmpDataHolder tmpDataHolder = new ItemTmpDataHolder(limit, offset, loginUserId, parameterHolder);

        this.itemListFromDbByKeyObj.setValue(tmpDataHolder);
        setLoadingState(true);


    }

    public void setItemListByKeyObj(String loginUserId, String limit, String offset, ItemParameterHolder parameterHolder) {
        if (!isLoading) {
            ItemTmpDataHolder tmpDataHolder = new ItemTmpDataHolder(limit, offset, loginUserId, parameterHolder);

            this.itemListByKeyObj.setValue(tmpDataHolder);
            setLoadingState(true);

        }
    }

    public LiveData<List<Item>> getItemListFromDbByKeyData() {
        return itemListFromDbByKeyData;
    }

    public LiveData<Resource<List<Item>>> getItemListByKeyData() {
        return itemListByKeyData;
    }

    public void setNextPageItemListByKeyObj(String limit, String offset, String loginUserId, ItemParameterHolder parameterHolder) {

        if (!isLoading) {
            ItemTmpDataHolder tmpDataHolder = new ItemTmpDataHolder(limit, offset, loginUserId, parameterHolder);

            setLoadingState(true);

            this.nextPageItemListByKeyObj.setValue(tmpDataHolder);
        }
    }

    public LiveData<Resource<Boolean>> getNextPageItemListByKeyData() {
        return nextPageItemListByKeyData;
    }

    //report item

    public void setReportItemStatusObj(String itemId, String reportUserId) {

        ReportItemTmpDataHolder tmpDataHolder = new ReportItemTmpDataHolder(itemId, reportUserId);

        this.reportItemStatusObj.setValue(tmpDataHolder);
    }

    public LiveData<Resource<Boolean>> getReportItemStatusData() {
        return reportItemStatusData;
    }

    //block user

    public void setBlockUserStatusObj(String loginUserId, String addedUserId) {

        BlockUserTmpDataHolder blockUsertmpDataHolder = new BlockUserTmpDataHolder(loginUserId, addedUserId);

        this.blockUserStatusObj.setValue(blockUsertmpDataHolder);
    }

    public LiveData<Resource<Boolean>> getBlockUserStatusData() {
        return blockUserStatusData;
    }

    //endregion

    //delete item

    public void setDeleteItemObj(String itemId, String loginUserId) {

        DeleteItemTmpDataHolder deleteItemTmpDataHolder = new DeleteItemTmpDataHolder(itemId, loginUserId);

        this.deleteItemObj.setValue(deleteItemTmpDataHolder);

    }

    public LiveData<Resource<Boolean>> getDeleteItemStatus() {
        return deleteItemData;
    }

    //endregion

    class ItemTmpDataHolder {

        private String limit, offset, loginUserId;
        private ItemParameterHolder itemParameterHolder;

        public ItemTmpDataHolder(String limit, String offset, String loginUserId, ItemParameterHolder itemParameterHolder) {
            this.limit = limit;
            this.offset = offset;
            this.loginUserId = loginUserId;
            this.itemParameterHolder = itemParameterHolder;
        }
    }

    class ReportItemTmpDataHolder {

        private String itemId, reportUserId;

        public ReportItemTmpDataHolder(String itemId, String reportUserId) {
            this.itemId = itemId;
            this.reportUserId = reportUserId;
        }
    }

    class BlockUserTmpDataHolder {

        private String loginUserId, addedUserId;

        public BlockUserTmpDataHolder(String loginUserId, String addedUserId) {
            this.loginUserId = loginUserId;
            this.addedUserId = addedUserId;
        }
    }

    class DeleteItemTmpDataHolder {

        public String itemId;
        public String loginUserId;

        private DeleteItemTmpDataHolder(String itemId, String loginUserId) {
            this.itemId = itemId;
            this.loginUserId = loginUserId;

        }
    }

    //region Getter And Setter for item detail List

    public void setItemDetailObj(String itemId, String historyFlag, String userId) {
        if (!isLoading) {
            ItemViewModel.TmpDataHolder tmpDataHolder = new ItemViewModel.TmpDataHolder();
            tmpDataHolder.itemId = itemId;
            tmpDataHolder.historyFlag = historyFlag;
            tmpDataHolder.userId = userId;
            productDetailObj.setValue(tmpDataHolder);

            // start loading
            setLoadingState(true);
        }
    }

    public LiveData<Resource<Item>> getItemDetailData() {
        return productDetailListData;
    }

    //endregion

    //region Getter And Setter for item mark as sold out

    public void setMarkAsSoldOutItemObj(String itemId, String userId) {
        if (!isLoading) {
            ItemViewModel.TmpDataHolder tmpDataHolder = new ItemViewModel.TmpDataHolder();
            tmpDataHolder.itemId = itemId;
            tmpDataHolder.userId = userId;
            markAsSoldOutItemObj.setValue(tmpDataHolder);

            // start loading
            setLoadingState(true);
        }
    }

    public LiveData<Resource<Item>> getMarkAsSoldOutItemData() {
        return markAsSoldOutItemData;
    }

    //endregion

    //region Getter And Setter for item detail List

    public void setItemDetailFromDBById(String itemId) {
        if (!isLoading) {
            ItemViewModel.TmpDataHolder tmpDataHolder = new ItemViewModel.TmpDataHolder();
            tmpDataHolder.itemId = itemId;
            productDetailFromDBByIdObj.setValue(tmpDataHolder);

            // start loading
            setLoadingState(true);
        }
    }

    public LiveData<Item> getItemDetailFromDBByIdData() {
        return productDetailFromDBByIdData;
    }

    //endregion

    //region Holder
    class TmpDataHolder {
        public String offset = "";
        public String itemId = "";
        public String historyFlag = "";
        public String userId = "";
        public String cityId = "";
        public Boolean isConnected = false;
    }
    //endregion

    class UploadItemImageTmpDataHolder {

        Uri uri;
        String filePath, itemId, imageId, ordering;
        ContentResolver contentResolver;

        private UploadItemImageTmpDataHolder(String filePath, Uri uri, String itemId, String imageId, String ordering, ContentResolver contentResolver) {
            this.filePath = filePath;
            this.uri = uri;
            this.itemId = itemId;
            this.imageId = imageId;
            this.ordering = ordering;
            this.contentResolver = contentResolver;
        }
    }

    class UploadItemTmpDataHolder {

        String catId, subCatId, itemTypeId, itemCurrencyId, locationId, locationTownshipId, description, price, brand, businessMode,
                isSoldOut, title, address, itemId, userId, DateOfRegistration, Date_of_manufacturing, HP, Gear_system, Hours_used, Hands_changed_drop_down;

        public UploadItemTmpDataHolder(String catId, String subCatId, String itemTypeId, String itemCurrencyId, String locationId, String locationTownshipId, String description, String price, String brand, String businessMode, String isSoldOut, String title, String address, String itemId, String userId, String DateOfRegistration, String Date_of_manufacturing, String HP, String Gear_system, String Hours_used, String Hands_changed_drop_down) {
            this.catId = catId;
            this.subCatId = subCatId;
            this.itemTypeId = itemTypeId;
            this.itemCurrencyId = itemCurrencyId;
            this.locationId = locationId;
            this.locationTownshipId = locationTownshipId;
            this.description = description;
            this.price = price;
            this.brand = brand;
            this.businessMode = businessMode;
            this.isSoldOut = isSoldOut;
            this.title = title;
            this.address = address;
            this.itemId = itemId;
            this.userId = userId;
            this.DateOfRegistration = DateOfRegistration;
            this.Date_of_manufacturing = Date_of_manufacturing;
            this.HP = HP;
            this.Gear_system = Gear_system;
            this.Hours_used = Hours_used;
            this.Hands_changed_drop_down = Hands_changed_drop_down;
        }
    }
}

