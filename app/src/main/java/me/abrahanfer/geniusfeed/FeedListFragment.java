package me.abrahanfer.geniusfeed;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import me.abrahanfer.geniusfeed.models.Category;
import me.abrahanfer.geniusfeed.models.DRResponseModels.FeedDRResponse;
import me.abrahanfer.geniusfeed.models.DRResponseModels.FeedItemReadDRResponse;
import me.abrahanfer.geniusfeed.models.Feed;
import me.abrahanfer.geniusfeed.models.FeedItemRead;
import me.abrahanfer.geniusfeed.utils.Authentication;
import me.abrahanfer.geniusfeed.utils.Constants;
import me.abrahanfer.geniusfeed.utils.DividerItemDecoration;
import me.abrahanfer.geniusfeed.utils.FeedArrayAdapter;
import me.abrahanfer.geniusfeed.utils.GeniusFeedContract;
import me.abrahanfer.geniusfeed.utils.network.GeniusFeedService;
import me.abrahanfer.geniusfeed.utils.network.NetworkServiceBuilder;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static me.abrahanfer.geniusfeed.R.attr.icon;


interface FeedListUpdater {
    void updateFeedData(Feed newFeed);
}

/**
 * Created by abrahan on 2/04/16.
 */
public class FeedListFragment extends Fragment implements FeedListUpdater, SearchView.OnQueryTextListener {
    public final static String FEED = "me.abrahanfer.geniusfeed" +
            ".FEED";
    public final static String LOGIN_CREDENTIALS =
            "me.abrahanfer.geniusfeed" + ".LOGIN_CREDENTIALS";

    public final static String FEED_LINK = "FEED_LINK";
    public final static String FEED_PK = "FEED_PK";
    public final static String FEED_API = "FEED_API";

    private Boolean timeframeFeeds = false;

    private View mBaseView;
    private Activity mActivity;
    private ProgressBar mProgressBar;
    private RecyclerView mFeedListView;
    private Map<String, List<FeedItemRead>> mFeedItemsReadByFeed;

    private Unbinder unbinder;

    private ArrayList<Feed> mFeedList = new ArrayList<>();

    @BindView(R.id.feed_list_swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;

    public Boolean getTimeframeFeeds() {
        return timeframeFeeds;
    }

    public void setTimeframeFeeds(Boolean timeframeFeeds) {
        this.timeframeFeeds = timeframeFeeds;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBaseView = inflater.inflate(R.layout.feed_list_fragment,
                                container, false);

        unbinder = ButterKnife.bind(this, mBaseView);

        FloatingActionButton floatingActionButton = (FloatingActionButton) mBaseView.findViewById(R.id.add_feed_button);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        mFeedListView = (RecyclerView) mBaseView.findViewById(R.id.feeds_list);
        mFeedListView.setHasFixedSize(true);

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
                        // remove from API
                        deleteFeed(viewHolder.getAdapterPosition());
                        // remove from adapter
                        mFeedList.remove(viewHolder.getAdapterPosition());
                        mFeedListView.getAdapter().notifyItemRemoved(viewHolder.getAdapterPosition());
                        //mFeedListView.getAdapter().notifyDataSetChanged();
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
        itemTouchHelper.attachToRecyclerView(mFeedListView);

        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(mBaseView.getContext());
        mFeedListView.setLayoutManager(layoutManager);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mFeedListView.setVisibility(RecyclerView.INVISIBLE);
                getFeedFromAPI();
            }
        });

        swipeRefreshLayout.setColorSchemeResources(R.color.color_primary,R.color.color_accent);

        return mBaseView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = getActivity();
        mProgressBar = (ProgressBar) mActivity.findViewById(R.id.pbLoading);
        setupListFeeds();
        setupAuthenticationFromDB();
        //testRequest();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public boolean onQueryTextChange(String query) {
        // Add logic to filter feeds by titles and categories
        ArrayList<Feed> newFeedList;
        Map<String, List<FeedItemRead>> newFeedItemsReadByFeed;
        if (query.length() > 0) {
            newFeedList = filterByQuery(query);
            newFeedItemsReadByFeed = filterByNewFeeds(newFeedList);
        } else {
            newFeedList = mFeedList;
            newFeedItemsReadByFeed = mFeedItemsReadByFeed;
        }
        FeedArrayAdapter newAdapter = new FeedArrayAdapter(newFeedList, newFeedItemsReadByFeed);
        mFeedListView.setAdapter(newAdapter);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    private ArrayList<Feed> filterByQuery(String query) {
        ArrayList<Feed> newFeedList = new ArrayList<>();
        for(Feed feed : mFeedList) {
            if (feed.getTitle().toLowerCase().contains(query.toLowerCase())) {
                newFeedList.add(feed);
            }
        }

        return newFeedList;
    }

    private Map<String, List<FeedItemRead>> filterByNewFeeds(ArrayList<Feed> feeds) {
        Map<String, List<FeedItemRead>> newFeedItemsRead = new HashMap<String, List<FeedItemRead>>();
        for(Feed feed : feeds) {
            newFeedItemsRead.put(feed.getPk(), mFeedItemsReadByFeed.get(feed.getPk()));
        }

        return newFeedItemsRead;
    }

    public void getFeedFromAPI() {
        // ProgressBar
        showProgressBar();

        String username;
        String token;


        Authentication authentication = Authentication.getCredentials();
        if (authentication == null) {
            Intent intent = new Intent(mActivity.getApplicationContext(), LoginActivity.class);
            startActivity(intent);

            return ;
        } else {
            username = authentication.getUsername();
            token = authentication.getToken();
        }

        GeniusFeedService service = NetworkServiceBuilder.createService(GeniusFeedService.class, token);

        // Get all feeds or get only timeframe feeds
        Call<FeedDRResponse> call;

        if (timeframeFeeds)
            call = service.getFeedsBytTimeframe(1);
        else
            call = service.getFeeds(1);

        call.enqueue(new Callback<FeedDRResponse>() {
            @Override
            public void onResponse(Call<FeedDRResponse> call, Response<FeedDRResponse> response) {
                if (response.isSuccessful()) {
                    ArrayList<Feed> feedArrayList = new ArrayList<>(
                            response.body().getResults());

                    mFeedList = feedArrayList;
                    if (response.body().getNext() != null){
                        getFeedFromAPIPagination(2);
                    }else{
                        downloadAllFeedData(mFeedList);
                    }

                } else {
                    // error response, no access to resource?
                    Log.e("ERROR FEED LIST", "error in response");

                    // Check error status code
                    if (response.code() == 403) {
                        Intent intent = new Intent(mActivity.getApplicationContext(), LoginActivity.class);
                        startActivity(intent);

                        return;
                    }
                }
            }

            @Override
            public void onFailure(Call<FeedDRResponse> call, Throwable t) {
                // something went completely south (like no internet connection)
                Log.e("Error GetFeeds RETROFIT", t.getMessage());
            }
        });
    }

    public void getFeedFromAPIPagination(final int page) {
        String token = Authentication.getCredentials().getToken();
        GeniusFeedService service = NetworkServiceBuilder.createService(GeniusFeedService.class, token);
        Call<FeedDRResponse> call;
        if(timeframeFeeds)
            call = service.getFeedsBytTimeframe(page);
        else
            call = service.getFeeds(page);

        call.enqueue(new Callback<FeedDRResponse>() {
            @Override
            public void onResponse(Call<FeedDRResponse> call, Response<FeedDRResponse> response) {
                if (response.isSuccessful()) {
                    ArrayList<Feed> feedArrayList = new ArrayList<>(
                            response.body().getResults());
                    mFeedList.addAll(feedArrayList);

                    if (response.body().getNext() != null) {
                        getFeedFromAPIPagination(page + 1);
                    } else {
                        // Call to download all data
                        downloadAllFeedData(mFeedList);
                    }
                } else {
                    // error response, no access to resource?
                    Log.e("ERROR FEED LIST", "error in response");
                    // TODO Feedback to user
                }
            }

            @Override
            public void onFailure(Call<FeedDRResponse> call, Throwable t) {
                // something went completely south (like no internet connection)
                Log.e("Error GetFeeds RETROFIT", t.getMessage());
                // TODO Feedback to user
            }
        });
    }

    public void setupListFeeds() {

        RecyclerView feedListView = (RecyclerView) mBaseView.findViewById(R.id.feeds_list);

        ItemClickSupport.addTo(feedListView).setOnItemClickListener(
                new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        Intent intent = new Intent(
                                mActivity.getApplicationContext(),
                                FeedActivity.class);

                        Feed feed = mFeedList.get(position);
                        intent.putExtra(FEED_LINK,
                                        feed.getLink().toString());
                        intent.putExtra(FEED_PK, feed.getPk());
                        intent.putExtra(FEED_API, feed);

                        startActivity(intent);
                    }
                }
        );
    }

    public void setupAuthenticationFromDB() {
        showProgressBar();

        // Get DBHelper
        final GeniusFeedContract.GeniusFeedDbHelper mDbHelper =
                new GeniusFeedContract.GeniusFeedDbHelper(getContext());

        class GetReadableDatabase extends AsyncTask<Void, Void, SQLiteDatabase> {
            @Override
            protected SQLiteDatabase doInBackground(Void... params) {
                return mDbHelper.getReadableDatabase();
            }

            protected void onPostExecute(SQLiteDatabase dataBase) {
                // Define projector
                String[] projection = {
                        GeniusFeedContract.User.COLUMN_NAME_USER_ID,
                        GeniusFeedContract.User.COLUMN_NAME_USERNAME,
                        GeniusFeedContract.User.COLUMN_NAME_TOKEN
                };

                Cursor c = dataBase.query(
                        GeniusFeedContract.User.TABLE_NAME,
                        projection,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "1"
                );

                if (c.getCount() > 0) {
                    c.moveToFirst();
                    String username = c.getString
                            (c.getColumnIndex(GeniusFeedContract.User
                                     .COLUMN_NAME_USERNAME));
                    String token = c.getString
                            (c.getColumnIndex(GeniusFeedContract.User
                                     .COLUMN_NAME_TOKEN));

                    if (username != null &&
                            !username.trim().isEmpty() &&
                            token != null &&
                            !token.trim().isEmpty()) {
                        Authentication auth = new Authentication(username);
                        auth.setToken(token);
                        Authentication.setCredentials(auth);
                    }
                }

                // Remove progressBar
                hideProgressBar();
                getFeedFromAPI();
            }

        }

        new GetReadableDatabase().execute();
    }

    public void showDialog() {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        AddFeedDialogFragment newFragment = AddFeedDialogFragment.newInstance();
        // Setting helper to update values after operation
        newFragment.setUpdateHelper(this);

        newFragment.show(ft, "dialog");
    }

    private void showProgressBar() {
        // Show progressBar
        mProgressBar.setVisibility(ProgressBar.VISIBLE);
        mFeedListView.setVisibility(RecyclerView.INVISIBLE);
    }

    private void hideProgressBar() {
        // Stopping progress bar from swipe
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }

        // Remove progressBar
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);
        mFeedListView.setVisibility(RecyclerView.VISIBLE);
    }


    private void showAlertMessageForError(int errorCode) {
        // Print alert on mainThread
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.error_deleting_feed)
               .setCancelable(false)
               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // Dismiss dialog
                       //dialog.dismiss();
                   }
               });

        builder.create().show();
    }

    private void deleteFeed(final int position) {
        long feedPk = Long.parseLong(mFeedList.get(position).getPk());

        String token = Authentication.getCredentials().getToken();
        GeniusFeedService service = NetworkServiceBuilder.createService(GeniusFeedService.class, token);

        Call<ResponseBody> call = service.deleteFeedSource(feedPk);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d("RESPONSE", "Feed Remove");
                if (response.isSuccessful()) {
                    CoordinatorLayout parentView = (CoordinatorLayout) getActivity().findViewById(R.id.feed_list_content);
                    Snackbar.make(parentView, R.string.done_deleting_feed, Snackbar.LENGTH_LONG).show();
                    getFeedFromAPI();
                } else {
                    showAlertMessageForError(0);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("ERROR", "Feed fail to delete");
                showAlertMessageForError(0);
            }
        });
    }

    @Override
    public void updateFeedData(Feed newFeed) {
        mFeedList.add(newFeed);
        mFeedListView.getAdapter().notifyDataSetChanged();
        getFeedFromAPI();
    }

    private void downloadAllFeedData(List<Feed> feeds) {
        // Download all feed item read
        mFeedItemsReadByFeed = new HashMap<String, List<FeedItemRead>>();
        if(feeds.size() > 0) {
            getFeedItemReadList(new ArrayList<FeedItemRead>(), 1, feeds.get(0).getPk());
        } else {
            hideProgressBar();
        }
    }

    private void getFeedItemReadList(final List<FeedItemRead> feedItemList, final int page, final String feedPk) {
        final String token = Authentication.getCredentials().getToken();

        GeniusFeedService service = NetworkServiceBuilder.createService(GeniusFeedService.class, token);

        Call<FeedItemReadDRResponse> call = service.getFeedItemReads(feedPk, page);

        call.enqueue(new Callback<FeedItemReadDRResponse>() {
            @Override
            public void onResponse(Call<FeedItemReadDRResponse> call, Response<FeedItemReadDRResponse> response) {
                if (response.isSuccessful()) {
                    List<FeedItemRead> feedItemReadList =  response.body().getResults();
                    // Sync feedItemReads from API with new items of Feed Source
                    for(Iterator<FeedItemRead> iterator = feedItemReadList.iterator(); iterator.hasNext();) {
                        FeedItemRead item = iterator.next();
                        feedItemList.add(item);
                    }

                    if(response.body().getNext() != null) {
                        getFeedItemReadList(feedItemList, page + 1, feedPk);
                    }else{
                        mFeedItemsReadByFeed.put(feedPk,feedItemList);
                        Feed nextFeed = getNextFeedToFeedPk(feedPk);
                        if(nextFeed != null)
                            getFeedItemReadList(new ArrayList<FeedItemRead>(), 1, nextFeed.getPk());
                        else {
                            hideProgressBar();

                            FeedArrayAdapter feedArrayAdapter = new FeedArrayAdapter(new ArrayList<Feed>(mFeedList),
                                                                                     mFeedItemsReadByFeed);
                            if(timeframeFeeds) {
                                feedArrayAdapter.setEnableTimeFrameMark(true);
                            }
                            mFeedListView.setAdapter(feedArrayAdapter);
                        }

                    }
                }else{
                    // error response, no access to resource?
                    Log.e("ERROR FEED ITEM LIST", "error in response, with bad http code");
                    // TODO feedback to user
                }
            }

            @Override
            public void onFailure(Call<FeedItemReadDRResponse> call, Throwable t) {
                // something went completely south (like no internet connection)
                Log.e("Error Login RETROFIT", t.getMessage());
                // TODO feedback to user
                hideProgressBar();
            }
        });
    }

    private Feed getNextFeedToFeedPk(String feedPk) {
        for (Iterator<Feed> i = mFeedList.iterator();
             i.hasNext();) {
            Feed feed = i.next();
            if(feed.getPk().equals(feedPk)) {
                if(i.hasNext()){
                    return i.next();
                }
            }
        }

        return null;
    }
}
