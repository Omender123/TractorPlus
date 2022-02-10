package com.prospective.tractorplus.ui.item.entry;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.prospective.tractorplus.Config;
import com.prospective.tractorplus.MainActivity;
import com.prospective.tractorplus.R;
import com.prospective.tractorplus.binding.FragmentDataBindingComponent;
import com.prospective.tractorplus.databinding.FragmentItemEntryBinding;
import com.prospective.tractorplus.databinding.ItemEntryBottomBoxBinding;
import com.prospective.tractorplus.ui.common.DataBoundListAdapter;
import com.prospective.tractorplus.ui.common.PSFragment;
import com.prospective.tractorplus.utils.AutoClearedValue;
import com.prospective.tractorplus.utils.Constants;
import com.prospective.tractorplus.utils.PSDialogMsg;
import com.prospective.tractorplus.utils.paths.RealPathUtil;
import com.prospective.tractorplus.utils.Utils;
import com.prospective.tractorplus.viewmodel.image.ImageViewModel;
import com.prospective.tractorplus.viewmodel.item.ItemViewModel;
import com.prospective.tractorplus.viewobject.Image;
import com.prospective.tractorplus.viewobject.Item;
import com.prospective.tractorplus.viewobject.common.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * A simple {@link Fragment} subclass.
 */
public class ItemEntryFragment extends PSFragment implements DataBoundListAdapter.DiffUtilDispatchedInterface {

    private final androidx.databinding.DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);
    private String catId = Constants.EMPTY_STRING;
    private String subCatId = Constants.EMPTY_STRING;
    private String typeId = Constants.EMPTY_STRING;
    private String priceTypeId = Constants.EMPTY_STRING;
    private String dealOptionId = Constants.EMPTY_STRING;
    private String conditionId = Constants.EMPTY_STRING;
    private String locationId = Constants.EMPTY_STRING;
    private String locationTownshipId = Constants.EMPTY_STRING;
    private String currencyId = Constants.EMPTY_STRING;
    private String businessMode = Constants.EMPTY_STRING;

    private String firstImageId = Constants.EMPTY_STRING;
    private String secImageId = Constants.EMPTY_STRING;
    private String thirdImageId = Constants.EMPTY_STRING;
    private String fourthImageId = Constants.EMPTY_STRING;
    private String fifthImageId = Constants.EMPTY_STRING;

    private boolean isFirstImageSelected = false;
    private boolean isSecImageSelected = false;
    private boolean isThirdImageSelected = false;
    private boolean isFourthImageSelected = false;
    private boolean isFifthImageSelected = false;

    private PSDialogMsg psDialogMsg;
    private ItemViewModel itemViewModel;
    private ImageViewModel imageViewModel;
    private String imagePath = "";
    private GoogleMap map;
    private Marker marker;
    private List<String> imagePathList = new ArrayList<>();
    private List<Uri> imageUriList = new ArrayList<>();
    private boolean selected = false;
    private int imageCount = 0;
    private ProgressDialog progressDialog;
    private boolean isUploadSuccess = false;

    private static final int REQUEST_LOCATION = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    LocationRequest locationRequest;


    @VisibleForTesting
    private AutoClearedValue<FragmentItemEntryBinding> binding;
    private AutoClearedValue<BottomSheetDialog> mBottomSheetDialog;
    private AutoClearedValue<ItemEntryBottomBoxBinding> bottomBoxLayoutBinding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentItemEntryBinding dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_item_entry, container, false, dataBindingComponent);

        binding = new AutoClearedValue<>(this, dataBinding);
        setHasOptionsMenu(true);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.getActivity());
        locationRequest = new LocationRequest().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(1000).setFastestInterval(1000).setNumUpdates(1);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location locationGPS : locationResult.getLocations()) {
                    try {
                        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(locationGPS.getLatitude(), locationGPS.getLongitude(), 1);
                        itemViewModel.latValue = String.valueOf(locationGPS.getLatitude());
                        itemViewModel.lngValue = String.valueOf(locationGPS.getLongitude());
                        changeCamera();
                        bindingLatLng(itemViewModel.latValue, itemViewModel.lngValue);
                        itemViewModel.mapLat = itemViewModel.latValue;
                        itemViewModel.mapLng = itemViewModel.lngValue;
                        bindMap(itemViewModel.latValue, itemViewModel.lngValue);
                        String address = addresses.get(0).getAddressLine(0);
                        binding.get().addressEditText.setText(address);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        initializeMap(savedInstanceState);

        return binding.get().getRoot();
    }

    private void initializeMap(Bundle savedInstanceState) {
        try {
            if (this.getActivity() != null) {
                MapsInitializer.initialize(this.getActivity());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        binding.get().mapView.onCreate(savedInstanceState);
        bindMap(selectedLat, selectedLng);

    }

    private void bindMap(String latValue, String lngValue) {
        binding.get().mapView.onResume();

        binding.get().mapView.getMapAsync(googleMap -> {
            map = googleMap;

            try {
                map.addMarker(new MarkerOptions()
                        .position(new LatLng(Double.valueOf(latValue), Double.valueOf(lngValue)))
                        .title("City Name"));

                //zoom
                if (!latValue.isEmpty() && !lngValue.isEmpty()) {
                    int zoomlevel = 15;
                    // Animating to the touched position
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(latValue), Double.parseDouble(lngValue)), zoomlevel));
                }
            } catch (Exception e) {
                Utils.psErrorLog("", e);
            }

        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_CODE__SEARCH_FRAGMENT && resultCode == Constants.RESULT_CODE__SEARCH_WITH_CATEGORY) {

            this.catId = data.getStringExtra(Constants.CATEGORY_ID);
            binding.get().categoryTextView.setText(data.getStringExtra(Constants.CATEGORY_NAME));
            itemViewModel.holder.cat_id = this.catId;
            this.subCatId = "";
            itemViewModel.holder.sub_cat_id = this.subCatId;
            binding.get().subCategoryTextView.setText("");

        } else if (requestCode == Constants.REQUEST_CODE__SEARCH_FRAGMENT && resultCode == Constants.RESULT_CODE__SEARCH_WITH_SUBCATEGORY) {

            this.subCatId = data.getStringExtra(Constants.SUBCATEGORY_ID);
            binding.get().subCategoryTextView.setText(data.getStringExtra(Constants.SUBCATEGORY_NAME));
            itemViewModel.holder.sub_cat_id = this.subCatId;
        } else if (requestCode == Constants.REQUEST_CODE__SEARCH_VIEW_FRAGMENT && resultCode == Constants.RESULT_CODE__SEARCH_WITH_ITEM_TYPE) {

            this.typeId = data.getStringExtra(Constants.ITEM_TYPE_ID);
            binding.get().typeTextView.setText(data.getStringExtra(Constants.ITEM_TYPE_NAME));
            itemViewModel.holder.type_id = this.typeId;
        } else if (requestCode == Constants.REQUEST_CODE__SEARCH_VIEW_FRAGMENT && resultCode == Constants.RESULT_CODE__SEARCH_WITH_ITEM_PRICE_TYPE) {

            this.priceTypeId = data.getStringExtra(Constants.ITEM_PRICE_TYPE_ID);
            //    binding.get().priceTypeTextView.setText(data.getStringExtra(Constants.ITEM_PRICE_TYPE_NAME));
            itemViewModel.holder.price_type_id = this.priceTypeId;
        } else if (requestCode == Constants.REQUEST_CODE__SEARCH_VIEW_FRAGMENT && resultCode == Constants.RESULT_CODE__SEARCH_WITH_ITEM_CURRENCY_TYPE) {

            defaultCurrencyId = data.getStringExtra(Constants.ITEM_CURRENCY_TYPE_ID);
            binding.get().priceTextView.setText(data.getStringExtra(Constants.ITEM_CURRENCY_TYPE_NAME));
            itemViewModel.holder.currency_id = defaultCurrencyId;
        } else if (requestCode == Constants.REQUEST_CODE__SEARCH_VIEW_FRAGMENT && resultCode == Constants.RESULT_CODE__SEARCH_WITH_ITEM_OPTION_TYPE) {

            this.dealOptionId = data.getStringExtra(Constants.ITEM_OPTION_TYPE_ID);
            //    binding.get().dealOptionTextView.setText(data.getStringExtra(Constants.ITEM_OPTION_TYPE_NAME));
            itemViewModel.holder.deal_option_id = this.dealOptionId;
        } else if (requestCode == Constants.REQUEST_CODE__SEARCH_VIEW_FRAGMENT && resultCode == Constants.RESULT_CODE__SEARCH_WITH_ITEM_CONDITION_TYPE) {

            this.conditionId = data.getStringExtra(Constants.ITEM_CONDITION_TYPE_ID);
            //    binding.get().itemConditionTextView.setText(data.getStringExtra(Constants.ITEM_CONDITION_TYPE_NAME));
            itemViewModel.holder.condition_id = this.conditionId;
        } else if (requestCode == Constants.REQUEST_CODE__SEARCH_VIEW_FRAGMENT && resultCode == Constants.RESULT_CODE__SEARCH_WITH_ITEM_LOCATION_TYPE) {

            this.locationId = data.getStringExtra(Constants.ITEM_LOCATION_TYPE_ID);
            //  itemViewModel.latValue = data.getStringExtra(Constants.LAT);
            //  itemViewModel.lngValue = data.getStringExtra(Constants.LNG);
            binding.get().locationTextView.setText(data.getStringExtra(Constants.ITEM_LOCATION_TYPE_NAME));
            itemViewModel.holder.location_id = this.locationId;

            this.locationTownshipId = "";
            binding.get().townshipTextView.setText("");

            itemViewModel.mapLat = itemViewModel.latValue;
            itemViewModel.mapLng = itemViewModel.lngValue;

            bindMap(itemViewModel.latValue, itemViewModel.lngValue);

            bindingLatLng(itemViewModel.latValue, itemViewModel.lngValue);

        } else if (requestCode == Constants.REQUEST_CODE__SEARCH_VIEW_FRAGMENT && resultCode == Constants.RESULT_CODE__SEARCH_WITH_ITEM_TOWNSHIP_TYPE) {

            this.locationTownshipId = data.getStringExtra(Constants.ITEM_TOWNSHIP_TYPE_ID);
            itemViewModel.latValue = data.getStringExtra(Constants.LAT);
            itemViewModel.lngValue = data.getStringExtra(Constants.LNG);
            binding.get().townshipTextView.setText(data.getStringExtra(Constants.ITEM_TOWNSHIP_TYPE_NAME));
            itemViewModel.holder.location_township_id = this.locationTownshipId;

            if (!itemViewModel.latValue.equals(Constants.INVALID_LAT_LNG) && !itemViewModel.lngValue.equals(Constants.INVALID_LAT_LNG)) {
                itemViewModel.mapLat = itemViewModel.latValue;
                itemViewModel.mapLng = itemViewModel.lngValue;

                bindMap(itemViewModel.latValue, itemViewModel.lngValue);

                bindingLatLng(itemViewModel.latValue, itemViewModel.lngValue);
            }
        } else if (requestCode == Constants.RESULT_CODE__TO_MAP_VIEW && resultCode == Constants.RESULT_CODE__FROM_MAP_VIEW) {

            itemViewModel.latValue = data.getStringExtra(Constants.LAT);
            itemViewModel.lngValue = data.getStringExtra(Constants.LNG);

            changeCamera();

            bindingLatLng(itemViewModel.latValue, itemViewModel.lngValue);
        }

        //image  gallery upload

        if ((requestCode == Constants.REQUEST_CODE__FIRST_GALLERY || requestCode == Constants.REQUEST_CODE__SEC_GALLERY || requestCode == Constants.REQUEST_CODE__THIRD_GALLERY ||
                requestCode == Constants.REQUEST_CODE__FOURTH_GALLERY || requestCode == Constants.REQUEST_CODE__FIFTH_GALLERY)
                && resultCode == Constants.RESULT_OK && null != data) {
            selected = true;
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            if (requestCode == Constants.REQUEST_CODE__FIRST_GALLERY) {

                if (RealPathUtil.getRealPath(getContext(), selectedImage).contains(".webp")) {
                    psDialogMsg.showErrorDialog(getString(R.string.error_message__webp_image), getString(R.string.app__ok));
                    psDialogMsg.show();
                } else {
                    dataBindingComponent.getFragmentBindingAdapters().bindFullImageUri(binding.get().firstImageView, selectedImage);
                    itemViewModel.firstImagePath = RealPathUtil.getRealPath(getContext(), selectedImage);//convertToImagePath(data, selectedImage, filePathColumn);
                    itemViewModel.firstImageUri = selectedImage;
                    isFirstImageSelected = true;
                }
            }
            if (requestCode == Constants.REQUEST_CODE__SEC_GALLERY) {
                if (RealPathUtil.getRealPath(getContext(), selectedImage).contains(".webp")) {
                    psDialogMsg.showErrorDialog(getString(R.string.error_message__webp_image), getString(R.string.app__ok));
                    psDialogMsg.show();
                } else {
                    dataBindingComponent.getFragmentBindingAdapters().bindFullImageUri(binding.get().secImageView, selectedImage);
                    itemViewModel.secImagePath = RealPathUtil.getRealPath(getContext(), selectedImage);
                    itemViewModel.secImageUri = selectedImage;
                    isSecImageSelected = true;
                }
            }
            if (requestCode == Constants.REQUEST_CODE__THIRD_GALLERY) {
                if (RealPathUtil.getRealPath(getContext(), selectedImage).contains(".webp")) {
                    psDialogMsg.showErrorDialog(getString(R.string.error_message__webp_image), getString(R.string.app__ok));
                    psDialogMsg.show();
                } else {
                    dataBindingComponent.getFragmentBindingAdapters().bindFullImageUri(binding.get().thirdImageView, selectedImage);
                    itemViewModel.thirdImagePath = RealPathUtil.getRealPath(getContext(), selectedImage);
                    itemViewModel.thirdImageUri = selectedImage;
                    isThirdImageSelected = true;
                }
            }
            if (requestCode == Constants.REQUEST_CODE__FOURTH_GALLERY) {
                if (RealPathUtil.getRealPath(getContext(), selectedImage).contains(".webp")) {
                    psDialogMsg.showErrorDialog(getString(R.string.error_message__webp_image), getString(R.string.app__ok));
                    psDialogMsg.show();
                } else {
                    dataBindingComponent.getFragmentBindingAdapters().bindFullImageUri(binding.get().fouthImageView, selectedImage);
                    itemViewModel.fourthImagePath = RealPathUtil.getRealPath(getContext(), selectedImage);
                    itemViewModel.fourthImageUri = selectedImage;
                    isFourthImageSelected = true;
                }
            }
            if (requestCode == Constants.REQUEST_CODE__FIFTH_GALLERY) {
                if (RealPathUtil.getRealPath(getContext(), selectedImage).contains(".webp")) {
                    psDialogMsg.showErrorDialog(getString(R.string.error_message__webp_image), getString(R.string.app__ok));
                    psDialogMsg.show();
                } else {
                    dataBindingComponent.getFragmentBindingAdapters().bindFullImageUri(binding.get().fifthImageView, selectedImage);
                    itemViewModel.fifthImagePath = RealPathUtil.getRealPath(getContext(), selectedImage);
                    itemViewModel.fifthImageUri = selectedImage;
                    isFifthImageSelected = true;
                }
            }


        }

        //image camera

        if ((requestCode == Constants.REQUEST_CODE__FIRST_CAMERA || requestCode == Constants.REQUEST_CODE__SEC_CAMERA || requestCode == Constants.REQUEST_CODE__THIRD_CAMERA ||
                requestCode == Constants.REQUEST_CODE__FOURTH_CAMERA || requestCode == Constants.REQUEST_CODE__FIFTH_CAMERA) && resultCode == Constants.RESULT_OK) {
            selected = true;

            if (requestCode == Constants.REQUEST_CODE__FIRST_CAMERA) {
                dataBindingComponent.getFragmentBindingAdapters().bindFullImageUri(binding.get().firstImageView, navigationController.photoURI);
                itemViewModel.firstImagePath = Utils.currentPhotoPath;
                itemViewModel.firstImageUri = navigationController.photoURI;
                isFirstImageSelected = true;
            }
            if (requestCode == Constants.REQUEST_CODE__SEC_CAMERA) {
                dataBindingComponent.getFragmentBindingAdapters().bindFullImageUri(binding.get().secImageView, navigationController.photoURI);
                itemViewModel.secImagePath = Utils.currentPhotoPath;//photoURI.getPath();
                itemViewModel.secImageUri = navigationController.photoURI;
                isSecImageSelected = true;
            }
            if (requestCode == Constants.REQUEST_CODE__THIRD_CAMERA) {
                dataBindingComponent.getFragmentBindingAdapters().bindFullImageUri(binding.get().thirdImageView, navigationController.photoURI);
                itemViewModel.thirdImagePath = Utils.currentPhotoPath;
                itemViewModel.thirdImageUri = navigationController.photoURI;
                isThirdImageSelected = true;
            }
            if (requestCode == Constants.REQUEST_CODE__FOURTH_CAMERA) {
                dataBindingComponent.getFragmentBindingAdapters().bindFullImageUri(binding.get().fouthImageView, navigationController.photoURI);
                itemViewModel.fourthImagePath = Utils.currentPhotoPath;
                itemViewModel.fourthImageUri = navigationController.photoURI;
                isFourthImageSelected = true;
            }
            if (requestCode == Constants.REQUEST_CODE__FIFTH_CAMERA) {
                dataBindingComponent.getFragmentBindingAdapters().bindFullImageUri(binding.get().fifthImageView, navigationController.photoURI);
                itemViewModel.fifthImagePath = Utils.currentPhotoPath;
                itemViewModel.fifthImageUri = navigationController.photoURI;
                isFifthImageSelected = true;
            }
        }

        //custom camera

        if ((requestCode == Constants.REQUEST_CODE__FIRST_CUSTOM_CAMERA || requestCode == Constants.REQUEST_CODE__SEC_CUSTOM_CAMERA || requestCode == Constants.REQUEST_CODE__THIRD_CUSTOM_CAMERA ||
                requestCode == Constants.REQUEST_CODE__FOURTH_CUSTOM_CAMERA || requestCode == Constants.REQUEST_CODE__FIFTH_CUSTOM_CAMERA) && resultCode == Constants.RESULT_CODE__ITEM_ENTRY_WITH_CUSTOM_CAMERA) {

            itemViewModel.customImageUrl = data.getStringExtra(Constants.IMAGE_PATH);

            selected = true;
            Uri selectedImage = Uri.parse(data.getStringExtra(Constants.IMAGE_URI));//data.getData();
            if (requestCode == Constants.REQUEST_CODE__FIRST_CUSTOM_CAMERA) {
                dataBindingComponent.getFragmentBindingAdapters().bindFullImageUri(binding.get().firstImageView, selectedImage);
                itemViewModel.firstImagePath = itemViewModel.customImageUrl;
                itemViewModel.firstImageUri = selectedImage;
                isFirstImageSelected = true;
            }
            if (requestCode == Constants.REQUEST_CODE__SEC_CUSTOM_CAMERA) {
                dataBindingComponent.getFragmentBindingAdapters().bindFullImageUri(binding.get().secImageView, selectedImage);
                itemViewModel.secImagePath = itemViewModel.customImageUrl;
                itemViewModel.secImageUri = selectedImage;
                isSecImageSelected = true;
            }
            if (requestCode == Constants.REQUEST_CODE__THIRD_CUSTOM_CAMERA) {
                dataBindingComponent.getFragmentBindingAdapters().bindFullImageUri(binding.get().thirdImageView, selectedImage);
                itemViewModel.thirdImagePath = itemViewModel.customImageUrl;
                itemViewModel.thirdImageUri = selectedImage;
                isThirdImageSelected = true;
            }
            if (requestCode == Constants.REQUEST_CODE__FOURTH_CUSTOM_CAMERA) {
                dataBindingComponent.getFragmentBindingAdapters().bindFullImageUri(binding.get().fouthImageView, selectedImage);
                itemViewModel.fourthImagePath = itemViewModel.customImageUrl;
                itemViewModel.fourthImageUri = selectedImage;
                isFourthImageSelected = true;
            }
            if (requestCode == Constants.REQUEST_CODE__FIFTH_CUSTOM_CAMERA) {
                dataBindingComponent.getFragmentBindingAdapters().bindFullImageUri(binding.get().fifthImageView, selectedImage);
                itemViewModel.fifthImagePath = itemViewModel.customImageUrl;
                itemViewModel.fifthImageUri = selectedImage;
                isFifthImageSelected = true;
            }
        }
        //endregion
    }

//    private String convertToImagePath(Intent data, Uri selectedImage, String[] filePathColumn) {
//
//        if (getActivity() != null && selectedImage != null) {
////            Cursor cursor = getActivity().getContentResolver().query(selectedImage,
////                    filePathColumn, null, null, null);
//            Cursor cursor = getActivity().getContentResolver().query(selectedImage,
//                    null, null, null, null);
//
//            selected = true;
//            if (cursor != null) {
//                cursor.moveToFirst();
//
////                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
////                imagePath = cursor.getString(columnIndex);
//                String documentId = cursor.getString(0);
//                documentId =documentId.substring(documentId.lastIndexOf(":") + 1);
//                Utils.psLog(documentId);
//
//                cursor.close();
//
//
//                cursor = getActivity().getContentResolver().query(
//                        MediaStore.Images.Media.INTERNAL_CONTENT_URI,
//                        null,
//                        MediaStore.Images.Media._ID + " = ? ",
//                        new String[] {documentId}, null);
//                cursor.moveToFirst();
//                imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
//                cursor.close();
//            }
//
//            final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//            getActivity().getContentResolver().takePersistableUriPermission(selectedImage, takeFlags);
//
//            Bitmap bmp = null;
//            try {
//                InputStream stream = getActivity().getContentResolver().openInputStream(selectedImage);
//                bmp = BitmapFactory.decodeStream(stream);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            int i = 0;
//            int j = i+2323;
//        }
//        return imagePath;
//    }

    @Override
    public void onDispatched() {


    }

    @Override
    protected void initUIAndActions() {

        itemViewModel.latValue = selectedLat;
        itemViewModel.lngValue = selectedLng;

        if (getActivity() instanceof MainActivity) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            ((MainActivity) this.getActivity()).binding.toolbar.setBackgroundColor(getResources().getColor(R.color.global__primary));
            ((MainActivity) getActivity()).updateMenuIconWhite();
        }

        if (getContext() != null) {

            BottomSheetDialog mBottomSheetDialog2 = new BottomSheetDialog(getContext());
            mBottomSheetDialog = new AutoClearedValue<>(this, mBottomSheetDialog2);

            ItemEntryBottomBoxBinding bottomBoxLayoutBinding2 = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.item_entry_bottom_box, null, false);
            bottomBoxLayoutBinding = new AutoClearedValue<>(this, bottomBoxLayoutBinding2);
            mBottomSheetDialog.get().setContentView(bottomBoxLayoutBinding.get().getRoot());

        }

        psDialogMsg = new PSDialogMsg(getActivity(), false);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.message__loading));
        progressDialog.setCancelable(false);

        if (Config.SHOW_ADMOB && connectivity.isConnected()) {
            AdRequest adRequest = new AdRequest.Builder()
                    .build();
            binding.get().adView.loadAd(adRequest);
            AdRequest adRequest2 = new AdRequest.Builder()
                    .build();
            binding.get().adView2.loadAd(adRequest2);

        } else {
            binding.get().adView.setVisibility(View.GONE);
            binding.get().adView2.setVisibility(View.GONE);
        }

        if (Config.ENABLE_SUB_LOCATION) {
            binding.get().townshipTitleTextView.setVisibility(View.VISIBLE);
            binding.get().townshipCardView.setVisibility(View.VISIBLE);
            binding.get().starTextView12.setVisibility(View.VISIBLE);
        } else {
            binding.get().townshipTitleTextView.setVisibility(View.GONE);
            binding.get().townshipCardView.setVisibility(View.GONE);
            binding.get().starTextView12.setVisibility(View.GONE);
        }


        binding.get().titleEditText.setHint(R.string.search__notSet);
        binding.get().categoryTextView.setHint(R.string.search__notSet);
        binding.get().subCategoryTextView.setHint(R.string.search__notSet);

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        AutoClearedValue<AlertDialog.Builder> alertDialog = new AutoClearedValue<>(this, builder);
        alertDialog.get().setTitle(getResources().getString(R.string.Feature_UI__search_alert_cat_title));

        binding.get().categoryTextView.setText("");
        binding.get().subCategoryTextView.setText("");

        binding.get().categorySelectionView.setOnClickListener(view -> navigationController.navigateToSearchActivityCategoryFragment(this.getActivity(), Constants.CATEGORY, catId, subCatId));

        binding.get().subCategorySelectionView.setOnClickListener(view -> {

            if (catId.equals(Constants.NO_DATA) || catId.isEmpty()) {

                psDialogMsg.showWarningDialog(getString(R.string.error_message__choose_category), getString(R.string.app__ok));

                psDialogMsg.show();

            } else {
                navigationController.navigateToSearchActivityCategoryFragment(this.getActivity(), Constants.SUBCATEGORY, catId, subCatId);
            }
        });

        binding.get().typeCardView.setOnClickListener(view -> navigationController.navigateToSearchViewActivity(this.getActivity(), Constants.ITEM_TYPE, typeId, priceTypeId, conditionId, dealOptionId, currencyId, locationId));

        //   binding.get().itemConditionCardView.setOnClickListener(view -> navigationController.navigateToSearchViewActivity(this.getActivity(), Constants.ITEM_CONDITION_TYPE, typeId, priceTypeId, conditionId, dealOptionId, currencyId, locationId));

//        binding.get().priceTypeCardView.setOnClickListener(view -> navigationController.navigateToSearchViewActivity(this.getActivity(), Constants.ITEM_PRICE_TYPE, typeId, priceTypeId, conditionId, dealOptionId, currencyId, locationId));

        //      binding.get().dealOptionCardView.setOnClickListener(view -> navigationController.navigateToSearchViewActivity(this.getActivity(), Constants.ITEM_DEAL_OPTION_TYPE, typeId, priceTypeId, conditionId, dealOptionId, currencyId, locationId));

        binding.get().locationCardView.setOnClickListener(view -> navigationController.navigateToSearchViewActivity(this.getActivity(), Constants.ITEM_LOCATION_TYPE, typeId, priceTypeId, conditionId, dealOptionId, currencyId, locationId));

        binding.get().townshipCardView.setOnClickListener(view -> {

            if (locationId.equals(Constants.NO_DATA) || locationId.isEmpty()) {

                psDialogMsg.showWarningDialog(getString(R.string.error_message__choose_city), getString(R.string.app__ok));

                psDialogMsg.show();

            } else {
                navigationController.navigateToSearchViewActivity(this.getActivity(), Constants.ITEM_TOWNSHIP_TYPE, typeId, priceTypeId, conditionId, dealOptionId, currencyId, locationId);
            }
        });

        binding.get().priceCardView.setOnClickListener(view -> navigationController.navigateToSearchViewActivity(this.getActivity(), Constants.ITEM_CURRENCY_TYPE, typeId, priceTypeId, conditionId, dealOptionId, currencyId, locationId));

        binding.get().mapViewButton.setOnClickListener(v -> {

            map.clear();

            if (itemViewModel.itemId.equals(Constants.ADD_NEW_ITEM)) {
                navigationController.navigateToMapActivity(ItemEntryFragment.this.getActivity(), selectedLng, selectedLat, Constants.MAP_PICK);
            } else {
                navigationController.navigateToMapActivity(ItemEntryFragment.this.getActivity(), itemViewModel.mapLng, itemViewModel.mapLat, Constants.MAP_PICK);
            }


        });

        binding.get().submitButton.setOnClickListener(view -> {

            if ((itemViewModel.firstImagePath.equals("") && firstImageId.equals(""))
                    && (itemViewModel.secImagePath.equals("") && secImageId.equals(""))
                    && (itemViewModel.thirdImagePath.equals("") && thirdImageId.equals(""))
                    && (itemViewModel.fourthImagePath.equals("") && fourthImageId.equals(""))
                    && (itemViewModel.fifthImagePath.equals("") && fifthImageId.equals(""))
            ) {
                psDialogMsg.showWarningDialog(ItemEntryFragment.this.getString(R.string.item_entry_need_image), ItemEntryFragment.this.getString(R.string.app__ok));
                psDialogMsg.show();
            } else if (binding.get().titleEditText.getText().toString().isEmpty()) {
                psDialogMsg.showWarningDialog(ItemEntryFragment.this.getString(R.string.item_entry_need_list_title), ItemEntryFragment.this.getString(R.string.app__ok));
                psDialogMsg.show();
            } else if (binding.get().categoryTextView.getText().toString().isEmpty()) {
                psDialogMsg.showWarningDialog("Manufacturers", ItemEntryFragment.this.getString(R.string.app__ok));
                psDialogMsg.show();
            } else if (binding.get().subCategoryTextView.getText().toString().isEmpty()) {
                psDialogMsg.showWarningDialog("Model", ItemEntryFragment.this.getString(R.string.app__ok));
                psDialogMsg.show();
            } else if (binding.get().typeTextView.getText().toString().isEmpty()) {
                psDialogMsg.showWarningDialog(ItemEntryFragment.this.getString(R.string.item_entry_need_type), ItemEntryFragment.this.getString(R.string.app__ok));
                psDialogMsg.show();
            } /*else if (binding.get().itemConditionTextView.getText().toString().isEmpty()) {
                psDialogMsg.showWarningDialog(ItemEntryFragment.this.getString(R.string.item_entry_need_item_condition), ItemEntryFragment.this.getString(R.string.app__ok));
                psDialogMsg.show();
            } else if (binding.get().dealOptionTextView.getText().toString().isEmpty()) {
                psDialogMsg.showWarningDialog(ItemEntryFragment.this.getString(R.string.item_entry_need_deal_option), ItemEntryFragment.this.getString(R.string.app__ok));
                psDialogMsg.show();
            }*/ else if (binding.get().descEditText.getText().toString().isEmpty()) {
                psDialogMsg.showWarningDialog(ItemEntryFragment.this.getString(R.string.item_entry_need_description), ItemEntryFragment.this.getString(R.string.app__ok));
                psDialogMsg.show();
            } else if (binding.get().priceEditText.getText().toString().isEmpty()) {
                psDialogMsg.showWarningDialog(ItemEntryFragment.this.getString(R.string.item_entry_need_price), ItemEntryFragment.this.getString(R.string.app__ok));
                psDialogMsg.show();
            } else if (binding.get().priceTextView.getText().toString().isEmpty()) {
                psDialogMsg.showWarningDialog(ItemEntryFragment.this.getString(R.string.item_entry_need_currency_symbol), ItemEntryFragment.this.getString(R.string.app__ok));
                psDialogMsg.show();
            } else if (binding.get().locationTextView.getText().toString().isEmpty()) {
                psDialogMsg.showWarningDialog(ItemEntryFragment.this.getString(R.string.item_entry_location_select), ItemEntryFragment.this.getString(R.string.app__ok));
                psDialogMsg.show();
            } else if (Config.ENABLE_SUB_LOCATION && binding.get().townshipTextView.getText().toString().isEmpty()) {
                psDialogMsg.showWarningDialog(ItemEntryFragment.this.getString(R.string.location_township_select), ItemEntryFragment.this.getString(R.string.app__ok));
                psDialogMsg.show();
            } else if (binding.get().townshipTextView.getText().toString().equals(Constants.LOCATION_ALL)) {
                psDialogMsg.showWarningDialog(ItemEntryFragment.this.getString(R.string.item_entry_city_all_error), ItemEntryFragment.this.getString(R.string.app__ok));
                psDialogMsg.show();

            } else if (binding.get().ResgisterEditText.getText().toString().isEmpty() || binding.get().ManufacturingEditText.getText().toString().isEmpty()
                    || binding.get().HpEditText.getText().toString().isEmpty() || binding.get().GearSystemEditText.getText().toString().isEmpty()
                    || binding.get().HoursUsedEditText.getText().toString().isEmpty() || binding.get().HandsDropEditText.getText().toString().isEmpty()) {

                Toast.makeText(getApplicationContext(), "Please enter all mandatory Filed", Toast.LENGTH_SHORT).show();

            } else {

                isUploadSuccess = false;

                ItemEntryFragment.this.getImagePathList();

                ItemEntryFragment.this.checkIsShop();


                if (itemViewModel.itemId != null) {
                    if (!itemViewModel.itemId.equals(Constants.ADD_NEW_ITEM)) {//edit
                        if (selectedLat.equals(Constants.INVALID_LAT_LNG) || selectedLat.equals("") ||
                                selectedLng.equals(Constants.INVALID_LAT_LNG) || selectedLng.equals("")) {
                            itemViewModel.latValue = itemViewModel.mapLat;
                            itemViewModel.lngValue = itemViewModel.mapLng;
                        }
                        itemViewModel.setUploadItemObj(ItemEntryFragment.this.catId, ItemEntryFragment.this.subCatId, ItemEntryFragment.this.typeId, defaultCurrencyId, ItemEntryFragment.this.locationId, ItemEntryFragment.this.locationTownshipId,
                                binding.get().descEditText.getText().toString(), binding.get().priceEditText.getText().toString(),
                                binding.get().brandEditText.getText().toString(), businessMode, itemViewModel.is_sold_out, binding.get().titleEditText.getText().toString(), binding.get().addressEditText.getText().toString(),
                                itemViewModel.itemId, loginUserId, binding.get().ResgisterEditText.getText().toString(), binding.get().ManufacturingEditText.getText().toString(),
                                binding.get().HpEditText.getText().toString(), binding.get().GearSystemEditText.getText().toString(),
                                binding.get().HoursUsedEditText.getText().toString(), binding.get().HandsDropEditText.getText().toString());
                    } else {//add new item
                        itemViewModel.setUploadItemObj(ItemEntryFragment.this.catId, ItemEntryFragment.this.subCatId, ItemEntryFragment.this.typeId, defaultCurrencyId, ItemEntryFragment.this.locationId, ItemEntryFragment.this.locationTownshipId,
                                binding.get().descEditText.getText().toString(),
                                binding.get().priceEditText.getText().toString(),
                                binding.get().brandEditText.getText().toString(), businessMode, "", binding.get().titleEditText.getText().toString(), binding.get().addressEditText.getText().toString(),
                                "", loginUserId, binding.get().ResgisterEditText.getText().toString(), binding.get().ManufacturingEditText.getText().toString(),
                                binding.get().HpEditText.getText().toString(), binding.get().GearSystemEditText.getText().toString(),
                                binding.get().HoursUsedEditText.getText().toString(), binding.get().HandsDropEditText.getText().toString());
                    }

                }

                progressDialog.show();
            }

        });

        binding.get().firstImageView.setOnClickListener(v -> {
            mBottomSheetDialog.get().show();
            ButtonSheetClick(Constants.ONE);
        });

        binding.get().secImageView.setOnClickListener(v -> {
            mBottomSheetDialog.get().show();
            ButtonSheetClick(Constants.TWO);
        });

        binding.get().thirdImageView.setOnClickListener(v -> {
            mBottomSheetDialog.get().show();
            ButtonSheetClick(Constants.THREE);
        });

        binding.get().fouthImageView.setOnClickListener(v -> {
            mBottomSheetDialog.get().show();
            ButtonSheetClick(Constants.FOUR);
        });

        binding.get().fifthImageView.setOnClickListener(v -> {
            mBottomSheetDialog.get().show();
            ButtonSheetClick(Constants.FIVE);
        });

        binding.get().deleteSecondImageView.setOnClickListener(v -> {
            psDialogMsg.showConfirmDialog(ItemEntryFragment.this.getString(R.string.item_entry__confirm_delete_image), ItemEntryFragment.this.getString(R.string.app__ok), ItemEntryFragment.this.getString(R.string.message__cancel_close));
            psDialogMsg.show();

            psDialogMsg.okButton.setOnClickListener(v12 -> {
                imageViewModel.setDeleteImageObj(String.valueOf(Config.ITEM_DELETE_COUNT), String.valueOf(imageViewModel.offset), loginUserId, secImageId);

                psDialogMsg.cancel();

                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().secImageView, "");

            });

            psDialogMsg.cancelButton.setOnClickListener(v1 -> psDialogMsg.cancel());
        });

        binding.get().deleteThirdImageView.setOnClickListener(v -> {
            psDialogMsg.showConfirmDialog(ItemEntryFragment.this.getString(R.string.item_entry__confirm_delete_image), ItemEntryFragment.this.getString(R.string.app__ok), ItemEntryFragment.this.getString(R.string.message__cancel_close));
            psDialogMsg.show();

            psDialogMsg.okButton.setOnClickListener(v12 -> {
                imageViewModel.setDeleteImageObj(String.valueOf(Config.ITEM_DELETE_COUNT), String.valueOf(imageViewModel.offset), loginUserId, thirdImageId);

                psDialogMsg.cancel();

                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().thirdImageView, "");

            });

            psDialogMsg.cancelButton.setOnClickListener(v1 -> psDialogMsg.cancel());
        });

        binding.get().deleteFouthImageView.setOnClickListener(v -> {
            psDialogMsg.showConfirmDialog(ItemEntryFragment.this.getString(R.string.item_entry__confirm_delete_image), ItemEntryFragment.this.getString(R.string.app__ok), ItemEntryFragment.this.getString(R.string.message__cancel_close));
            psDialogMsg.show();

            psDialogMsg.okButton.setOnClickListener(v12 -> {
                imageViewModel.setDeleteImageObj(String.valueOf(Config.ITEM_DELETE_COUNT), String.valueOf(imageViewModel.offset), loginUserId, fourthImageId);

                psDialogMsg.cancel();

                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().fouthImageView, "");

            });


            psDialogMsg.cancelButton.setOnClickListener(v1 -> psDialogMsg.cancel());
        });

        binding.get().deleteFiveImageView.setOnClickListener(v -> {
            psDialogMsg.showConfirmDialog(ItemEntryFragment.this.getString(R.string.item_entry__confirm_delete_image), ItemEntryFragment.this.getString(R.string.app__ok), ItemEntryFragment.this.getString(R.string.message__cancel_close));
            psDialogMsg.show();

            psDialogMsg.okButton.setOnClickListener(v12 -> {
                imageViewModel.setDeleteImageObj(String.valueOf(Config.ITEM_DELETE_COUNT), String.valueOf(imageViewModel.offset), loginUserId, fifthImageId);

                psDialogMsg.cancel();

                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().fifthImageView, "");


            });

            psDialogMsg.cancelButton.setOnClickListener(v1 -> psDialogMsg.cancel());
        });


        binding.get().locationPickCardView.setOnClickListener(v -> {

            if (ActivityCompat.checkSelfPermission(
                    this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this.getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

            } else {
                LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
//                Location locationGPS = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                fusedLocationClient.requestLocationUpdates(locationRequest,
                        locationCallback,
                        Looper.getMainLooper());
                boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                if (!isGPSEnabled) {
                    psDialogMsg.showWarningDialog(ItemEntryFragment.this.getString(R.string.map_open_gps), ItemEntryFragment.this.getString(R.string.app__ok));
                    psDialogMsg.show();
                }

            }

        });

    }


    private void getImagePathList() {

        if (!itemViewModel.firstImagePath.isEmpty()) {
            imagePathList.add(itemViewModel.firstImagePath);
            imageUriList.add(itemViewModel.firstImageUri);

        }
        if (!itemViewModel.secImagePath.isEmpty()) {
            imagePathList.add(itemViewModel.secImagePath);
            imageUriList.add(itemViewModel.secImageUri);
        }
        if (!itemViewModel.thirdImagePath.isEmpty()) {
            imagePathList.add(itemViewModel.thirdImagePath);
            imageUriList.add(itemViewModel.thirdImageUri);
        }
        if (!itemViewModel.fourthImagePath.isEmpty()) {
            imagePathList.add(itemViewModel.fourthImagePath);
            imageUriList.add(itemViewModel.fourthImageUri);
        }
        if (!itemViewModel.fifthImagePath.isEmpty()) {
            imagePathList.add(itemViewModel.fifthImagePath);
            imageUriList.add(itemViewModel.fifthImageUri);
        }
    }

    private void checkIsShop() {

        if (binding.get().isShopCheckBox.isChecked()) {
            businessMode = Constants.ONE;
        } else {
            businessMode = Constants.ZERO;
        }
    }

    private void ButtonSheetClick(String flag) {
        bottomBoxLayoutBinding.get().cameraButton.setOnClickListener(v -> {

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                openCamera(flag);
            } else {
                if (getActivity() != null) {
                    if ((getActivity()).checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
                            ((getActivity()).checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)) {

                        String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(permission, Constants.REQUEST_CODE__PERMISSION_CODE);
                    } else {
                        //granted
//                        navigationController.navigateToCamera(getActivity());
                        openCamera(flag);
                    }
                }
            }

            mBottomSheetDialog.get().dismiss();
        });

        bottomBoxLayoutBinding.get().galleryButton.setOnClickListener(v -> {

//            bottomBoxLayoutBinding.get().galleryButton.setBackgroundResource(R.color.button__select_green);
//            bottomBoxLayoutBinding.get().cameraButton.setBackgroundResource(R.color.md_white_1000);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                navigationController.navigateToGallery(getActivity(), flag);
            } else {
                if (getActivity() != null) {
                    if ((getActivity()).checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
                            ((getActivity()).checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)) {

                        String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(permission, Constants.REQUEST_CODE__PERMISSION_CODE);
                    } else {
                        //granted
                        navigationController.navigateToGallery(getActivity(), flag);
                    }
                }
            }

            mBottomSheetDialog.get().dismiss();

        });


    }

//    Uri photoURI;
//    private void dispatchTakePictureIntent() {
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        // Ensure that there's a camera activity to handle the intent
//        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
//            // Create the File where the photo should go
//            File photoFile = null;
//            try {
//                photoFile = createImageFile();
//            } catch (IOException ex) {
//                // Error occurred while creating the File
//
//            }
//            // Continue only if the File was successfully created
//            if (photoFile != null) {
//                photoURI = FileProvider.getUriForFile(getContext(),
//                        "com.panaceasoft.psbuyandsell.fileprovider",
//                        photoFile);
//                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
//                startActivityForResult(takePictureIntent,  Constants.REQUEST_CODE__SEC_CAMERA);
//            }
//        }
//    }

//    private File createImageFile() throws IOException {
//        // Create an image file name
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        String imageFileName = "JPEG_" + timeStamp + "_";
//        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//        File image = File.createTempFile(
//                imageFileName,  /* prefix */
//                ".jpg",         /* suffix */
//                storageDir      /* directory */
//        );
//
//        // Save a file: path for use with ACTION_VIEW intents
//        currentPhotoPath = image.getAbsolutePath();
//        return image;
//    }

    private void openCamera(String flag) {
        if (getActivity() != null) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Images.Media.TITLE, "New Picture");
            contentValues.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");

            if (cameraType.equals(Constants.DEFAULT_CAMERA)) {
                navigationController.navigateToCamera(getActivity(), flag);
//                dispatchTakePictureIntent();
            } else {
                navigationController.navigateToCustomCamera(getActivity(), flag);
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.REQUEST_CODE__PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //open camera
//                navigationController.navigateToCamera(getActivity());
                openCamera(Constants.ZERO);
            } else {
                //permission denied
                Toast.makeText(getContext(), "permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void initViewModels() {
        itemViewModel = new ViewModelProvider(this, viewModelFactory).get(ItemViewModel.class);
        imageViewModel = new ViewModelProvider(this, viewModelFactory).get(ImageViewModel.class);

    }

    @Override
    protected void initAdapters() {

    }

    @Override
    protected void initData() {

        getIntentData();

        bindingLatLng(itemViewModel.latValue, itemViewModel.lngValue);

        getItemDetail();

        getImageList();

        getDeleteImageStatus();

        itemViewModel.getUploadItemData().observe(this, result -> {

            if (result != null) {

                switch (result.status) {
                    case SUCCESS:
                        if (result.data != null) {
                            if (selected) {

                                boolean isLastResult = true;
                                itemViewModel.itemId = result.data.id;

                                if (isFirstImageSelected) {//reload
                                    if (imagePathList.size() > 1) {//multi image from start
                                        itemViewModel.setUploadItemImageObj(imagePathList.get(0), imageUriList.get(0), itemViewModel.itemId, firstImageId, "1", (getActivity()).getContentResolver());
                                    } else {//single image from end for last update
                                        itemViewModel.setUploadItemImageObj(imagePathList.get(imagePathList.size() - 1), imageUriList.get(imageUriList.size() - 1), itemViewModel.itemId, firstImageId, "1", (getActivity()).getContentResolver());

                                    }
                                    if (!progressDialog.isShowing()) {
                                        progressDialog.show();
                                    }
                                    isFirstImageSelected = false;
                                    isLastResult = false;
                                } else if (isSecImageSelected) {
                                    if (imagePathList.size() > 1) {
                                        itemViewModel.setUploadItemImageObj(imagePathList.get(0), imageUriList.get(0), itemViewModel.itemId, secImageId, "2", (getActivity()).getContentResolver());
                                    } else {
                                        itemViewModel.setUploadItemImageObj(imagePathList.get(imagePathList.size() - 1), imageUriList.get(imageUriList.size() - 1), itemViewModel.itemId, secImageId, "2", (getActivity()).getContentResolver());
                                    }
                                    if (!progressDialog.isShowing()) {
                                        progressDialog.show();
                                    }
                                    isSecImageSelected = false;
                                    isLastResult = false;
                                } else if (isThirdImageSelected) {
                                    if (imagePathList.size() > 1) {
                                        itemViewModel.setUploadItemImageObj(imagePathList.get(0), imageUriList.get(0), itemViewModel.itemId, thirdImageId, "3", (getActivity()).getContentResolver());
                                    } else {
                                        itemViewModel.setUploadItemImageObj(imagePathList.get(imagePathList.size() - 1), imageUriList.get(imageUriList.size() - 1), itemViewModel.itemId, thirdImageId, "3", (getActivity()).getContentResolver());
                                    }
                                    if (!progressDialog.isShowing()) {
                                        progressDialog.show();
                                    }
                                    isThirdImageSelected = false;
                                    isLastResult = false;
                                } else if (isFourthImageSelected) {
                                    if (imagePathList.size() > 1) {
                                        itemViewModel.setUploadItemImageObj(imagePathList.get(0), imageUriList.get(0), itemViewModel.itemId, fourthImageId, "4", (getActivity()).getContentResolver());
                                    } else {
                                        itemViewModel.setUploadItemImageObj(imagePathList.get(imagePathList.size() - 1), imageUriList.get(imageUriList.size() - 1), itemViewModel.itemId, fourthImageId, "4", (getActivity()).getContentResolver());
                                    }
                                    if (!progressDialog.isShowing()) {
                                        progressDialog.show();
                                    }
                                    isFourthImageSelected = false;
                                    isLastResult = false;
                                } else if (isFifthImageSelected) {
                                    if (imagePathList.size() > 1) {
                                        itemViewModel.setUploadItemImageObj(imagePathList.get(0), imageUriList.get(0), itemViewModel.itemId, fifthImageId, "5", (getActivity()).getContentResolver());
                                    } else {
                                        itemViewModel.setUploadItemImageObj(imagePathList.get(imagePathList.size() - 1), imageUriList.get(imageUriList.size() - 1), itemViewModel.itemId, fifthImageId, "5", (getActivity()).getContentResolver());
                                    }
                                    if (!progressDialog.isShowing()) {
                                        progressDialog.show();
                                    }
                                    isFifthImageSelected = false;
                                    isLastResult = false;
                                }

                                if (isLastResult) {
                                    progressDialog.cancel();
                                }

                            } else {
                                Toast.makeText(getActivity(), "item upload success", Toast.LENGTH_SHORT).show();
                                progressDialog.cancel();

                                if (Config.CLOSE_ENTRY_AFTER_SUBMIT) {
                                    if (getActivity() != null) {
                                        getActivity().finish();
                                    }
                                }

                            }

                        }

                        break;

                    case ERROR:
                        progressDialog.cancel();
                        psDialogMsg.showErrorDialog(getString(R.string.error_message__item_cannot_upload), getString(R.string.app__ok));
                        psDialogMsg.show();
                        break;
                }
            }

        });


        itemViewModel.getUploadItemImageData().observe(this, result -> {

            if (result != null) {
                switch (result.status) {
                    case SUCCESS:
                        progressDialog.cancel();
                        if (!isUploadSuccess) {
//                            int toastImageCount = imageCount+1;
//                            Toast.makeText(ItemEntryFragment.this.getActivity(), "Success image : "+toastImageCount+" uploaded", Toast.LENGTH_SHORT).show();
                            imageCount += 1;

                            if (imagePathList.size() > imageCount) {
                                ItemEntryFragment.this.callImageUpload(imageCount);//first is one
                            } else {

                                isUploadSuccess = true;
                                imageViewModel.setImageParentId(Constants.IMAGE_TYPE_PRODUCT, itemViewModel.itemId);

                                if (Config.CLOSE_ENTRY_AFTER_SUBMIT) {
                                    if (getActivity() != null) {
                                        getActivity().finish();
                                    }
                                }


                            }
                        }
//                        else {
//                            if (ItemEntryFragment.this.getActivity() != null) {
//                                ItemEntryFragment.this.getActivity().finish();
//                            }
//                        }

                        break;

                    case ERROR:

                        Toast.makeText(ItemEntryFragment.this.getActivity(), "Error", Toast.LENGTH_SHORT).show();
                        progressDialog.cancel();
                        psDialogMsg.showErrorDialog(ItemEntryFragment.this.getString(R.string.error_message__image_cannot_upload), ItemEntryFragment.this.getString(R.string.app__ok));
                        psDialogMsg.show();
                        break;
                }

            }
        });

    }


    private void getImageList() {
        LiveData<Resource<List<Image>>> imageListLiveData = imageViewModel.getImageListLiveData();
        imageViewModel.setImageParentId(Constants.IMAGE_TYPE_PRODUCT, itemViewModel.itemId);
        imageListLiveData.observe(this, listResource -> {
            // we don't need any null checks here for the adapter since LiveData guarantees that
            // it won't call us if fragment is stopped or not started.
            if (listResource != null && listResource.data != null) {
                Utils.psLog("Got Data");

                //fadeIn Animation
                fadeIn(binding.get().getRoot());

                // Update the data
                bindingImageListData(listResource.data);

//                this.binding.get().executePendingBindings();

            } else {
                //noinspection Constant Conditions
                Utils.psLog("Empty Data");
            }
        });
    }

    private void bindingImageListData(List<Image> imageList) {

        if (imageList.size() != 0) {
            if (imageList.size() == 1) {
                firstImageId = imageList.get(0).imgId;
                secImageId = "";
                thirdImageId = "";
                fourthImageId = "";
                fifthImageId = "";
//                itemViewModel.firstImagePath = imageList.get(0).imgPath;
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().firstImageView, imageList.get(0).imgPath);
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().secImageView, "");
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().thirdImageView, "");
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().fouthImageView, "");
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().fifthImageView, "");

                if (secImageId.equals("")) {
                    binding.get().deleteSecondImageView.setVisibility(View.GONE);
                } else {
                    binding.get().deleteSecondImageView.setVisibility(View.VISIBLE);
                }
                if (thirdImageId.equals("")) {
                    binding.get().deleteThirdImageView.setVisibility(View.GONE);
                } else {
                    binding.get().deleteThirdImageView.setVisibility(View.VISIBLE);
                }
                if (fourthImageId.equals("")) {
                    binding.get().deleteFouthImageView.setVisibility(View.GONE);
                } else {
                    binding.get().deleteFouthImageView.setVisibility(View.VISIBLE);
                }
                if (fifthImageId.equals("")) {
                    binding.get().deleteFiveImageView.setVisibility(View.GONE);
                } else {
                    binding.get().deleteFiveImageView.setVisibility(View.VISIBLE);
                }
            }
            if (imageList.size() == 2) {
                firstImageId = imageList.get(0).imgId;
                secImageId = imageList.get(1).imgId;
                thirdImageId = "";
                fourthImageId = "";
                fifthImageId = "";
//                itemViewModel.firstImagePath = imageList.get(0).imgPath;
//                itemViewModel.secImagePath = imageList.get(1).imgPath;
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().firstImageView, imageList.get(0).imgPath);
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().secImageView, imageList.get(1).imgPath);
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().thirdImageView, "");
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().fouthImageView, "");
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().fifthImageView, "");

                if (secImageId.equals("")) {
                    binding.get().deleteSecondImageView.setVisibility(View.GONE);
                } else {
                    binding.get().deleteSecondImageView.setVisibility(View.VISIBLE);
                }
                if (thirdImageId.equals("")) {
                    binding.get().deleteThirdImageView.setVisibility(View.GONE);
                } else {
                    binding.get().deleteThirdImageView.setVisibility(View.VISIBLE);
                }
                if (fourthImageId.equals("")) {
                    binding.get().deleteFouthImageView.setVisibility(View.GONE);
                } else {
                    binding.get().deleteFouthImageView.setVisibility(View.VISIBLE);
                }
                if (fifthImageId.equals("")) {
                    binding.get().deleteFiveImageView.setVisibility(View.GONE);
                } else {
                    binding.get().deleteFiveImageView.setVisibility(View.VISIBLE);
                }
            }
            if (imageList.size() == 3) {
                firstImageId = imageList.get(0).imgId;
                secImageId = imageList.get(1).imgId;
                thirdImageId = imageList.get(2).imgId;
                fourthImageId = "";
                fifthImageId = "";
//                itemViewModel.firstImagePath = imageList.get(0).imgPath;
//                itemViewModel.secImagePath = imageList.get(1).imgPath;
//                itemViewModel.thirdImagePath = imageList.get(2).imgPath;
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().firstImageView, imageList.get(0).imgPath);
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().secImageView, imageList.get(1).imgPath);
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().thirdImageView, imageList.get(2).imgPath);
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().fouthImageView, "");
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().fifthImageView, "");

                if (secImageId.equals("")) {
                    binding.get().deleteSecondImageView.setVisibility(View.GONE);
                } else {
                    binding.get().deleteSecondImageView.setVisibility(View.VISIBLE);
                }
                if (thirdImageId.equals("")) {
                    binding.get().deleteThirdImageView.setVisibility(View.GONE);
                } else {
                    binding.get().deleteThirdImageView.setVisibility(View.VISIBLE);
                }
                if (fourthImageId.equals("")) {
                    binding.get().deleteFouthImageView.setVisibility(View.GONE);
                } else {
                    binding.get().deleteFouthImageView.setVisibility(View.VISIBLE);
                }
                if (fifthImageId.equals("")) {
                    binding.get().deleteFiveImageView.setVisibility(View.GONE);
                } else {
                    binding.get().deleteFiveImageView.setVisibility(View.VISIBLE);
                }
            }
            if (imageList.size() == 4) {
                firstImageId = imageList.get(0).imgId;
                secImageId = imageList.get(1).imgId;
                thirdImageId = imageList.get(2).imgId;
                fourthImageId = imageList.get(3).imgId;
                fifthImageId = "";

                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().firstImageView, imageList.get(0).imgPath);
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().secImageView, imageList.get(1).imgPath);
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().thirdImageView, imageList.get(2).imgPath);
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().fouthImageView, imageList.get(3).imgPath);
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().fifthImageView, "");

                if (secImageId.equals("")) {
                    binding.get().deleteSecondImageView.setVisibility(View.GONE);
                } else {
                    binding.get().deleteSecondImageView.setVisibility(View.VISIBLE);
                }
                if (thirdImageId.equals("")) {
                    binding.get().deleteThirdImageView.setVisibility(View.GONE);
                } else {
                    binding.get().deleteThirdImageView.setVisibility(View.VISIBLE);
                }
                if (fourthImageId.equals("")) {
                    binding.get().deleteFouthImageView.setVisibility(View.GONE);
                } else {
                    binding.get().deleteFouthImageView.setVisibility(View.VISIBLE);
                }
                if (fifthImageId.equals("")) {
                    binding.get().deleteFiveImageView.setVisibility(View.GONE);
                } else {
                    binding.get().deleteFiveImageView.setVisibility(View.VISIBLE);
                }
            }
            if (imageList.size() == 5) {
                firstImageId = imageList.get(0).imgId;
                secImageId = imageList.get(1).imgId;
                thirdImageId = imageList.get(2).imgId;
                fourthImageId = imageList.get(3).imgId;
                fifthImageId = imageList.get(4).imgId;
//                itemViewModel.firstImagePath = imageList.get(0).imgPath;
//                itemViewModel.secImagePath = imageList.get(1).imgPath;
//                itemViewModel.thirdImagePath = imageList.get(2).imgPath;
//                itemViewModel.fouthImagePath = imageList.get(3).imgPath;
//                itemViewModel.fifthImagePath = imageList.get(4).imgPath;
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().firstImageView, imageList.get(0).imgPath);
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().secImageView, imageList.get(1).imgPath);
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().thirdImageView, imageList.get(2).imgPath);
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().fouthImageView, imageList.get(3).imgPath);
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().fifthImageView, imageList.get(4).imgPath);

                if (secImageId.equals("")) {
                    binding.get().deleteSecondImageView.setVisibility(View.GONE);
                } else {
                    binding.get().deleteSecondImageView.setVisibility(View.VISIBLE);
                }
                if (thirdImageId.equals("")) {
                    binding.get().deleteThirdImageView.setVisibility(View.GONE);
                } else {
                    binding.get().deleteThirdImageView.setVisibility(View.VISIBLE);
                }
                if (fourthImageId.equals("")) {
                    binding.get().deleteFouthImageView.setVisibility(View.GONE);
                } else {
                    binding.get().deleteFouthImageView.setVisibility(View.VISIBLE);
                }
                if (fifthImageId.equals("")) {
                    binding.get().deleteFiveImageView.setVisibility(View.GONE);
                } else {
                    binding.get().deleteFiveImageView.setVisibility(View.VISIBLE);
                }
            }
        } else {
            binding.get().deleteSecondImageView.setVisibility(View.GONE);
            binding.get().deleteThirdImageView.setVisibility(View.GONE);
            binding.get().deleteFouthImageView.setVisibility(View.GONE);
            binding.get().deleteFiveImageView.setVisibility(View.GONE);

        }
    }

    private void getItemDetail() {

        LiveData<Item> historyItemList = itemViewModel.getItemDetailFromDBByIdData();
        if (historyItemList != null) {
            historyItemList.observe(this, listResource -> {
                if (listResource != null) {
                    bindingItemDetailData(listResource);

                }

            });
        }
    }

    private void getDeleteImageStatus() {

        imageViewModel.getDeleteImageStatus().observe(this, result -> {

            if (result != null) {
                switch (result.status) {
                    case SUCCESS:

                        //add offer text
                        Toast.makeText(getContext(), "Success Delete this Image", Toast.LENGTH_SHORT).show();

                        break;

                    case ERROR:
                        Toast.makeText(getContext(), "Fail Delete Image", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }

    private void bindingItemDetailData(Item item) {
        binding.get().titleEditText.setText(item.title);
        itemViewModel.holder.cat_id = item.catId;
        itemViewModel.holder.sub_cat_id = item.subCatId;
        itemViewModel.holder.type_id = item.itemTypeId;
        itemViewModel.holder.condition_id = item.conditionOfItem;
        itemViewModel.holder.price_type_id = item.itemPriceTypeId;
        itemViewModel.holder.currency_id = item.itemCurrencyId;
        itemViewModel.holder.location_id = item.itemLocation.id;
        itemViewModel.holder.location_township_id = item.itemTownshipLocation.id;
        itemViewModel.holder.deal_option_id = item.dealOptionId;
        itemViewModel.is_sold_out = item.isSoldOut;
        this.catId = item.catId;
        this.subCatId = item.subCatId;
        this.typeId = item.itemTypeId;
        this.conditionId = item.conditionOfItem;
        this.priceTypeId = item.itemPriceTypeId;
        this.currencyId = item.itemCurrencyId;
        this.locationId = item.itemLocation.id;
        this.locationTownshipId = item.itemTownshipLocation.id;
        this.dealOptionId = item.dealOptionId;
        binding.get().categoryTextView.setText(item.category.name);
        binding.get().subCategoryTextView.setText(item.subCategory.name);
        binding.get().typeTextView.setText(item.itemType.name);
        binding.get().priceTextView.setText(item.itemCurrency.currencySymbol);
        binding.get().locationTextView.setText(item.itemLocation.name);
        binding.get().townshipTextView.setText(item.itemTownshipLocation.townshipName);
        bindMap(item.lat, item.lng);
        itemViewModel.mapLat = item.lat;
        itemViewModel.mapLng = item.lng;
        bindingLatLng(item.lat, item.lng);
        binding.get().brandEditText.setText(item.brand);
        binding.get().priceEditText.setText(item.price);
        binding.get().descEditText.setText(item.description);
         bindingIsShop(item.businessMode);
        binding.get().addressEditText.setText(item.address);
        binding.get().ResgisterEditText.setText(item.birthday);
        binding.get().ManufacturingEditText.setText(item.birthday1);
        binding.get().HpEditText.setText(item.birthday2);
        binding.get().GearSystemEditText.setText(item.birthday3);
        binding.get().HoursUsedEditText.setText(item.birthday4);
        binding.get().HandsDropEditText.setText(item.birthday5);

    }

    private void bindingIsShop(String businessMode) {
        if (businessMode.equals(Constants.ONE)) {
            binding.get().isShopCheckBox.setChecked(true);
        } else {
            binding.get().isShopCheckBox.setChecked(false);
        }
    }

    private void getIntentData() {
        try {
            if (getActivity() != null) {
                if (getActivity().getIntent().getExtras() != null) {

                    itemViewModel.itemId = getActivity().getIntent().getExtras().getString(Constants.ITEM_ID);
                    this.locationId = getActivity().getIntent().getExtras().getString(Constants.SELECTED_LOCATION_ID);
                    itemViewModel.holder.location_id = this.locationId;
                    String locationName = getActivity().getIntent().getExtras().getString(Constants.SELECTED_LOCATION_NAME);
                    if (locationName.equals(Constants.LOCATION_ALL)) {
                        binding.get().locationTextView.setText("");
                    } else {
                        binding.get().locationTextView.setText(locationName);
                    }

                    bindMap(selectedLat, selectedLng);

                    bindingLatLng(selectedLat, selectedLng);

                    this.locationTownshipId = getActivity().getIntent().getExtras().getString(Constants.SELECTED_TOWNSHIP_ID);
                    itemViewModel.holder.location_township_id = this.locationTownshipId;
                    String locationTownshipName = getActivity().getIntent().getExtras().getString(Constants.SELECTED_TOWNSHIP_NAME);
                    if (locationTownshipName.equals(Constants.LOCATION_ALL)) {
                        binding.get().townshipTextView.setText("");
                    } else {
                        binding.get().townshipTextView.setText(locationTownshipName);
                    }
                    binding.get().priceTextView.setText(defaultCurrency);
                    itemViewModel.holder.currency_id = defaultCurrencyId;

                    if (itemViewModel.itemId != null) {
                        if (!itemViewModel.itemId.equals(Constants.ADD_NEW_ITEM)) {//edit

                            itemViewModel.setItemDetailFromDBById(itemViewModel.itemId);

                        }
                    }
                }
            }
        } catch (Exception e) {
            Utils.psErrorLog("", e);
        }
    }

    private void callImageUpload(int imageCount) {

        if (isSecImageSelected) {
            itemViewModel.setUploadItemImageObj(imagePathList.get(imageCount), imageUriList.get(imageCount), itemViewModel.itemId, secImageId, "2", (getActivity()).getContentResolver());
            isSecImageSelected = false;
        } else if (isThirdImageSelected) {
            itemViewModel.setUploadItemImageObj(imagePathList.get(imageCount), imageUriList.get(imageCount), itemViewModel.itemId, thirdImageId, "3", (getActivity()).getContentResolver());
            isThirdImageSelected = false;
        } else if (isFourthImageSelected) {
            itemViewModel.setUploadItemImageObj(imagePathList.get(imageCount), imageUriList.get(imageCount), itemViewModel.itemId, fourthImageId, "4", (getActivity()).getContentResolver());
            isFourthImageSelected = false;
        } else if (isFifthImageSelected) {
            itemViewModel.setUploadItemImageObj(imagePathList.get(imageCount), imageUriList.get(imageCount), itemViewModel.itemId, fifthImageId, "5", (getActivity()).getContentResolver());
            isFifthImageSelected = false;
        }

    }

    private void bindingLatLng(String latValue, String lngValue) {
        //  binding.get().latitudeEditText.setText(latValue);
        //binding.get().lngEditText.setText(lngValue);
    }

    private void changeCamera() {

        if (marker != null) {
            marker.remove();
        }

        map.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(new LatLng(Double.valueOf(itemViewModel.latValue), Double.valueOf(itemViewModel.lngValue))).zoom(10).bearing(10).tilt(10).build()));

        marker = map.addMarker(new MarkerOptions()
                .position(new LatLng(Double.valueOf(itemViewModel.latValue), Double.valueOf(itemViewModel.lngValue)))
                .title("Shop Name"));
    }

}
