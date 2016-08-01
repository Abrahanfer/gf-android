/*
 * Copyright (C) 2016 Abrahán Fernández Nieto
 *
 * TODO License
 */

package me.abrahanfer.geniusfeed;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.zip.DataFormatException;

import me.abrahanfer.geniusfeed.models.DRResponseModels.FeedDRResponse;
import me.abrahanfer.geniusfeed.models.DRResponseModels.FeedItemReadDRResponse;
import me.abrahanfer.geniusfeed.models.Feed;
import me.abrahanfer.geniusfeed.models.FeedItem;
import me.abrahanfer.geniusfeed.models.FeedItemAtom;
import me.abrahanfer.geniusfeed.models.FeedItemRSS;
import me.abrahanfer.geniusfeed.models.FeedItemRead;
import me.abrahanfer.geniusfeed.utils.Authentication;
import me.abrahanfer.geniusfeed.utils.Constants;
import me.abrahanfer.geniusfeed.utils.FeedItemsArrayAdapter;
import me.abrahanfer.geniusfeed.utils.network.GeniusFeedService;
import me.abrahanfer.geniusfeed.utils.network.NetworkServiceBuilder;
import me.abrahanfer.geniusfeed.utils.network.bodyclass.Token;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Class to handle activity to show items from feed
 */
public class FeedActivity extends AppCompatActivity {
    public final static String FEED_ITEM = "me.abrahanfer.geniusfeed" +
            ".FEED_ITEM";
    public final static String FEED_ITEM_TYPE = "me.abrahanfer.geniusfeed" +
            ".FeedItemType";
    final static public String EARL_TAG = "EarlFeedUtil";
    final static public String FEED_ACTIVITY_TAG = "FeedActivity";
    private com.einmalfel.earl.Feed mFeed;
    private URL feedLink;
    private String mFeedPk;
    private ProgressBar mProgressBar;
    private List<FeedItem> mSourceItems;
    private Feed mFeedAPI;
    private ListView mListFeedItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_feed);

        mFeedPk = getIntent().getStringExtra(FeedListFragment.FEED_PK);
        mFeedAPI = getIntent().getParcelableExtra(FeedListFragment.FEED_API);
        try {
            feedLink = new URL(getIntent()
                    .getStringExtra(FeedListFragment.FEED_LINK));
        } catch (MalformedURLException exception) {
            Log.e(FEED_ACTIVITY_TAG,"BAD URL FORMAT");
        }

        TextView textView =
                (TextView) findViewById(R.id.feedTextView);
        textView.setText("");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Prueba");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set up progressBar
        mProgressBar = (ProgressBar) findViewById(R.id.pbLoading);
        mProgressBar.setVisibility(ProgressBar.VISIBLE);

        getFeedItemsFromFeed(feedLink);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_feed, menu);
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
                mSourceItems = feedItems;
                return feedItems;
            }

            protected void onPostExecute(ArrayList<FeedItem> feedItems){
                TextView textView =(TextView) findViewById(R.id.feedTextView);
                if (mFeed != null) {
                    textView.setText(mFeed.getTitle());
                }

                // Sort FeedItem objects by publication date
                Collections.sort(feedItems, new
                        Comparator<FeedItem>()   {
                            @Override
                            public int compare(FeedItem feedItem1,
                                               FeedItem feedItem2)
                            {

                                return  feedItem1
                                        .getPublicationDate()
                                        .compareTo(feedItem2
                                                   .getPublicationDate()) * -1;
                            }
                        });

                ListView listFeedItems =(ListView) findViewById(R.id.listFeedItems);
                FeedItemsArrayAdapter feedItemsArrayAdapter = new
                        FeedItemsArrayAdapter(getApplicationContext(),
                                         feedItems);

                listFeedItems.setAdapter(feedItemsArrayAdapter);
                Log.d(FEED_ACTIVITY_TAG, "Get all feed items " +
                        "completed");
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                // setupListFeeds();
                // Call to get list fo feedItemRead from API
                getFeedItemReadList(new ArrayList<FeedItem>(), 1);
            }
        }

        new RetrieveFeedItems().execute(feedLink);
    }
    public void getFeedItemReadList(final List<FeedItem> feedItemList, final int page) {
        final String token = Authentication.getCredentials().getToken();

        mProgressBar.setVisibility(ProgressBar.VISIBLE);
        // Set listview as invisible
        mListFeedItems =(ListView) findViewById(R.id.listFeedItems);
        mListFeedItems.setVisibility(ListView.INVISIBLE);
        GeniusFeedService service = NetworkServiceBuilder.createService(GeniusFeedService.class, token);

        Call<FeedItemReadDRResponse> call = service.getFeedItemReads(mFeedPk, page);

        call.enqueue(new Callback<FeedItemReadDRResponse>() {
            @Override
            public void onResponse(Call<FeedItemReadDRResponse> call, Response<FeedItemReadDRResponse> response) {
                if (response.isSuccessful()) {
                    List<FeedItemRead> feedItemReadList =  response.body().getResults();
                    // TODO Sync feeditemreads from API with new items of Feed Source
                    for(Iterator<FeedItemRead> iterator = feedItemReadList.iterator(); iterator.hasNext();) {
                        FeedItemRead item = iterator.next();
                        feedItemList.add(item.getFeed_item());
                    }

                    if(response.body().getNext() != null) {
                        getFeedItemReadList(feedItemList, page + 1);
                    }else{
                        List<FeedItem> itemsToPost = new ArrayList<FeedItem>();
                        for(Iterator<FeedItem> iterator = mSourceItems.iterator(); iterator.hasNext();) {
                            FeedItem item = iterator.next();
                            if(!feedItemList.contains(item)) {
                                itemsToPost.add(item);
                            }
                        }
                        postAllNewItemsAsNonRead(itemsToPost);
                        setupListFeeds();
                    }
                }else{
                    // error response, no access to resource?
                    Log.e("ERROR FEED ITEM LIST", "error in response, with bad http code");
                }
            }

            @Override
            public void onFailure(Call<FeedItemReadDRResponse> call, Throwable t) {
                // something went completely south (like no internet connection)
                Log.e("Error Login RETROFIT", t.getMessage());
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
            }
        });
    }

    public void setupListFeeds(){
        final ListView listFeeds = (ListView) findViewById(R.id.listFeedItems);



        Log.e("Mirando Feed Activity", "ListView activity " + listFeeds);
        listFeeds.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        Log.d(FEED_ACTIVITY_TAG, "Feed Item click" + position);
                        Intent intent = new Intent(getApplicationContext(), FeedItemActivity
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

    public void postAllNewItemsAsNonRead(List<FeedItem> feedItems) {
        final String token = Authentication.getCredentials().getToken();

       GeniusFeedService service = NetworkServiceBuilder.createService(GeniusFeedService.class, token);
        if (feedItems.size() > 0) {
            FeedItem firstToPost = feedItems.get(0);
            feedItems.remove(0);
            postOneFeedItemRead(service,firstToPost,feedItems);
        }else{
            mProgressBar.setVisibility(ProgressBar.INVISIBLE);
            // Set listview as visible
            mListFeedItems.setVisibility(ListView.VISIBLE);
        }
    }

    private void postOneFeedItemRead(final GeniusFeedService service, FeedItem item, final List<FeedItem> feedItemsToPost) {

        item.setFeed(mFeedAPI);
        FeedItemRead newFeedItemRead = new FeedItemRead(false, false, item);
        Call<FeedItemRead> call = service.createFeedItemRead(newFeedItemRead);

        call.enqueue(new Callback<FeedItemRead>() {
            @Override
            public void onResponse(Call<FeedItemRead> call, Response<FeedItemRead> response) {
                if(response.isSuccessful()){
                    Log.e("GOOD respondes", "Mirando las respuesta " + response.toString());
                }else{
                    //bad code response
                }
                if(feedItemsToPost.size() > 0) {
                    FeedItem feedItem = feedItemsToPost.get(0);
                    feedItemsToPost.remove(0);
                    postOneFeedItemRead(service, feedItem, feedItemsToPost);
                }else{
                    mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                    // Set listview as visible
                    mListFeedItems.setVisibility(ListView.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<FeedItemRead> call, Throwable t) {
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                // Set listview as visible
                mListFeedItems.setVisibility(ListView.VISIBLE);
            }
        });
    }
}
