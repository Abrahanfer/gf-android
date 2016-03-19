package me.abrahanfer.geniusfeed;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.einmalfel.earl.EarlParser;

import org.springframework.http.HttpAuthentication;
import org.springframework.http.HttpBasicAuthentication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.DataFormatException;

import me.abrahanfer.geniusfeed.models.Feed;
import me.abrahanfer.geniusfeed.models.FeedItem;
import me.abrahanfer.geniusfeed.models.FeedItemRead;

/**
 * Created by abrahan on 19/03/16.
 */
public class FeedActivity extends AppCompatActivity {
    final String EARL_TAG = "EarlFeedUtil";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        URL feedLink = null;
        try {
            feedLink = new URL(getIntent()
                    .getStringExtra(MainActivity.FEED));
        } catch (MalformedURLException exceptino) {
            System.out.println("Viene mal la URL");
        }

        TextView textView =
                (TextView) findViewById(R.id.feedTextView);

        textView.setText("");

        getFeedItemsFromFeed(feedLink);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_feed, menu);
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

    public void getFeedItemsFromFeed(final URL feedLink){
        class RetrieveFeedItems extends AsyncTask<URL, Void,
                FeedItem[]> {
            private Exception exception;

            @Override
            protected FeedItem[] doInBackground(URL... url) {
                InputStream inputStream = null;
                com.einmalfel.earl.Feed feed = null;
                Log.i(EARL_TAG, "Mirando link" + url);
                try {
                    inputStream = feedLink.openConnection().getInputStream();
                }catch (IOException exception) {
                    Log.d(EARL_TAG, "Exception IO");
                    return new FeedItem[0];
                }
                try {
                    feed = EarlParser.parseOrThrow(inputStream, 0);
                }catch (XmlPullParserException xmlExcepcion){
                    Log.d(EARL_TAG, "Exception XML Pasrser");
                }catch (IOException ioException){
                    Log.d(EARL_TAG, "Exception IO");
                }catch (DataFormatException dataException) {
                    Log.d(EARL_TAG, "Exception data format");
                }

                Log.i(EARL_TAG, "Processing feed: " + feed.getTitle());
                for (com.einmalfel.earl.Item item : feed.getItems()) {
                    String title = item.getTitle();
                    Log.i(EARL_TAG, "Item title: " + (title == null ? "N/A" : title));
                }

                FeedItem[] feedItems = new FeedItem[0];
                System.out.println("GOOD REQUEST!!!");

                return feedItems;
            }

            protected void onPostExecute(FeedItem[] feedItems){
                //TextView textView =(TextView) findViewById(R.id.feedItemTextView);

                //textView.setText(feedItemRead.getFeed_item().getTitle());

                System.out.println("Terminamos de obtener los feeds");
            }
        };


        /*String url = "http://" + MainActivity.DOMAIN + "/feed_item_reads/" +
                pkFeed +
                ".json";*/

        new RetrieveFeedItems().execute(feedLink);
    }
}
