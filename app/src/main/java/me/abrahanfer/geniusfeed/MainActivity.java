package me.abrahanfer.geniusfeed;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.HttpAuthHandler;


import org.springframework.http.HttpAuthentication;
import org.springframework.http.HttpBasicAuthentication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import me.abrahanfer.geniusfeed.models.FeedItemReadDRResponse;
import me.abrahanfer.geniusfeed.models.FeedItemRead;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        class GetFeedItemsUnread extends AsyncTask<String, Void, FeedItemRead[]> {
            // RequestQueue queue = Volley.newRequestQueue(this);
            private Exception exception;

            protected FeedItemRead[] doInBackground(String...urls) {

                // Adding header for Basic HTTP Authentication
                HttpAuthentication authHeader = new HttpBasicAuthentication("test-user-1", "test1");
                HttpHeaders requestHeaders = new HttpHeaders();
                requestHeaders.setAuthorization(authHeader);
                HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);
                // Testing Spring Framework
                RestTemplate restTemplate = new RestTemplate();

                restTemplate.getMessageConverters().

                        add(new MappingJackson2HttpMessageConverter()

                        );

                HttpEntity<FeedItemReadDRResponse> response;
                response = restTemplate.exchange(urls[0], HttpMethod.GET, requestEntity, FeedItemReadDRResponse.class);

                FeedItemReadDRResponse result = response.getBody();
                System.out.println("Array size: " + result.getResults().length);

                FeedItemReadDRResponse djangoRestResponse;
                djangoRestResponse = restTemplate.getForObject(urls[0], FeedItemReadDRResponse.class);

                System.out.println(djangoRestResponse.getCount());
                FeedItemRead[] feedItemReads = djangoRestResponse.getResults();
                System.out.println("Array size: " + feedItemReads.length);
                return feedItemReads;
            }
        };

        String url = "http://192.168.1.55/feed_item_reads/unread.json";
        FeedItemRead[] feedItemsUnread = new GetFeedItemsUnread().execute(url);
    }
}
