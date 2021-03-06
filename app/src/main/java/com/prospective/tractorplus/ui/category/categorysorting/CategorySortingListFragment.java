package com.prospective.tractorplus.ui.category.categorysorting;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.prospective.tractorplus.Config;
import com.prospective.tractorplus.MainActivity;
import com.prospective.tractorplus.R;
import com.prospective.tractorplus.binding.FragmentDataBindingComponent;
import com.prospective.tractorplus.databinding.FragmentCategoryListBinding;
import com.prospective.tractorplus.ui.category.adapter.CategoryAdapter;
import com.prospective.tractorplus.ui.common.DataBoundListAdapter;
import com.prospective.tractorplus.ui.common.PSFragment;
import com.prospective.tractorplus.utils.AutoClearedValue;
import com.prospective.tractorplus.utils.Constants;
import com.prospective.tractorplus.utils.PSDialogMsg;
import com.prospective.tractorplus.utils.Utils;
import com.prospective.tractorplus.viewmodel.item.TouchCountViewModel;
import com.prospective.tractorplus.viewmodel.itemcategory.ItemCategoryViewModel;
import com.prospective.tractorplus.viewobject.ItemCategory;
import com.prospective.tractorplus.viewobject.common.Resource;
import com.prospective.tractorplus.viewobject.common.Status;
import com.prospective.tractorplus.viewobject.holder.ItemParameterHolder;

import java.util.List;

public class CategorySortingListFragment extends PSFragment implements DataBoundListAdapter.DiffUtilDispatchedInterface {

    //region Variables

    private final androidx.databinding.DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);

    private ItemCategoryViewModel itemCategoryViewModel;
    private TouchCountViewModel touchCountViewModel;
    private ItemParameterHolder itemParameterHolder = new ItemParameterHolder();

    @VisibleForTesting
    private AutoClearedValue<FragmentCategoryListBinding> binding;
    private AutoClearedValue<CategoryAdapter> adapter;
    private PSDialogMsg psDialogMsg;

    //endregion


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentCategoryListBinding dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_category_list, container, false, dataBindingComponent);

        binding = new AutoClearedValue<>(this, dataBinding);

        binding.get().setLoadingMore(connectivity.isConnected());

        setHasOptionsMenu(true);

        return binding.get().getRoot();
    }

    @Override
    protected void initUIAndActions() {

        psDialogMsg = new PSDialogMsg(getActivity(), false);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setToolbarText(((MainActivity) getActivity()).binding.toolbar, getString(R.string.category__list_title));
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

        binding.get().categoryList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                GridLayoutManager layoutManager = (GridLayoutManager)
                        recyclerView.getLayoutManager();

                if (layoutManager != null) {
                    int lastPosition = layoutManager
                            .findLastVisibleItemPosition();
                    if (lastPosition == adapter.get().getItemCount() - 1) {

                        if (!binding.get().getLoadingMore() && !itemCategoryViewModel.forceEndLoading) {

                            if (connectivity.isConnected()) {

                                itemCategoryViewModel.loadingDirection = Utils.LoadingDirection.bottom;

                                int limit = Config.LIST_CATEGORY_COUNT;
                                itemCategoryViewModel.offset = itemCategoryViewModel.offset + limit;

                                itemCategoryViewModel.setNextPageLoadingStateObj(Utils.checkUserId(loginUserId), String.valueOf(Config.LIST_CATEGORY_COUNT),
                                        String.valueOf(itemCategoryViewModel.offset), itemCategoryViewModel.categoryParameterHolder);//itemCategoryViewModel.categoryParameterHolder.cityId);
                            }
                        }
                    }
                }
            }
        });

        binding.get().swipeRefresh.setColorSchemeColors(getResources().getColor(R.color.view__primary_line));
        binding.get().swipeRefresh.setProgressBackgroundColorSchemeColor(getResources().getColor(R.color.global__primary));
        binding.get().swipeRefresh.setOnRefreshListener(() -> {

            itemCategoryViewModel.loadingDirection = Utils.LoadingDirection.top;

            // reset itemCategoryViewModel.offset
            itemCategoryViewModel.offset = 0;

            // reset itemCategoryViewModel.forceEndLoading
            itemCategoryViewModel.forceEndLoading = false;

            // update live data
            itemCategoryViewModel.setCategoryListObj(Utils.checkUserId(loginUserId), String.valueOf(Config.LIST_CATEGORY_COUNT), String.valueOf(itemCategoryViewModel.offset), itemCategoryViewModel.categoryParameterHolder);

        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.clear_button, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.clear) {

            if (this.getActivity() != null) {

                psDialogMsg.showSubCategoryFilterDialog(getString(R.string.item_filter__highest_to_lowest_letter), getString(R.string.item_filter__lowest_to_highest_letter), getString(R.string.app__cancel));

                psDialogMsg.show();

                psDialogMsg.ascButton.setOnClickListener(v -> {

                    psDialogMsg.cancel();

                    itemCategoryViewModel.categoryParameterHolder.order_by = Constants.FILTERING_CAT_NAME;
                    itemCategoryViewModel.categoryParameterHolder.order_type = Constants.FILTERING_ASC;

                    itemCategoryViewModel.setCategoryListObj(Utils.checkUserId(loginUserId), String.valueOf(Config.LIST_CATEGORY_COUNT), String.valueOf(itemCategoryViewModel.offset), itemCategoryViewModel.categoryParameterHolder);

                });

                psDialogMsg.descButton.setOnClickListener(v -> {

                    psDialogMsg.cancel();

                    itemCategoryViewModel.categoryParameterHolder.order_by = Constants.FILTERING_CAT_NAME;
                    itemCategoryViewModel.categoryParameterHolder.order_type = Constants.FILTERING_DESC;

                    itemCategoryViewModel.setCategoryListObj(Utils.checkUserId(loginUserId), String.valueOf(Config.LIST_CATEGORY_COUNT), String.valueOf(itemCategoryViewModel.offset), itemCategoryViewModel.categoryParameterHolder);

                });

                psDialogMsg.cancelButton.setOnClickListener(view -> psDialogMsg.cancel());

            }

        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void initViewModels() {
        itemCategoryViewModel = new ViewModelProvider(this, viewModelFactory).get(ItemCategoryViewModel.class);
        touchCountViewModel = new ViewModelProvider(this, viewModelFactory).get(TouchCountViewModel.class);
    }

    @Override
    protected void initAdapters() {
        CategoryAdapter nvAdapter = new CategoryAdapter(dataBindingComponent,
                category -> {

                    itemParameterHolder.cat_id = category.id;
                    itemParameterHolder.isPaid = Constants.PAIDITEMFIRST;
                    itemParameterHolder.location_id = selected_location_id;
                    itemParameterHolder.location_township_id = selected_township_id;
                    itemParameterHolder.lat = selectedLat;
                    itemParameterHolder.lng = selectedLng;

                    if (Config.SHOW_SUBCATEGORY) {
                        navigationController.navigateToSubCategoryActivity(CategorySortingListFragment.this.getActivity(), category.id, category.name);
                    }
                    else {
                        navigationController.navigateToHomeFilteringActivity(CategorySortingListFragment.this.getActivity(), itemParameterHolder, category.name, selectedCityLat, selectedCityLng, Constants.MAP_MILES );
                    }
                }, this);

        this.adapter = new AutoClearedValue<>(this, nvAdapter);
        binding.get().categoryList.setAdapter(nvAdapter);
    }

    @Override
    protected void initData() {
        loadCategory();
    }

    //region Private Methods

    private void loadCategory() {

        // Load Category List

        itemCategoryViewModel.categoryParameterHolder.cityId = selectedCityId;

        itemCategoryViewModel.setCategoryListObj(Utils.checkUserId(loginUserId), String.valueOf(Config.LIST_CATEGORY_COUNT), String.valueOf(itemCategoryViewModel.offset), itemCategoryViewModel.categoryParameterHolder);

        LiveData<Resource<List<ItemCategory>>> news = itemCategoryViewModel.getCategoryListData();

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

                            itemCategoryViewModel.setLoadingState(false);


                            break;

                        case ERROR:
                            // Error State

                            itemCategoryViewModel.setLoadingState(false);

                            break;
                        default:
                            // Default

                            break;
                    }

                } else {

                    // Init Object or Empty Data

                    if (itemCategoryViewModel.offset > 1) {
                        // No more data for this list
                        // So, Block all future loading
                        itemCategoryViewModel.forceEndLoading = true;
                    }

                }

            });
        }

        itemCategoryViewModel.getNextPageLoadingStateData().observe(this, state -> {
            if (state != null) {
                if (state.status == Status.ERROR) {

                    itemCategoryViewModel.setLoadingState(false);
                    itemCategoryViewModel.forceEndLoading = true;
                }
            }
        });

        itemCategoryViewModel.getLoadingState().observe(this, loadingState -> {

            binding.get().setLoadingMore(itemCategoryViewModel.isLoading);

            if (loadingState != null && !loadingState) {
                binding.get().swipeRefresh.setRefreshing(false);
            }

        });

        //get touch count post method
        touchCountViewModel.getTouchCountPostData().observe(this, result -> {
            if (result != null) {
                if (result.status == Status.SUCCESS) {
                    if (CategorySortingListFragment.this.getActivity() != null) {
                        Utils.psLog(result.status.toString());
                    }

                } else if (result.status == Status.ERROR) {
                    if (CategorySortingListFragment.this.getActivity() != null) {
                        Utils.psLog(result.status.toString());
                    }
                }
            }
        });

    }

    private void replaceData(List<ItemCategory> categoryList) {

        adapter.get().replace(categoryList);
        binding.get().executePendingBindings();

    }


    @Override
    public void onDispatched() {
        if (itemCategoryViewModel.loadingDirection == Utils.LoadingDirection.top) {

            if (binding.get().categoryList != null) {

                LinearLayoutManager layoutManager = (LinearLayoutManager)
                        binding.get().categoryList.getLayoutManager();

                if (layoutManager != null) {
                    layoutManager.scrollToPosition(0);
                }
            }
        }
    }
}
