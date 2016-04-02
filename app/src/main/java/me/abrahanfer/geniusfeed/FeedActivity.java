package me.abrahanfer.geniusfeed;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.einmalfel.earl.AtomEntry;
import com.einmalfel.earl.AtomFeed;
import com.einmalfel.earl.EarlParser;
import com.einmalfel.earl.RSSFeed;
import com.einmalfel.earl.RSSItem;

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
import java.util.ArrayList;
import java.util.zip.DataFormatException;

import me.abrahanfer.geniusfeed.models.Feed;
import me.abrahanfer.geniusfeed.models.FeedItem;
import me.abrahanfer.geniusfeed.models.FeedItemAtom;
import me.abrahanfer.geniusfeed.models.FeedItemRSS;
import me.abrahanfer.geniusfeed.models.FeedItemRead;
import me.abrahanfer.geniusfeed.utils.FeedItemsArrayAdapter;

/**
 * Created by abrahan on 19/03/16.
 */
public class FeedActivity extends AppCompatActivity {
    public final static String FEED_ITEM = "me.abrahanfer.geniusfeed" +
            ".FEED_ITEM";
    public final static String FEED_ITEM_TYPE = "me.abrahanfer.geniusfeed" +
            ".FeedItemType";
    final static public String EARL_TAG = "EarlFeedUtil";
    final static public String FEED_ACTIVITY_TAG = "EarlFeedUtil";
    private com.einmalfel.earl.Feed mFeed;
    private URL feedLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        try {
            feedLink = new URL(getIntent()
                    .getStringExtra(FeedListFragment.FEED));
        } catch (MalformedURLException exception) {
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
                ArrayList<FeedItem> > {
            private Exception exception;

            @Override
            protected ArrayList<FeedItem> doInBackground(URL... url) {
                InputStream inputStream = null;
                com.einmalfel.earl.Feed feed = null;
                Log.i(EARL_TAG, "Mirando link" + url);
                try {
                    inputStream = feedLink.openConnection().getInputStream();
                }catch (IOException exception) {
                    Log.d(EARL_TAG, "Exception IO");
                    return new ArrayList<FeedItem>();
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
                mFeed = feed;
                ArrayList<FeedItem> feedItems = new ArrayList<>();
                if (RSSFeed.class.isInstance(feed)) {
                    RSSFeed rssFeed = (RSSFeed) feed;
                    for (RSSItem rssItem : rssFeed.items) {
                        String title = rssItem.getTitle();
                        Log.i(EARL_TAG, "RSS title: " + (title == null ? "N/A" : title));
                        feedItems.add(new FeedItemRSS(rssItem));
                    }
                }else {
                    if (AtomFeed.class.isInstance(feed)){
                        AtomFeed atomFeed = (AtomFeed) feed;
                        for (AtomEntry atomEntry: atomFeed.entries) {
                            String title = atomEntry.getTitle();
                            Log.i(EARL_TAG, "Atom title: " + (title == null ? "N/A" : title));
                            feedItems.add(new FeedItemAtom(atomEntry));
                        }
                    }
                }

                return feedItems;
            }

            protected void onPostExecute(ArrayList<FeedItem> feedItems){
                TextView textView =(TextView) findViewById(R.id.feedTextView);
                if (mFeed != null) {
                    textView.setText(mFeed.getTitle());
                }

                ListView listFeedItems =(ListView) findViewById(R.id.listFeedItems);
                FeedItemsArrayAdapter feedItemsArrayAdapter = new
                        FeedItemsArrayAdapter(getApplicationContext(),
                                         feedItems);

                listFeedItems.setAdapter(feedItemsArrayAdapter);
                Log.d(FEED_ACTIVITY_TAG, "Terminamos de obtener los feeds");
                setupListFeeds();
            }
        }

        new RetrieveFeedItems().execute(feedLink);
    }

    public void setupListFeeds(){
        final ListView listFeeds = (ListView) findViewById(R.id.listFeedItems);

        listFeeds.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        Log.d(FEED_ACTIVITY_TAG, "Feed Item click" + position);
                        Intent
                                intent = new Intent(getApplicationContext(), FeedItemActivity
                                .class);
                        FeedItem feedItem =(FeedItem) listFeeds
                                .getAdapter()
                                .getItem(position);
                        intent.putExtra(FEED_ITEM_TYPE,
                                        FeedItemAtom.class.isInstance(feedItem) ? "Atom" : "RSS");
                        intent.putExtra(FEED_ITEM, feedItem);

                        startActivity(intent);
                    }
                }
        );
    }
}
