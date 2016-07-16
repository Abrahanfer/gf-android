package me.abrahanfer.geniusfeed;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.springframework.http.HttpAuthentication;
import org.springframework.http.HttpBasicAuthentication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;

import me.abrahanfer.geniusfeed.models.DRResponseModels.FeedDRResponse;
import me.abrahanfer.geniusfeed.models.Feed;
import me.abrahanfer.geniusfeed.models.FeedItem;
import me.abrahanfer.geniusfeed.utils.Authentication;
import me.abrahanfer.geniusfeed.utils.FeedArrayAdapter;
import me.abrahanfer.geniusfeed.utils.GeniusFeedContract;

/**
 * Created by abrahan on 2/04/16.
 */
public class FeedListFragment extends Fragment {
    public final static String FEED = "me.abrahanfer.geniusfeed" +
            ".FEED";
    public final static String LOGIN_CREDENTIALS =
            "me.abrahanfer.geniusfeed" + ".LOGIN_CREDENTIALS";
    // public final static String DOMAIN = "10.0.240.29";
    public final static String DOMAIN = "192.168.1.55";

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

    public void testRequest() {
        // ProgressBar
        mProgressBar.setVisibility(ProgressBar.VISIBLE);

        // Make a request to API
        // Instantiate the RequestQueue.

        class RetrieveFeeds extends AsyncTask<String, Void, Feed[]> {
            private Exception exception;

            @Override
            protected Feed[] doInBackground(String... urls) {

                Log.e("FeedListFragment", "Error con barra de progreso 2");
                Authentication authentication = Authentication
                        .getCredentials();
                if (authentication == null) {
                    Intent intent = new Intent(
                            mActivity.getApplicationContext(),
                            LoginActivity.class);


                    startActivity(intent);
                    return new Feed[0];
                } else {
                    String username = authentication.getUsername();
                    String password = authentication.getPassword();


                    // Adding header for Basic HTTP Authentication
                    HttpAuthentication authHeader
                            = new HttpBasicAuthentication(username,
                                                          password);
                    HttpHeaders requestHeaders = new HttpHeaders();
                    requestHeaders.setAuthorization(authHeader);
                    HttpEntity<?> requestEntity
                            = new HttpEntity<Object>(requestHeaders);

                    RestTemplate restTemplate = new RestTemplate();

                    restTemplate.getMessageConverters()
                                .add(new MappingJackson2HttpMessageConverter());
                    try {
                        Log.e("FeedListFragment", "Error con barra de progreso 3");
                        HttpEntity<FeedDRResponse> response
                                = restTemplate
                                .exchange(urls[0], HttpMethod.GET,
                                          requestEntity,
                                          FeedDRResponse.class);

                        FeedDRResponse result = response.getBody();
                        Feed[] feeds = result.getResults();
                        System.out.println(
                                "Array size: " + feeds.length);


                        return feeds;
                    } catch (RestClientException springException) {
                        System.out.println(
                                "Mirando la excepcion de Spring");
                        System.out.println(springException);
                        return new Feed[0];
                    }
                }
            }

            protected void onPostExecute(Feed[] feeds) {
                System.out.println(
                        "Mirando en onPostExecute 2" + feeds +
                                "Tantos feed");
                ArrayList<Feed> feedArrayList = new ArrayList<>(
                        Arrays.asList(feeds));

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
        }


        String url = "http://" + DOMAIN + "/feeds";

        new RetrieveFeeds().execute(url);
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
                        intent.putExtra(FEED,
                                        feed.getLink().toString());
                        startActivity(intent);
                    }
                });
    }

    public void setupAuthenticationFromDB() {
        // ProgressBar
        mProgressBar.setVisibility(ProgressBar.VISIBLE);
        Log.e("FeedListFragment", "Error con barra de progreso -3");
        // Get DBHelper
        final GeniusFeedContract.GeniusFeedDbHelper mDbHelper =
                new GeniusFeedContract.GeniusFeedDbHelper(getContext
                                                                 ());

        Log.e("FeedListFragment", "Error con barra de progreso -2");
        class GetReadableDatabase extends AsyncTask<Void, Void, SQLiteDatabase> {
            @Override
            protected SQLiteDatabase doInBackground(Void... params) {
                Log.e("FeedListFragment", "Error con barra de progreso -1");
                return mDbHelper.getReadableDatabase();
            }

            protected void onPostExecute(SQLiteDatabase dataBase) {
                // Define projector
                String[] projection = {
                        GeniusFeedContract.User.COLUMN_NAME_USER_ID,
                        GeniusFeedContract.User.COLUMN_NAME_USERNAME,
                        GeniusFeedContract.User.COLUMN_NAME_PASSWORD
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
                    String password = c.getString
                            (c.getColumnIndex(GeniusFeedContract.User
                                     .COLUMN_NAME_PASSWORD));

                    if (username != null &&
                            !username.trim().isEmpty() &&
                            password != null &&
                            !password.trim().isEmpty()) {
                        Authentication auth = new Authentication(username,password);
                        Authentication.setCredentials(auth);
                    }
                }
                Log.e("FeedListFragment", "Error con barra de progreso 1");
                // Remove progressBar
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                testRequest();
            }

        }

        new GetReadableDatabase().execute();


    }
}
