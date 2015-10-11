package me.abrahanfer.geniusfeed;

import android.app.Notification;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import org.springframework.http.HttpAuthentication;
import org.springframework.http.HttpBasicAuthentication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;

import me.abrahanfer.geniusfeed.models.Feed;
import me.abrahanfer.geniusfeed.models.FeedItemRead;
import me.abrahanfer.geniusfeed.models.FeedItemReadDRResponse;
import me.abrahanfer.geniusfeed.utils.FeedItemArrayAdapter;

public class FeedItemActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_item);

        String pkFeedItemRead = getIntent()
                .getStringExtra(MainActivity.FEED_ITEM_READ);

        TextView textView = (TextView) findViewById(R.id.feedItemTextView);

        textView.setText("");

        getFeedItemRead(pkFeedItemRead);
        markFeedItemAsRead(pkFeedItemRead);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_feed_item, menu);
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

    public void getFeedItemRead(String pkFeedItem){
            class RetrieveFeedItem extends AsyncTask<String, Void,
                    FeedItemRead> {
                // RequestQueue queue = Volley.newRequestQueue(this);
                private Exception exception;

                @Override
                protected FeedItemRead doInBackground(String...urls) {

                    // Adding header for Basic HTTP Authentication
                    HttpAuthentication authHeader = new HttpBasicAuthentication
                            ("test-user-1", "test1");
                    HttpHeaders requestHeaders = new HttpHeaders();
                    requestHeaders.setAuthorization(authHeader);
                    HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);
                    // Testing Spring Framework
                    RestTemplate restTemplate = new RestTemplate();

                    restTemplate.getMessageConverters().

                            add(new MappingJackson2HttpMessageConverter()

                            );

                    HttpEntity<FeedItemRead> response = restTemplate
                            .exchange(urls[0], HttpMethod.GET, requestEntity,
                                    FeedItemRead.class);



                    FeedItemRead feedItemRead =  response.getBody();;

                    System.out.println("GOOD REQUEST!!!");

                    return feedItemRead;
                }

                protected void onPostExecute(FeedItemRead feedItemRead){
                    TextView textView =(TextView) findViewById(R.id.feedItemTextView);

                    textView.setText(feedItemRead.getFeed_item().getTitle());
                }
            };


            String url = "http://" + MainActivity.DOMAIN + "/feed_item_reads/" +
                    pkFeedItem +
                    ".json";

            new RetrieveFeedItem().execute(url);
        }

    public void markFeedItemAsRead(String pkFeedItemRead){
        class PostToFeedItem extends AsyncTask<String, Void,
                FeedItemRead> {
            // RequestQueue queue = Volley.newRequestQueue(this);
            private Exception exception;

            @Override
            protected FeedItemRead doInBackground(String...urls) {

                // Adding header for Basic HTTP Authentication
                HttpAuthentication authHeader = new HttpBasicAuthentication
                        ("test-user-1", "test1");
                HttpHeaders requestHeaders = new HttpHeaders();
                requestHeaders.setAuthorization(authHeader);
                // Create the request body as a MultiValueMap
                MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();

                body.add("read", "True");
                HttpEntity<?> requestEntity = new HttpEntity<Object>
                        (body,requestHeaders);
                // Testing Spring Framework
                RestTemplate restTemplate = new RestTemplate();

                restTemplate.getMessageConverters().

                        add(new MappingJackson2HttpMessageConverter()

                        );

                HttpEntity<FeedItemRead> response = restTemplate
                        .exchange(urls[0], HttpMethod.PUT, requestEntity,
                                FeedItemRead.class);



                FeedItemRead feedItemRead =  response.getBody();;

                System.out.println("GOOD REQUEST!!!");

                return feedItemRead;
            }

            protected void onPostExecute(FeedItemRead feedItemRead){
                System.out.println("Esta leido? :" + feedItemRead.getRead());
            }
        };


        String url = "http://" + MainActivity.DOMAIN + "/feed_item_reads/" +
                pkFeedItemRead +
                "";

        new PostToFeedItem().execute(url);
    }
}
