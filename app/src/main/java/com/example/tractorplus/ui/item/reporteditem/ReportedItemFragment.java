package com.example.tractorplus.ui.item.reporteditem;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tractorplus.Config;
import com.example.tractorplus.MainActivity;
import com.example.tractorplus.R;
import com.example.tractorplus.binding.FragmentDataBindingComponent;
import com.example.tractorplus.databinding.FragmentReportedItemBinding;
import com.example.tractorplus.ui.common.DataBoundListAdapter;
import com.example.tractorplus.ui.common.PSFragment;
import com.example.tractorplus.ui.item.reporteditem.adapter.ReportedItemAdapter;
import com.example.tractorplus.utils.AutoClearedValue;
import com.example.tractorplus.utils.Utils;
import com.example.tractorplus.viewmodel.reporteditem.ReportedItemViewModel;
import com.example.tractorplus.viewobject.ReportedItem;
import com.example.tractorplus.viewobject.common.Status;

import java.util.List;

public class ReportedItemFragment  extends PSFragment implements DataBoundListAdapter.DiffUtilDispatchedInterface {

    private final androidx.databinding.DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);

    private ReportedItemViewModel reportedItemViewModel;

    @VisibleForTesting
    private AutoClearedValue<FragmentReportedItemBinding> binding;
    private AutoClearedValue<ReportedItemAdapter> reportedItemAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentReportedItemBinding dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_reported_item, container, false, dataBindingComponent);
        binding = new AutoClearedValue<>(this, dataBinding);

        return binding.get().getRoot();
    }


    @Override
    public void onDispatched() {
        if (reportedItemViewModel.loadingDirection == Utils.LoadingDirection.bottom) {

            if (binding.get().reportedItemListRecyclerView != null) {

                LinearLayoutManager layoutManager = (LinearLayoutManager)
                        binding.get().reportedItemListRecyclerView.getLayoutManager();

                if (layoutManager != null) {
                    layoutManager.scrollToPosition(0);
                }
            }
        }
    }

    @Override
    protected void initUIAndActions() {

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setToolbarText(((MainActivity) getActivity()).binding.toolbar, getString(R.string.menu__report_item));
            ((MainActivity) this.getActivity()).binding.toolbar.setBackgroundColor(getResources().getColor(R.color.global__primary));
            ((MainActivity) getActivity()).updateMenuIconWhite();
            ((MainActivity) getActivity()).updateToolbarIconColor(Color.WHITE);
            ((MainActivity) getActivity()).refreshPSCount();
        }



        binding.get().reportedItemListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager)
                        recyclerView.getLayoutManager();

                if (layoutManager != null) {

                    int lastPosition = layoutManager
                            .findLastVisibleItemPosition();
                    if (lastPosition == reportedItemAdapter.get().getItemCount() - 1) {

                        if (!binding.get().getLoadingMore() && !reportedItemViewModel.forceEndLoading) {

                            reportedItemViewModel.loadingDirection = Utils.LoadingDirection.bottom;

                            int limit = Config.REPORTED_ITEM_COUNT;
                            reportedItemViewModel.offset = reportedItemViewModel.offset + limit;
                            reportedItemViewModel.setLoadingState(true);
                            reportedItemViewModel.setNextPagereportedObj(String.valueOf(Config.REPORTED_ITEM_COUNT),
                                    String.valueOf(reportedItemViewModel.offset),loginUserId);
                        }
                    }
                }
            }
        });

        binding.get().swipeRefresh.setColorSchemeColors(getResources().getColor(R.color.view__primary_line));
        binding.get().swipeRefresh.setProgressBackgroundColorSchemeColor(getResources().getColor(R.color.global__primary));
        binding.get().swipeRefresh.setOnRefreshListener(() -> {

            reportedItemViewModel.loadingDirection = Utils.LoadingDirection.top;

            // reset productViewModel.offset

            reportedItemViewModel.offset = 0;

            // reset productViewModel.forceEndLoading
            reportedItemViewModel.forceEndLoading = false;

            reportedItemViewModel.setReportedObj(String.valueOf(Config.REPORTED_ITEM_COUNT), String.valueOf(reportedItemViewModel.offset),loginUserId);

            // update live data

        });
    }

    @Override
    protected void initViewModels() {
        reportedItemViewModel = new ViewModelProvider(this,viewModelFactory).get(ReportedItemViewModel.class);

    }

    @Override
    protected void initAdapters() {
        ReportedItemAdapter reportedItemAdapter = new ReportedItemAdapter(dataBindingComponent,
                reportedItem -> navigationController.navigateToItemDetailFromHistoryListOnly(ReportedItemFragment.this.getActivity(), reportedItem.id, reportedItem.title));
        this.reportedItemAdapter = new AutoClearedValue<>(this, reportedItemAdapter);
        binding.get().reportedItemListRecyclerView.setAdapter(reportedItemAdapter);

    }

    @Override
    protected void initData() {
        reportedItemViewModel.setReportedObj(String.valueOf(Config.REPORTED_ITEM_COUNT),String.valueOf(reportedItemViewModel.offset),loginUserId);
        reportedItemViewModel.getReportedData().observe(this, result -> {

            if (result != null) {
                switch (result.status) {
                    case SUCCESS:
                        replaceItemReportedData(result.data);
                        reportedItemViewModel.setLoadingState(false);
                        break;

                    case LOADING:
                        replaceItemReportedData(result.data);
                        break;

                    case ERROR:

                        reportedItemViewModel.setLoadingState(false);
                        break;
                }
            }

        });

        reportedItemViewModel.getNextPagereportedData().observe(this, state -> {
            if (state != null) {
                if (state.status == Status.ERROR) {

                    reportedItemViewModel.setLoadingState(false);
                    reportedItemViewModel.forceEndLoading = true;
                }
            }
        });


        reportedItemViewModel.getLoadingState().observe(this, loadingState -> {

            binding.get().setLoadingMore(reportedItemViewModel.isLoading);

            if (loadingState != null && !loadingState) {
                binding.get().swipeRefresh.setRefreshing(false);
            }

        });

    }

    private void replaceItemReportedData(List<ReportedItem> reportedItems) {
        reportedItemAdapter.get().replace(reportedItems);
        binding.get().executePendingBindings();

    }
}
