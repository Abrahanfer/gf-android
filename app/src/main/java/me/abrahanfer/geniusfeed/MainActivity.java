package me.abrahanfer.geniusfeed;

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
import android.widget.ArrayAdapter;
import android.widget.ListView;


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

        class RetrieveFeedItemUnreads extends AsyncTask<String, Void,
                FeedItemRead[]> {
            // RequestQueue queue = Volley.newRequestQueue(this);
            private Exception exception;

            @Override
            protected FeedItemRead[] doInBackground(String...urls) {


                // Request a string response from the provided URL.
                /*JsonObjectRequest jsonRequest = new JsonObjectRequest(url,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        TextView mTextView =(TextView) findViewById(R.id.textTest);
                        // Display the first 500 characters of the response string.
                        mTextView.setText("Response is: "+ response.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                TextView mTextView =(TextView) findViewById(R.id.textTest);
                mTextView.setText("That didn't work!" + error.networkResponse.data);
            }
        });
        // Add the request to the RequestQueue.
        queue.add(jsonRequest);*/
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

                HttpEntity<FeedItemReadDRResponse> response = restTemplate
                        .exchange(urls[0], HttpMethod.GET, requestEntity,
                                FeedItemReadDRResponse.class);

                FeedItemReadDRResponse result = response.getBody();
                FeedItemRead[] feedItemReads = result.getResults();
                System.out.println("Array size: " + feedItemReads.length);

                return feedItemReads;
            }

            protected void onPostExecute(FeedItemRead[] feedItemReads){
                String[] titles = new String[feedItemReads.length];
                for(int i = 0; i < feedItemReads.length; ++i)
                    titles[i] = feedItemReads[i].getFeed_item().getTitle();

                ListView listFeeds =(ListView) findViewById(R.id.listFeeds);
                ArrayAdapter<String> feedItemReadArrayAdapter = new
                        ArrayAdapter<String>(getApplicationContext(),
                        R.layout.feed_item,
                        titles);

                listFeeds.setAdapter(feedItemReadArrayAdapter);
            }
        };

        String ip = "10.0.240.29";
        String url = "http://" + ip + "/feed_item_reads/unread.json";

        new RetrieveFeedItemUnreads().execute(url);



    }
}
