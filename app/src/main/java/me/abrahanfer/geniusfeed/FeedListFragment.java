package me.abrahanfer.geniusfeed;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBaseView = inflater.inflate(R.layout.feed_list_fragment,
                                container, false);

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

                    if (response.body().getNext() != null){
                        getFeedFromAPIPagination(feedArrayList, 2);
                    }else{
                        // tasks available
                        mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                        // TODO All ok

                        ListView listFeeds = (ListView) mBaseView
                                .findViewById(
                                        R.id.listFeeds);
                        FeedArrayAdapter feedArrayAdapter
                                = new FeedArrayAdapter(
                                mActivity.getApplicationContext(),
                                feedArrayList);

                        mProgressBar.setVisibility(ProgressBar
                                                           .INVISIBLE);
                        listFeeds.setAdapter(feedArrayAdapter);
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

    public void getFeedFromAPIPagination(final List<Feed> feedList, final int page) {
        String token = Authentication.getCredentials().getToken();
        GeniusFeedService service = NetworkServiceBuilder.createService(GeniusFeedService.class, token);

        Call<FeedDRResponse> call = service.getFeeds(page);

        call.enqueue(new Callback<FeedDRResponse>() {
            @Override
            public void onResponse(Call<FeedDRResponse> call, Response<FeedDRResponse> response) {
                if (response.isSuccessful()) {
                    ArrayList<Feed> feedArrayList = new ArrayList<>(
                            response.body().getResults());
                    feedList.addAll(feedArrayList);

                    if (response.body().getNext() != null){
                        getFeedFromAPIPagination(feedList, page + 1);
                    }else{

                        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

                        ListView listFeeds = (ListView) mBaseView.findViewById(R.id.listFeeds);
                        FeedArrayAdapter feedArrayAdapter = new FeedArrayAdapter(mActivity.getApplicationContext(),
                                                                                 new ArrayList<Feed>(feedList));
                        listFeeds.setAdapter(feedArrayAdapter);
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
        final ListView listFeeds = (ListView) mBaseView.findViewById(
                R.id.listFeeds);

        listFeeds.setOnItemClickListener(
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
                });
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
}
