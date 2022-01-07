package com.example.tractorplus.ui.item.featured;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.example.tractorplus.Config;
import com.example.tractorplus.MainActivity;
import com.example.tractorplus.R;
import com.example.tractorplus.binding.FragmentDataBindingComponent;
import com.example.tractorplus.databinding.FragmentFeaturedListBinding;
import com.example.tractorplus.ui.common.DataBoundListAdapter;
import com.example.tractorplus.ui.common.PSFragment;
import com.example.tractorplus.ui.item.adapter.ItemVerticalListAdapter;
import com.example.tractorplus.utils.AutoClearedValue;
import com.example.tractorplus.utils.Constants;
import com.example.tractorplus.utils.Utils;
import com.example.tractorplus.viewmodel.item.FeaturedItemViewModel;
import com.example.tractorplus.viewobject.Item;
import com.example.tractorplus.viewobject.common.Resource;
import com.example.tractorplus.viewobject.common.Status;

import java.util.List;

public class FeaturedListFragment extends PSFragment implements DataBoundListAdapter.DiffUtilDispatchedInterface  {

    private final androidx.databinding.DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);
    private FeaturedItemViewModel featuredItemViewModel;

    @VisibleForTesting
    private AutoClearedValue<FragmentFeaturedListBinding> binding;
    private AutoClearedValue<ItemVerticalListAdapter> adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentFeaturedListBinding dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_featured_list, container, false, dataBindingComponent);
        binding = new AutoClearedValue<>(this, dataBinding);

        binding.get().setLoadingMore(connectivity.isConnected());

        return binding.get().getRoot();

    }

    @Override
    public void onDispatched() {

        if (featuredItemViewModel.loadingDirection == Utils.LoadingDirection.top) {

            if (binding.get().featuredList != null) {

                GridLayoutManager layoutManager = (GridLayoutManager)
                        binding.get().featuredList.getLayoutManager();

                if (layoutManager != null) {
                    layoutManager.scrollToPosition(0);
                }
            }
        }

    }

    @Override
    protected void initUIAndActions() {

        if (getActivity() instanceof MainActivity) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            ((MainActivity) this.getActivity()).binding.toolbar.setBackgroundColor(getResources().getColor(R.color.global__primary));
            ((MainActivity) getActivity()).updateToolbarIconColor(Color.WHITE);
            ((MainActivity) getActivity()).updateMenuIconWhite();
            ((MainActivity) getActivity()).refreshPSCount();
        }

        if (Config.SHOW_ADMOB && connectivity.isConnected()) {
            AdRequest adRequest = new AdRequest.Builder()
                    .build();
            binding.get().adView.loadAd(adRequest);
        } else {
            binding.get().adView.setVisibility(View.GONE);
        }

        binding.get().featuredList.setNestedScrollingEnabled(false);
        binding.get().featuredList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                GridLayoutManager layoutManager = (GridLayoutManager)
                        recyclerView.getLayoutManager();

                if (layoutManager != null) {

                    int lastPosition = layoutManager
                            .findLastVisibleItemPosition();
                    if (lastPosition == adapter.get().getItemCount() - 1) {

                        if (!binding.get().getLoadingMore() && !featuredItemViewModel.forceEndLoading) {

                            if (connectivity.isConnected()) {

                                featuredItemViewModel.loadingDirection = Utils.LoadingDirection.bottom;

                                int limit = Config.ITEM_COUNT;
                                featuredItemViewModel.offset = featuredItemViewModel.offset + limit;

                                featuredItemViewModel.setNextPageFeaturedItemListByKeyObj(Config.LIMIT_FROM_DB_COUNT, String.valueOf(featuredItemViewModel.offset), loginUserId, featuredItemViewModel.featuredItemParameterHolder);
                            }
                        }
                    }
                }
            }
        });

        binding.get().swipeRefresh.setColorSchemeColors(getResources().getColor(R.color.view__primary_line));
        binding.get().swipeRefresh.setProgressBackgroundColorSchemeColor(getResources().getColor(R.color.global__primary));
        binding.get().swipeRefresh.setOnRefreshListener(() -> {

            featuredItemViewModel.loadingDirection = Utils.LoadingDirection.top;

            // reset productViewModel.offset
            featuredItemViewModel.offset = 0;

            // reset productViewModel.forceEndLoading
            featuredItemViewModel.forceEndLoading = false;

            // update live data
            featuredItemViewModel.featuredItemParameterHolder.location_id = selected_location_id;
            featuredItemViewModel.featuredItemParameterHolder.location_township_id = selected_township_id;
            featuredItemViewModel.setFeaturedItemListByKeyObj(loginUserId, Config.LIMIT_FROM_DB_COUNT, String.valueOf(featuredItemViewModel.offset), featuredItemViewModel.featuredItemParameterHolder);

        });
    }

    @Override
    protected void initViewModels() {

        featuredItemViewModel = new ViewModelProvider(this, viewModelFactory).get(FeaturedItemViewModel.class);

    }

    @Override
    protected void initAdapters() {

        ItemVerticalListAdapter nvAdapter = new ItemVerticalListAdapter(dataBindingComponent, item -> navigationController.navigateToItemDetailActivity(getActivity(), item.id), this);

        this.adapter = new AutoClearedValue<>(this, nvAdapter);
        binding.get().featuredList.setAdapter(nvAdapter);

    }

    @Override
    protected void initData() {

        featuredItemViewModel.getNextPageFeaturedItemListByKeyData().observe(this, state -> {
            if (state != null) {
                if (state.status == Status.ERROR) {

                    featuredItemViewModel.setLoadingState(false);//hide
                    featuredItemViewModel.forceEndLoading = true;//stop

                }
            }
        });

        featuredItemViewModel.getLoadingState().observe(this, loadingState -> {

            binding.get().setLoadingMore(featuredItemViewModel.isLoading);

            if (loadingState != null && !loadingState) {
                binding.get().swipeRefresh.setRefreshing(false);
            }

        });

        featuredItemViewModel.featuredItemParameterHolder.location_id = selected_location_id;
        featuredItemViewModel.featuredItemParameterHolder.location_township_id = selected_township_id;

        featuredItemViewModel.setFeaturedItemListByKeyObj(loginUserId, Config.LIMIT_FROM_DB_COUNT, Constants.ZERO, featuredItemViewModel.featuredItemParameterHolder);

        LiveData<Resource<List<Item>>> news = featuredItemViewModel.getFeaturedItemListByKeyData();

        if (news != null) {

            news.observe(this, listResource -> {
                if (listResource != null) {

                    switch (listResource.status) {
                        case LOADING:
                            // Loading State
                            // Data are from Local DB

                            if (listResource.data != null) {
                                //fadeIn Animation
                                fadeIn(binding.get().getRoot());

                                // Update the data
                                replaceData(listResource.data);

                            }

                            break;

                        case SUCCESS:
                            // Success State
                            // Data are from Server

                            if (listResource.data != null) {
                                // Update the data
                                replaceData(listResource.data);
                            }

                            featuredItemViewModel.setLoadingState(false);

                            break;

                        case ERROR:
                            // Error State

                            featuredItemViewModel.setLoadingState(false);
                            featuredItemViewModel.forceEndLoading = true;

                            break;
                        default:
                            // Default

                            break;
                    }

                } else {

                    // Init Object or Empty Data

                    if (featuredItemViewModel.offset > 1) {
                        // No more data for this list
                        // So, Block all future loading
                        featuredItemViewModel.forceEndLoading = true;
                    }

                }

            });
        }
    }

    private void replaceData(List<Item> featuredList) {

        adapter.get().replace(featuredList);
        binding.get().executePendingBindings();

    }

    @Override
    public void onResume() {
        super.onResume();

        loadLoginUserId();
    }
}
