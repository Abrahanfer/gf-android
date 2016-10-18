package me.abrahanfer.geniusfeed;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import me.abrahanfer.geniusfeed.models.DRResponseModels.FeedDRResponse;
import me.abrahanfer.geniusfeed.models.Feed;
import me.abrahanfer.geniusfeed.models.FeedItemRead;
import me.abrahanfer.geniusfeed.utils.Authentication;
import me.abrahanfer.geniusfeed.utils.FeedArrayAdapter;
import me.abrahanfer.geniusfeed.utils.RecommendedFeedsArrayAdapter;
import me.abrahanfer.geniusfeed.utils.network.GeniusFeedService;
import me.abrahanfer.geniusfeed.utils.network.NetworkServiceBuilder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by abrahan on 17/09/16.
 */

public class RecommendedFeedsFragment extends Fragment implements SearchView.OnQueryTextListener {
    private View mBaseView;
    private Activity mActivity;

    private Unbinder unbinder;

    private ArrayList<Feed> recommendFeeds;

    @BindView(R.id.pbLoading) ProgressBar pbLoading;
    @BindView(R.id.recommended_feeds_list) RecyclerView recommendFeedsList;
    @BindView(R.id.recommended_feeds_swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBaseView = inflater.inflate(R.layout.recommended_feeds_fragment,
                                     container, false);

        unbinder = ButterKnife.bind(this, mBaseView);

        recommendFeedsList.setHasFixedSize(true);

        // Add Swipe gesture to items
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                                                   ItemTouchHelper.RIGHT) {
                    public boolean onMove(RecyclerView recyclerView,
                                          RecyclerView.ViewHolder viewHolder,
                                          RecyclerView.ViewHolder target) {

                        return false;
                    }

                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                        // remove from Recycler view
                        recommendFeeds.remove(viewHolder.getAdapterPosition());
                        recommendFeedsList.getAdapter().notifyItemRemoved(viewHolder.getAdapterPosition());
                    }

                    @Override
                    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                            // Get RecyclerView item from the ViewHolder
                            View itemView = viewHolder.itemView;

                            Paint p = new Paint();
                            Bitmap trashIcon;
                            float height = (float) itemView.getBottom() - (float) itemView.getTop();
                            float width = height / 3;

                            if (dX > 0) {
                                /* Set your color for positive displacement */
                                trashIcon = BitmapFactory.decodeResource(
                                        mActivity.getBaseContext().getResources(), R.drawable
                                                .ic_delete_black_36dp);

                                p.setColor(Color.parseColor("#f44336")); // App RED
                                // Draw Rect with varying right side, equal to displacement dX
                                c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX,
                                           (float) itemView.getBottom(), p);

                                // Set the image icon for swipe background
                                RectF icon_dest = new RectF((float) itemView.getLeft() + width , (float) itemView.getTop() + width, (float) itemView.getLeft()+ 2*width, (float)itemView.getBottom() - width);
                                ColorFilter whiteFilter = new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                                p.setColorFilter(whiteFilter);
                                c.drawBitmap(trashIcon,null,icon_dest,p);
                            }

                            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                        }
                    }
                });
        itemTouchHelper.attachToRecyclerView(recommendFeedsList);

        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(mBaseView.getContext());
        recommendFeedsList.setLayoutManager(layoutManager);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                recommendFeedsList.setVisibility(RecyclerView.INVISIBLE);
                getRecommendedFeedsFromAPI();
            }
        });

        swipeRefreshLayout.setColorSchemeResources(R.color.color_primary,R.color.color_accent);

        return mBaseView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = getActivity();
        getRecommendedFeedsFromAPI();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public boolean onQueryTextChange(String query) {
        ArrayList<Feed> newFeedList = filterByQuery(query);

        RecommendedFeedsArrayAdapter newAdapter = new RecommendedFeedsArrayAdapter(newFeedList);
        recommendFeedsList.setAdapter(newAdapter);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    private ArrayList<Feed> filterByQuery(String query) {

        ArrayList<Feed> newFeedList = new ArrayList<>();

        if (query.length() > 0) {
            for (Feed feed : recommendFeeds) {
                if (feed.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    newFeedList.add(feed);
                }
            }
        } else {
           newFeedList = recommendFeeds;
        }

        return newFeedList;
    }

    private void showProgressBar() {
        pbLoading.setVisibility(ProgressBar.VISIBLE);
        recommendFeedsList.setVisibility(RecyclerView.INVISIBLE);
    }

    private void hideProgressBar() {
        pbLoading.setVisibility(ProgressBar.INVISIBLE);
        recommendFeedsList.setVisibility(RecyclerView.VISIBLE);
    }

    private void stopRefreshing() {
        // Stopping progress bar from swipe
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }
    private void getRecommendedFeedsFromAPI() {
        // show visible ProgressBar
        showProgressBar();
        String username;
        String token;

        // TODO Fix navigation flow with credentials and login
        Authentication authentication = Authentication.getCredentials();
        if (authentication == null) {
            Intent intent = new Intent(mActivity.getApplicationContext(), LoginActivity.class);
            stopRefreshing();
            startActivity(intent);

            return ;
        } else {
            username = authentication.getUsername();
            token = authentication.getToken();
        }

        GeniusFeedService service = NetworkServiceBuilder.createService(GeniusFeedService.class, token);

        // Get all feeds or get only timeframe feeds
        Call<FeedDRResponse> call = service.getRecommendedFeeds(1);


        call.enqueue(new Callback<FeedDRResponse>() {
            @Override
            public void onResponse(Call<FeedDRResponse> call, Response<FeedDRResponse> response) {
                if (response.isSuccessful()) {
                    ArrayList<Feed> feedArrayList = new ArrayList<>(
                            response.body().getResults());

                    recommendFeeds = feedArrayList;
                    RecommendedFeedsArrayAdapter feedArrayAdapter = new RecommendedFeedsArrayAdapter(recommendFeeds);
                    feedArrayAdapter.setActivity(mActivity);
                    recommendFeedsList.setAdapter(feedArrayAdapter);
                    stopRefreshing();
                    hideProgressBar();
                } else {
                    // error response, no access to resource?
                    stopRefreshing();

                    // Check error status code
                    if (response.code() == 403) {
                        Intent intent = new Intent(mActivity.getApplicationContext(), LoginActivity.class);
                        startActivity(intent);

                        return;
                    } else {
                        // Feedback error
                        MainActivity mainActivity = (MainActivity) mActivity;
                        mainActivity.showAlertMessages(3);
                    }
                }
            }

            @Override
            public void onFailure(Call<FeedDRResponse> call, Throwable t) {
                // something went completely south (like no internet connection)
                Log.e("Error GetFeeds RETROFIT", t.getMessage());
                stopRefreshing();
                // Feedback error
                MainActivity mainActivity = (MainActivity) mActivity;
                mainActivity.showAlertMessages(3);
            }
        });
    }
}
