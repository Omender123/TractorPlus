package com.prospective.tractorplus.ui.user.editphone;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.LoggingBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.prospective.tractorplus.MainActivity;
import com.prospective.tractorplus.R;
import com.prospective.tractorplus.binding.FragmentDataBindingComponent;
import com.prospective.tractorplus.databinding.FragmentEditPhoneBinding;
import com.prospective.tractorplus.ui.common.PSFragment;
import com.prospective.tractorplus.utils.AutoClearedValue;
import com.prospective.tractorplus.utils.Constants;
import com.prospective.tractorplus.utils.PSDialogMsg;
import com.prospective.tractorplus.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class EditPhoneFragment extends PSFragment {


    //region Variables

    private final androidx.databinding.DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);

    private PSDialogMsg psDialogMsg;

    @VisibleForTesting
    private AutoClearedValue<FragmentEditPhoneBinding> binding;

    public AutoClearedValue<ProgressDialog> prgDialog;

    private CallbackManager callbackManager;

    private String number, userName;

    //Firebase test
    private FirebaseAuth mAuth;

    private FirebaseAuth.AuthStateListener mAuthListener;

    //endregion


    //region Override Methods

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FacebookSdk.addLoggingBehavior(LoggingBehavior.REQUESTS);
        FacebookSdk.sdkInitialize(getContext());

        callbackManager = CallbackManager.Factory.create();

        // Inflate the layout for this fragment
        FragmentEditPhoneBinding dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_phone, container, false, dataBindingComponent);

        binding = new AutoClearedValue<>(this, dataBinding);

        return binding.get().getRoot();
    }


    //firebase
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.addAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void initUIAndActions() {

        dataBindingComponent.getFragmentBindingAdapters().bindFullImageDrawable(binding.get().bgImageView, getResources().getDrawable(R.drawable.login_app_bg));

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setToolbarText(((MainActivity) getActivity()).binding.toolbar, getString(R.string.login__login));
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            ((MainActivity) this.getActivity()).binding.toolbar.setBackgroundColor(getResources().getColor(R.color.global__primary));
            ((MainActivity) getActivity()).updateToolbarIconColor(Color.WHITE);
            ((MainActivity) getActivity()).updateMenuIconWhite();
            ((MainActivity) getActivity()).refreshPSCount();
        }

        psDialogMsg = new PSDialogMsg(getActivity(), false);

        // Init Dialog
        prgDialog = new AutoClearedValue<>(this, new ProgressDialog(getActivity()));

        prgDialog.get().setMessage((Utils.getSpannableString(getContext(), getString(R.string.message__please_wait), Utils.Fonts.MM_FONT)));
        prgDialog.get().setCancelable(false);

        //fadeIn Animation
        fadeIn(binding.get().getRoot());

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = firebaseAuth -> {

        };
        //end

        binding.get().addPhoneButton.setOnClickListener(v -> {
          if(binding.get().phoneEditText.getText().toString().trim().isEmpty()){
                psDialogMsg.showWarningDialog(getString(R.string.error_message__blank_phone_no), getString(R.string.app__ok));
                psDialogMsg.show();
            } else {
                prgDialog.get().show();

                number = binding.get().phoneEditText.getText().toString();
                EditPhoneFragment.this.validNo(number);

                prgDialog.get().cancel();

              navigationController.navigateToPhoneEditVerifyActivity(EditPhoneFragment.this.getActivity(),number);

              Toast.makeText(EditPhoneFragment.this.getActivity(), number, Toast.LENGTH_LONG).show();
            }
        });


    }

    @Override
    protected void initViewModels() {

    }

    @Override
    protected void initAdapters() {

    }

    @Override
    protected void initData() {

    }

    private void validNo(String no) {
        if (no.isEmpty() || no.length() < 10) {
            binding.get().phoneEditText.setError("Enter a valid mobile");
            binding.get().phoneEditText.requestFocus();
            prgDialog.get().cancel();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        callbackManager.onActivityResult(requestCode, resultCode, data);

            if (requestCode == Constants.REQUEST_CODE__EDIT_PHONE_VERIFY_ACTIVITY &&
                    resultCode == Constants.RESULT_CODE__EDIT_PHONE_ACTIITY) {

                if(getActivity() != null) {

                    number = data.getStringExtra(Constants.USER_PHONE);

                    navigationController.navigateBackToEditProfileFragment(EditPhoneFragment.this.requireActivity(), number);

                    getActivity().finish();

            }
        }

    }

}
//endregion