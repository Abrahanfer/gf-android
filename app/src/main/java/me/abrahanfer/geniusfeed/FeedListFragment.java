package me.abrahanfer.geniusfeed;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

import me.abrahanfer.geniusfeed.models.DRResponseModels.FeedDRResponse;
import me.abrahanfer.geniusfeed.models.Feed;
import me.abrahanfer.geniusfeed.utils.Authentication;
import me.abrahanfer.geniusfeed.utils.Constants;
import me.abrahanfer.geniusfeed.utils.DividerItemDecoration;
import me.abrahanfer.geniusfeed.utils.FeedArrayAdapter;
import me.abrahanfer.geniusfeed.utils.GeniusFeedContract;
import me.abrahanfer.geniusfeed.utils.network.GeniusFeedService;
import me.abrahanfer.geniusfeed.utils.network.NetworkServiceBuilder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by abrahan on 2/04/16.
 */
public class FeedListFragment extends Fragment {
    public final static String FEED = "me.abrahanfer.geniusfeed" +
            ".FEED";
    public final static String LOGIN_CREDENTIALS =
            "me.abrahanfer.geniusfeed" + ".LOGIN_CREDENTIALS";

    public final static String FEED_LINK = "FEED_LINK";
    public final static String FEED_PK = "FEED_PK";
    public final static String FEED_API = "FEED_API";
    // public final static String DOMAIN = "10.0.240.29";
    // public final static String DOMAIN = "192.168.1.55";

    private View mBaseView;
    private Activity mActivity;
    private ProgressBar mProgressBar;

    private ArrayList<Feed> mFeedList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBaseView = inflater.inflate(R.layout.feed_list_fragment,
                                container, false);

        FloatingActionButton floatingActionButton = (FloatingActionButton) mBaseView.findViewById(R.id.add_feed_button);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("Mriando el feedback", "<---------");
                showDialog();
            }
        });

        return mBaseView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = getActivity();
        mProgressBar = (ProgressBar) mActivity.findViewById(R.id
                                                                    .pbLoading);
        setupListFeeds();
        setupAuthenticationFromDB();
        //testRequest();
    }

    public void getFeedFromAPI() {
        // ProgressBar
        mProgressBar.setVisibility(ProgressBar.VISIBLE);
        String username;
        String token;

        // TODO Make request with retrofit
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

        Call<FeedDRResponse> call = service.getFeeds(1);

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
                        // tasks available
                        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

                        RecyclerView feedList = (RecyclerView) mBaseView.findViewById(R.id.feeds_list);
                        feedList.setHasFixedSize(true);

                        // use a linear layout manager
                        LinearLayoutManager layoutManager = new LinearLayoutManager(mBaseView.getContext());
                        feedList.setLayoutManager(layoutManager);

                        RecyclerView.ItemDecoration itemDecoration = new
                                DividerItemDecoration(mBaseView.getContext(), DividerItemDecoration.VERTICAL_LIST);
                        feedList.addItemDecoration(itemDecoration);

                        FeedArrayAdapter feedArrayAdapter
                                = new FeedArrayAdapter(feedArrayList);

                        mProgressBar.setVisibility(ProgressBar
                                                           .INVISIBLE);
                        //listFeeds.setAdapter(feedArrayAdapter);

                        feedList.setAdapter(feedArrayAdapter);
                    }

                } else {
                    // error response, no access to resource?
                    Log.e("ERROR FEED LIST", "error in response");
                }
            }

            @Override
            public void onFailure(Call<FeedDRResponse> call, Throwable t) {
                // something went completely south (like no internet connection)
                Log.e("Error GetFeeds RETROFIT", t.getMessage());
            }
        });
    }

    public void getFeedFromAPIPagination( final int page) {
        String token = Authentication.getCredentials().getToken();
        GeniusFeedService service = NetworkServiceBuilder.createService(GeniusFeedService.class, token);

        Call<FeedDRResponse> call = service.getFeeds(page);

        call.enqueue(new Callback<FeedDRResponse>() {
            @Override
            public void onResponse(Call<FeedDRResponse> call, Response<FeedDRResponse> response) {
                if (response.isSuccessful()) {
                    ArrayList<Feed> feedArrayList = new ArrayList<>(
                            response.body().getResults());
                    mFeedList.addAll(feedArrayList);

                    if (response.body().getNext() != null){
                        getFeedFromAPIPagination(page + 1);
                    }else{

                        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

                        RecyclerView feedListView = (RecyclerView) mBaseView.findViewById(R.id.feeds_list);
                        feedListView.setHasFixedSize(true);

                        // use a linear layout manager
                        LinearLayoutManager layoutManager = new LinearLayoutManager(mBaseView.getContext());
                        feedListView.setLayoutManager(layoutManager);


                        FeedArrayAdapter feedArrayAdapter = new FeedArrayAdapter(new ArrayList<Feed>(mFeedList));
                        feedListView.setAdapter(feedArrayAdapter);
                    }
                } else {
                    // error response, no access to resource?
                    Log.e("ERROR FEED LIST", "error in response");
                }
            }

            @Override
            public void onFailure(Call<FeedDRResponse> call, Throwable t) {
                // something went completely south (like no internet connection)
                Log.e("Error GetFeeds RETROFIT", t.getMessage());
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
                        Log.d("Position", "POsition = " + position);
                        intent.putExtra(FEED_LINK,
                                        feed.getLink().toString());
                        intent.putExtra(FEED_PK, feed.getPk());
                        intent.putExtra(FEED_API, feed);

                        startActivity(intent);
                    }
                }
        );


        /*final RecyclerView feedList = (RecyclerView) mBaseView.findViewById(R.id.feeds_list);

        feedList.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent,
                                            View view, int position,
                                            long id) {
                        System.out.println("Feed click" + position);
                        Intent intent = new Intent(
                                mActivity.getApplicationContext(),
                                FeedActivity.class);

                        Feed feed = (Feed) listFeeds.getAdapter()
                                                    .getItem(
                                                            position);
                        intent.putExtra(FEED_LINK,
                                        feed.getLink().toString());
                        intent.putExtra(FEED_PK, feed.getPk());
                        intent.putExtra(FEED_API, feed);

                        startActivity(intent);
                    }
                });*/
    }

    public void setupAuthenticationFromDB() {
        // ProgressBar
        mProgressBar.setVisibility(ProgressBar.VISIBLE);

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

                if(c.getCount() > 0) {
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
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
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
        newFragment.show(ft, "dialog");
    }
}
