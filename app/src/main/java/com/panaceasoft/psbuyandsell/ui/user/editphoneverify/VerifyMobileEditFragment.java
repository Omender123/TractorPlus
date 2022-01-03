package com.panaceasoft.psbuyandsell.ui.user.editphoneverify;


import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.panaceasoft.psbuyandsell.MainActivity;
import com.panaceasoft.psbuyandsell.R;
import com.panaceasoft.psbuyandsell.binding.FragmentDataBindingComponent;
import com.panaceasoft.psbuyandsell.databinding.FragmentVerifyPhoneEditBinding;
import com.panaceasoft.psbuyandsell.ui.common.PSFragment;
import com.panaceasoft.psbuyandsell.utils.AutoClearedValue;
import com.panaceasoft.psbuyandsell.utils.Constants;
import com.panaceasoft.psbuyandsell.utils.PSDialogMsg;
import com.panaceasoft.psbuyandsell.utils.Utils;
import com.panaceasoft.psbuyandsell.viewmodel.user.UserViewModel;
import com.panaceasoft.psbuyandsell.viewobject.User;

import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 */
public class VerifyMobileEditFragment extends PSFragment {


    //region Variables

    private final androidx.databinding.DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);

    private UserViewModel userViewModel;

    private PSDialogMsg psDialogMsg;

    private String userPhoneNo, userName, phoneUserId, token;
    private FirebaseAuth mAuth;
    private String mVerificationId;
    public  String userEmail;

    @VisibleForTesting
    private AutoClearedValue<FragmentVerifyPhoneEditBinding> binding;

    private AutoClearedValue<ProgressDialog> prgDialog;

    //endregion


    //region Override Methods

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        // Inflate the layout for this fragment
        FragmentVerifyPhoneEditBinding dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_verify_phone_edit, container, false, dataBindingComponent);

        binding = new AutoClearedValue<>(this, dataBinding);

        if (getActivity() != null && getActivity().getIntent() != null) {
            userPhoneNo = getActivity().getIntent() .getStringExtra(Constants.USER_PHONE);

            if (getArguments() != null) {
                userPhoneNo = getArguments().getString(Constants.USER_PHONE);
            }

        }

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        sendVerificationCode(userPhoneNo);

        return binding.get().getRoot();
    }


    private void sendVerificationCode(String no) {
//        PhoneAuthProvider.getInstance().verifyPhoneNumber(
//                no,
//                60,
//                TimeUnit.SECONDS,
//                TaskExecutors.MAIN_THREAD,
//                mCallbacks);

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(no)                         // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(getActivity())                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)                   // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

    }


    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

            //Getting the code sent by SMS
            String code = phoneAuthCredential.getSmsCode();

            //sometime the code is not detected automatically
            //in this case the code will be null
            //so user has to manually enter the code
            if (code != null) {
                if (binding.get().enterCodeEditText != null) {
                    binding.get().enterCodeEditText.setText(code);
                }
                //verifying the code

                verifyVerificationCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);

            //storing the verification id that is sent to the user
            mVerificationId = s;
        }


    };


    @Override
    protected void initUIAndActions() {

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setToolbarText(((MainActivity) getActivity()).binding.toolbar, getString(R.string.verify_phone__title));
            ((MainActivity) this.getActivity()).binding.toolbar.setBackgroundColor(getResources().getColor(R.color.global__primary));
            ((MainActivity) getActivity()).updateMenuIconWhite();
            ((MainActivity) getActivity()).updateToolbarIconColor(Color.WHITE);

        }

        psDialogMsg = new PSDialogMsg(getActivity(), false);

        prgDialog = new AutoClearedValue<>(this, new ProgressDialog(getActivity()));
        prgDialog.get().setMessage((Utils.getSpannableString(getContext(), getString(R.string.message__please_wait), Utils.Fonts.MM_FONT)));
        prgDialog.get().setCancelable(false);

        binding.get().phoneTextView.setText(userPhoneNo);

        binding.get().submitButton.setOnClickListener(v -> {

            String code = binding.get().enterCodeEditText.getText().toString().trim();
            if (code.isEmpty() || code.length() < 6) {
                binding.get().enterCodeEditText.setError("Enter valid code");
                binding.get().enterCodeEditText.requestFocus();
                return;
            }

//                verifying the code entered manually
            verifyVerificationCode(code);

        });
    }

    private void verifyVerificationCode(String code) {

        if(mVerificationId != null) {
            //creating the credential
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);

            //signing the user
            signInWithPhoneAuthCredential(credential);
        } else {
            Toast.makeText(getContext(), R.string.verify_phone__code_processing, Toast.LENGTH_LONG).show();
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        if (getActivity() != null) {
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(getActivity(), task -> {
                        if (task.isSuccessful()) {
                            try {
                                //verification successful we will start the profile activity
                                if (task.getResult() != null && task.getResult().getUser() != null) {
//                                    phoneUserId = task.getResult().getUser().getUid();
//                                    userViewModel.setPhoneLoginUser(phoneUserId, userName, userPhoneNo, token);

                                    editProfileData();
                                }
                            } catch (Exception e1) {
                                // Error occurred while creating the File
                                Toast.makeText(getContext(),e1.getMessage(),Toast.LENGTH_LONG).show();
                            }
                        }

                    });
        }
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

//        if (checkToUpdateProfile()) {
//            updateUserProfile();
//        }

        userViewModel.getLoginUser().observe(this, listResource -> {
            // we don't need any null checks here for the adapter since LiveData guarantees that
            // it won't call us if fragment is stopped or not started.
            if (listResource != null && listResource.size() > 0) {
                Utils.psLog("Got Data");

                //fadeIn Animation
                fadeIn(binding.get().getRoot());

                userViewModel.user = listResource.get(0).user;

                Utils.psLog("Photo : " + listResource.get(0).user.userProfilePhoto);
            } else {
                //noinspection Constant Conditions
                Utils.psLog("Empty Data");

            }
        });

    }

        private void editProfileData() {

            updateUserProfile();

            userViewModel.getUpdateUserData().observe(this, listResource -> {

                if (listResource != null) {

                    Utils.psLog("Got Data" + listResource.message + listResource.toString());

                    switch (listResource.status) {
                        case LOADING:

                            break;
                        case SUCCESS:
                            // Success State
                            // Data are from Server

                            if (listResource.data != null) {

                                psDialogMsg.showSuccessDialog(listResource.data.message, getString(R.string.app__ok));
                                psDialogMsg.show();
                                psDialogMsg.okButton.setOnClickListener(view -> psDialogMsg.cancel());

                            }

                            psDialogMsg.showSuccessDialog(getString(R.string.profile_edit__success), getString(R.string.app__ok));
                            psDialogMsg.show();
                            psDialogMsg.okButton.setOnClickListener(view -> {
                                psDialogMsg.cancel();
                                prgDialog.get().cancel();

                                navigationController.navigateBackToEditPhoneFragment(VerifyMobileEditFragment.this.requireActivity(), userPhoneNo);

                                if(getActivity() != null) {
                                    VerifyMobileEditFragment.this.getActivity().finish();
                                }

                            });

                            userViewModel.setLoadingState(false);

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

    private void updateUserProfile() {

        if( userViewModel.user.userEmail.equals("")){
            userEmail = "default@gmail.com";
        } else {
            userEmail = userViewModel.user.userEmail;
        }

        User user = new User(userViewModel.user.userId,
                userViewModel.user.userIsSysAdmin,
                userViewModel.user.isCityAdmin,
                userViewModel.user.facebookId,
                userViewModel.user.phoneId,
                userViewModel.user.googleId,
                userViewModel.user.userName,
                userEmail,
                userPhoneNo,
                userViewModel.user.userAddress,
                userViewModel.user.city,
                userViewModel.user.userEmail,
                userViewModel.user.isShowPhone,
                userViewModel.user.isShowEmail,
                userViewModel.user.userAboutMe,
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
}
