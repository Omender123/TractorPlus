package com.prospective.tractorplus.ui.user;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.prospective.tractorplus.R;
import com.prospective.tractorplus.binding.FragmentDataBindingComponent;
import com.prospective.tractorplus.databinding.FragmentProfileEditBinding;
import com.prospective.tractorplus.ui.common.PSFragment;
import com.prospective.tractorplus.utils.AutoClearedValue;
import com.prospective.tractorplus.utils.Constants;
import com.prospective.tractorplus.utils.PSDialogMsg;
import com.prospective.tractorplus.utils.Utils;
import com.prospective.tractorplus.viewmodel.user.UserViewModel;
import com.prospective.tractorplus.viewobject.User;

/**
 * ProfileEditFragment
 */
public class ProfileEditFragment extends PSFragment {


    //region Variables

    private final androidx.databinding.DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);

    private UserViewModel userViewModel;

    private PSDialogMsg psDialogMsg;

    @VisibleForTesting
    private AutoClearedValue<FragmentProfileEditBinding> binding;

    private AutoClearedValue<ProgressDialog> prgDialog;


    //endregion


    //region Override Methods

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        // Inflate the layout for this fragment
        FragmentProfileEditBinding dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile_edit, container, false, dataBindingComponent);

        binding = new AutoClearedValue<>(this, dataBinding);

        return binding.get().getRoot();
    }

    @Override
    protected void initViewModels() {
        userViewModel = new ViewModelProvider(this, viewModelFactory).get(UserViewModel.class);
    }

    @Override
    protected void initAdapters() {

    }

    @Override
    protected void initData() {

        userViewModel.getLoginUser().observe(this, listResource -> {
            // we don't need any null checks here for the adapter since LiveData guarantees that
            // it won't call us if fragment is stopped or not started.
            if (listResource != null && listResource.size() > 0) {
                Utils.psLog("Got Data");

                //fadeIn Animation
                fadeIn(binding.get().getRoot());

                binding.get().setUser(listResource.get(0).user);
                userViewModel.user = listResource.get(0).user;
                userViewModel.showEmail = listResource.get(0).user.isShowEmail;
                userViewModel.showPhone = listResource.get(0).user.isShowPhone;

                if (userViewModel.showEmail.equals(Constants.ONE)) {
                    binding.get().emailCheckBox.setChecked(true);
                } else {
                    binding.get().emailCheckBox.setChecked(false);
                }

                if (userViewModel.showPhone.equals(Constants.ONE)) {
                    binding.get().phoneNoCheckBox.setChecked(true);
                } else {
                    binding.get().phoneNoCheckBox.setChecked(false);
                }

                Utils.psLog("Photo : " + listResource.get(0).user.userProfilePhoto);
            } else {
                //noinspection Constant Conditions
                Utils.psLog("Empty Data");

            }
        });

    }


    @Override
    protected void initUIAndActions() {

        psDialogMsg = new PSDialogMsg(getActivity(), false);

        if (getContext() != null) {
            binding.get().userNameEditText.setHint(R.string.edit_profile__user_name);
            binding.get().userEmailEditText.setHint(R.string.edit_profile__email);
            binding.get().userPhoneEditText.setHint(R.string.edit_profile__phone);
            binding.get().userAboutMeEditText.setHint(R.string.edit_profile__about_me);

            binding.get().nameTextView.setText(getContext().getString(R.string.edit_profile__user_name));
            binding.get().emailTextView.setText(getContext().getString(R.string.edit_profile__email));
            binding.get().phoneTextView.setText(getContext().getString(R.string.edit_profile__phone));
            binding.get().aboutMeTextView.setText(getContext().getString(R.string.edit_profile__about_me));

            binding.get().emailCheckBox.setText(getContext().getString(R.string.edit_profile__show_email));
            binding.get().phoneNoCheckBox.setText(getContext().getString(R.string.edit_profile__show_phone_no));

            binding.get().userPhoneEditText.setEnabled(false);

        }

        // Init Dialog
        prgDialog = new AutoClearedValue<>(this, new ProgressDialog(getActivity()));

        prgDialog.get().setMessage((Utils.getSpannableString(getContext(), getString(R.string.message__please_wait), Utils.Fonts.MM_FONT)));
        prgDialog.get().setCancelable(false);

        binding.get().profileImageView.setOnClickListener(view -> {

            if (connectivity.isConnected()) {
                try {

                    if (Utils.isStoragePermissionGranted(getActivity())) {
                        Intent i = new Intent(
                                Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                        startActivityForResult(i, Constants.RESULT_LOAD_IMAGE);
                    }
                } catch (Exception e) {
                    Utils.psErrorLog("Error in Image Gallery.", e);
                }
            } else {

                psDialogMsg.showWarningDialog(getString(R.string.no_internet_error), getString(R.string.app__ok));
                psDialogMsg.show();
            }

        });

        binding.get().saveButton.setOnClickListener(view -> ProfileEditFragment.this.editProfileData());

        binding.get().passwordChangeButton.setOnClickListener(view -> navigationController.navigateToPasswordChangeActivity(ProfileEditFragment.this.getActivity()));

        binding.get().editImageButton.setOnClickListener(view -> {
            navigationController.navigateToEditPhoneChangeActivity(getActivity());
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if(data != null){
                if (requestCode == Constants.RESULT_LOAD_IMAGE && resultCode == Constants.RESULT_OK ) {
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    if (getActivity() != null && selectedImage != null) {
                        Cursor cursor = getActivity().getContentResolver().query(selectedImage,
                                filePathColumn, null, null, null);

                        if (cursor != null) {
                            cursor.moveToFirst();

                            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                            userViewModel.profileImagePath = cursor.getString(columnIndex);
                            cursor.close();

                            uploadImage(selectedImage);
                        }

                    }
                } else if (requestCode == Constants.REQUEST_CODE__EDIT_PHONE_VERIFY_ACTIVITY &&
                        resultCode == Constants.RESULT_CODE__EDIT_PROFILE_ACTIITY) {
                    userViewModel.userPhoneNo = data.getStringExtra(Constants.USER_PHONE);
                    binding.get().userPhoneEditText.setText(userViewModel.userPhoneNo);
                }
            }

        } catch (Exception e) {
            Utils.psErrorLog("Error in load image.", e);
        }
    }


    //endregion


    //region Private Methods


    private void editProfileData() {

        if (!connectivity.isConnected()) {

            psDialogMsg.showWarningDialog(getString(R.string.no_internet_error), getString(R.string.app__ok));
            psDialogMsg.show();
            return;
        }

        String userName = binding.get().userNameEditText.getText().toString();
        if (userName.equals("")) {

            psDialogMsg.showWarningDialog(getString(R.string.error_message__blank_name), getString(R.string.app__ok));
            psDialogMsg.show();
            return;
        }

        String userEmail = binding.get().userEmailEditText.getText().toString();
        if (userEmail.equals("")) {

            psDialogMsg.showWarningDialog(getString(R.string.error_message__blank_email), getString(R.string.app__ok));
            psDialogMsg.show();
            return;
        }

        if (!checkToUpdateProfile()) {
            updateUserProfile();
        }

        userViewModel.getUpdateUserData().observe(this, listResource -> {

            if (listResource != null) {

                Utils.psLog("Got Data" + listResource.message + listResource.toString());

                switch (listResource.status) {
                    case LOADING:
                        // Loading State
                        // Data are from Local DB
//                            if(listResource.data != null){
//                                fadeIn(binding.get().getRoot());
//                            }

                        break;
                    case SUCCESS:
                        // Success State
                        // Data are from Server

                        if (listResource.data != null) {

                            psDialogMsg.showSuccessDialog(getString(R.string.profile_edit__success), getString(R.string.app__ok));
                            psDialogMsg.show();
                            psDialogMsg.okButton.setOnClickListener(view -> psDialogMsg.cancel());


                        }
                        userViewModel.setLoadingState(false);
                        psDialogMsg.showSuccessDialog(getString(R.string.profile_edit__success), getString(R.string.app__ok));
                        psDialogMsg.show();
                        psDialogMsg.okButton.setOnClickListener(view -> {
                            psDialogMsg.cancel();
                            prgDialog.get().cancel();
                            if(getActivity() != null){
                                getActivity().finish();
                            }
                        });

                        break;
                    case ERROR:
                        // Error State

                        psDialogMsg.showErrorDialog(listResource.message, getString(R.string.app__ok));
                        psDialogMsg.show();
                        prgDialog.get().cancel();

                        userViewModel.setLoadingState(false);
                        break;
                    default:


                        break;
                }

            } else {

                // Init Object or Empty Data
                Utils.psLog("Empty Data");
            }


        });
    }


    private void uploadImage(Uri uri) {

        prgDialog.get().show();
        Utils.psLog("Uploading Image.");

        if (getActivity() != null) {
            if (userViewModel.profileImagePath.contains(".webp")) {
                prgDialog.get().cancel();
                psDialogMsg.showErrorDialog(getString(R.string.error_message__webp_image), getString(R.string.app__ok));
                psDialogMsg.show();
            }else {
                userViewModel.uploadImage(getContext(), userViewModel.profileImagePath, uri, loginUserId, getActivity().getContentResolver()).observe(this, listResource -> {
                    // we don't need any null checks here for the SubCategoryAdapter since LiveData guarantees that
                    // it won't call us if fragment is stopped or not started.
                    if (listResource != null && listResource.data != null) {
                        Utils.psLog("Got Data" + listResource.message + listResource.toString());

                        prgDialog.get().cancel();

                    } else if (listResource != null && listResource.message != null) {
                        Utils.psLog("Message from server.");

                        psDialogMsg.showInfoDialog(listResource.message, getString(R.string.app__ok));
                        psDialogMsg.show();

                        prgDialog.get().cancel();
                    } else {
                        //noinspection Constant Conditions
                        Utils.psLog("Empty Data");

                    }

                });
            }
        }
    }

    private void checkShowEmail() {

        if (binding.get().emailCheckBox.isChecked()) {
            userViewModel.showEmail = Constants.ONE;
        } else {
            userViewModel.showEmail = Constants.ZERO;
        }

    }

    private void checkShowPhone() {

        if (binding.get().phoneNoCheckBox.isChecked()) {
            userViewModel.showPhone = Constants.ONE;
        } else {
            userViewModel.showPhone = Constants.ZERO;
        }

    }

    private boolean checkToUpdateProfile() {

        checkShowEmail();
        checkShowPhone();

        if(userViewModel.userPhoneNo == null){
            userViewModel.userPhoneNo = userViewModel.user.userPhone;
        }

        return binding.get().userNameEditText.getText().toString().equals(userViewModel.user.userName) &&
                binding.get().userEmailEditText.getText().toString().equals(userViewModel.user.userEmail) &&
                userViewModel.userPhoneNo.equals(userViewModel.user.userPhone) &&
                binding.get().userAboutMeEditText.getText().toString().equals(userViewModel.user.userAboutMe) &&
                binding.get().userAddressEditText.getText().toString().equals(userViewModel.user.userAddress) &&
                binding.get().userCityEditText.getText().toString().equals(userViewModel.user.city) &&
                userViewModel.showEmail.equals(userViewModel.user.isShowEmail) &&
                userViewModel.showPhone.equals(userViewModel.user.isShowPhone
                );
    }


    private void updateUserProfile() {

        checkShowEmail();
        checkShowPhone();

        if(userViewModel.userPhoneNo == null){
            userViewModel.userPhoneNo = userViewModel.user.userPhone;
        }

        User user = new User(userViewModel.user.userId,
                userViewModel.user.userIsSysAdmin,
                userViewModel.user.isCityAdmin,
                userViewModel.user.facebookId,
                userViewModel.user.phoneId,
                userViewModel.user.googleId,
                binding.get().userNameEditText.getText().toString(),
                binding.get().userEmailEditText.getText().toString(),
                userViewModel.userPhoneNo,
                binding.get().userAddressEditText.getText().toString(),
                binding.get().userCityEditText.getText().toString(),
                userViewModel.user.userEmail,
                binding.get().userAboutMeEditText.getText().toString(),
                userViewModel.showEmail,
                userViewModel.showPhone,
                userViewModel.user.userCoverPhoto,
                userViewModel.user.userProfilePhoto,
                userViewModel.user.roleId,
                userViewModel.user.status,
                userViewModel.user.isBanned,
                userViewModel.user.addedDate,
                userViewModel.user.deviceToken,
                userViewModel.user.code,
                userViewModel.user.overallRating,
                userViewModel.user.whatsapp,
                userViewModel.user.messenger,
                userViewModel.user.followerCount,
                userViewModel.user.followingCount,
                userViewModel.user.isFollowed,
                userViewModel.user.added_date_str,
                userViewModel.user.verifyTypes,
                userViewModel.user.emailVerify,
                userViewModel.user.facebookVerify,
                userViewModel.user.googleVerify,
                userViewModel.user.phoneVerify,
                userViewModel.user.ratingCount,
                userViewModel.user.ratingDetails);

        userViewModel.setUpdateUserObj(user);

        prgDialog.get().show();
    }


    //endregion
}
