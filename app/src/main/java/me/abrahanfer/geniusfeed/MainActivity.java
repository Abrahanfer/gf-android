package me.abrahanfer.geniusfeed;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


import org.json.JSONObject;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

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

        class RetrieveSomething extends AsyncTask<String, Void, String> {
            // RequestQueue queue = Volley.newRequestQueue(this);
    private Exception exception;

            protected String doInBackground(String...urls) {
                String url = "http://10.0.240.29/feed_item_reads.json";

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

                // Testing Spring Framework
                RestTemplate restTemplate = new RestTemplate();

                restTemplate.getMessageConverters().

                        add(new MappingJackson2HttpMessageConverter()

                        );

                FeedItemRead[] feedItemReads = restTemplate.getForObject(url, FeedItemRead[].class);
                for (
                        FeedItemRead item
                        : feedItemReads)

                {
                    System.out.println(item.getFeedItem());
                }
                return new String();
            }
        };

        new RetrieveSomething().execute("mierda");
    }
}
