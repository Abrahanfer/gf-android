package me.abrahanfer.geniusfeed;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import org.springframework.http.HttpAuthentication;
import org.springframework.http.HttpBasicAuthentication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;

import me.abrahanfer.geniusfeed.models.DRResponseModels.FeedDRResponse;
import me.abrahanfer.geniusfeed.models.Feed;
import me.abrahanfer.geniusfeed.utils.Authentication;
import me.abrahanfer.geniusfeed.utils.FeedArrayAdapter;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBaseView = inflater.inflate(R.layout.feed_list_fragment,
                                container, false);
        return mBaseView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = getActivity();
       
        setupListFeeds();
        testRequest();
    }

    public void testRequest() {
        // Make a request to API
        // Instantiate the RequestQueue.

        class RetrieveFeeds extends AsyncTask<String, Void, Feed[]> {
            private Exception exception;

            @Override
            protected Feed[] doInBackground(String... urls) {

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
}
