package me.abrahanfer.geniusfeed;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import me.abrahanfer.geniusfeed.models.DRResponseModels.FeedItemReadDRResponse;
import me.abrahanfer.geniusfeed.models.Feed;
import me.abrahanfer.geniusfeed.models.FeedItemRead;
import me.abrahanfer.geniusfeed.utils.FeedArrayAdapter;
import me.abrahanfer.geniusfeed.utils.Authentication;

public class MainActivity extends ActionBarActivity {
    public final static String FEED = "me.abrahanfer.geniusfeed" +
            ".FEED";
    public final static String LOGIN_CREDENTIALS = "me.abrahanfer.geniusfeed" +
            ".LOGIN_CREDENTIALS";
   // public final static String DOMAIN = "10.0.240.29";
   public final static String DOMAIN = "192.168.1.55";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupListFeeds();
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

        class RetrieveFeeds extends AsyncTask<String, Void,
                Feed[]> {
            private Exception exception;

            @Override
            protected Feed[] doInBackground(String...urls) {

                Authentication authentication = Authentication.getCredentials();
                if(authentication == null) {
                    Intent intent = new Intent(getApplicationContext(), LoginActivity
                            .class);


                    startActivity(intent);
                    return new Feed[0];
                }else {
                    String username = authentication.getUsername();
                    String password = authentication.getPassword();


                    // Adding header for Basic HTTP Authentication
                    HttpAuthentication authHeader = new HttpBasicAuthentication(username, password);
                    HttpHeaders requestHeaders = new HttpHeaders();
                    requestHeaders.setAuthorization(authHeader);
                    HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);

                    RestTemplate restTemplate = new RestTemplate();

                    restTemplate.getMessageConverters()
                            .add(new MappingJackson2HttpMessageConverter());
                    try {
                        HttpEntity<FeedDRResponse> response = restTemplate
                                .exchange(urls[0], HttpMethod.GET, requestEntity,
                                        FeedDRResponse.class);

                        FeedDRResponse result = response.getBody();
                        Feed[] feeds = result.getResults();
                        System.out.println("Array size: " + feeds.length);

                        return feeds;
                    } catch (RestClientException springException) {
                        System.out.println("Mirando la excepcion de Spring");
                        System.out.println(springException);
                        return new Feed[0];
                    }
                }
            }

            protected void onPostExecute(Feed[] feeds){
                System.out.println("Mirando en onPostExecute 2" + feeds + "Tantos feed");
                ArrayList<Feed> feedArrayList = new
                        ArrayList<>(Arrays.asList(feeds));

                ListView listFeeds =(ListView) findViewById(R.id.listFeeds);
                FeedArrayAdapter feedArrayAdapter = new
                        FeedArrayAdapter(getApplicationContext(),
                        feedArrayList);

                listFeeds.setAdapter(feedArrayAdapter);
            }
        }


        String url = "http://" + DOMAIN + "/feeds";

        new RetrieveFeeds().execute(url);
    }

    public void setupListFeeds(){
        final ListView listFeeds =(ListView) findViewById(R.id.listFeeds);

        listFeeds.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        System.out.println("Feed click" + position);
                        Intent intent = new Intent(getApplicationContext(),FeedActivity
                                .class);
                        Feed feed =(Feed) listFeeds
                                .getAdapter()
                                .getItem
                                (position);
                        intent.putExtra(FEED, feed
                                .getLink().toString());

                        startActivity(intent);
                    }
                }
        );
    }
}
