package me.abrahanfer.geniusfeed;

import android.content.Intent;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.HttpAuthHandler;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;


import org.springframework.http.HttpAuthentication;
import org.springframework.http.HttpBasicAuthentication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;

import me.abrahanfer.geniusfeed.models.Feed;
import me.abrahanfer.geniusfeed.models.FeedItemReadDRResponse;
import me.abrahanfer.geniusfeed.models.FeedItemRead;
import me.abrahanfer.geniusfeed.utils.FeedItemArrayAdapter;
import me.abrahanfer.geniusfeed.utils.Authentication;

public class MainActivity extends ActionBarActivity {
    public final static String FEED_ITEM_READ = "me.abrahanfer.geniusfeed" +
            ".FEED_ITEM_READ";
    public final static String LOGIN_CREDENTIALS = "me.abrahanfer.geniusfeed" +
            ".LOGIN_CREDENTIALS";
   // public final static String DOMAIN = "10.0.240.29";
   public final static String DOMAIN = "192.168.1.55";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupListFeedItems();
        testRequest();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void testRequest() {
        // Make a request to API
        // Instantiate the RequestQueue.

        class RetrieveFeedItemUnreads extends AsyncTask<String, Void,
                FeedItemRead[]> {
            // RequestQueue queue = Volley.newRequestQueue(this);
            private Exception exception;

            @Override
            protected FeedItemRead[] doInBackground(String...urls) {

                Authentication authentication = Authentication.getCredentials();
                if(authentication == null){
                    Intent intent = new Intent(getApplicationContext(),LoginActivity
                            .class);

                    startActivity(intent);
                    return null;
                }
                System.out.println("Que bastinazo NO!!???");
                String username = authentication.getUsername();
                String password = authentication.getPassword();
                // Adding header for Basic HTTP Authentication
                HttpAuthentication authHeader = new HttpBasicAuthentication
                        (username, password);
                HttpHeaders requestHeaders = new HttpHeaders();
                requestHeaders.setAuthorization(authHeader);
                HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);
                // Testing Spring Framework
                RestTemplate restTemplate = new RestTemplate();

                restTemplate.getMessageConverters().

                        add(new MappingJackson2HttpMessageConverter()

                        );

                HttpEntity<FeedItemReadDRResponse> response = restTemplate
                        .exchange(urls[0], HttpMethod.GET, requestEntity,
                                FeedItemReadDRResponse.class);

                FeedItemReadDRResponse result = response.getBody();
                FeedItemRead[] feedItemReads = result.getResults();
                System.out.println("Array size: " + feedItemReads.length);

                return feedItemReads;
            }

            protected void onPostExecute(FeedItemRead[] feedItemReads){
                ArrayList<FeedItemRead> feedArrayList = new
                        ArrayList<FeedItemRead>(Arrays.asList(feedItemReads));

                ListView listFeeds =(ListView) findViewById(R.id.listFeeds);
                FeedItemArrayAdapter feedItemReadArrayAdapter = new
                        FeedItemArrayAdapter(getApplicationContext(),
                        feedArrayList);

                listFeeds.setAdapter(feedItemReadArrayAdapter);
            }
        };


        String url = "http://" + DOMAIN + "/feed_item_reads/unread.json";

        new RetrieveFeedItemUnreads().execute(url);
    }

    public void setupListFeedItems(){
        final ListView listFeedItems =(ListView) findViewById(R.id.listFeeds);

        listFeedItems.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        System.out.println("Fed Item click" + position);
                        Intent intent = new Intent(getApplicationContext(),FeedItemActivity
                                .class);
                        FeedItemRead feedItemRead =(FeedItemRead) listFeedItems
                                .getAdapter()
                                .getItem
                                (position);
                        intent.putExtra(FEED_ITEM_READ, feedItemRead
                                .getPk());

                        startActivity(intent);
                    }
                }
        );
    }
}
