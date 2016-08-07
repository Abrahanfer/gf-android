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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
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
import me.abrahanfer.geniusfeed.utils.DividerItemDecoration;
import me.abrahanfer.geniusfeed.utils.FeedArrayAdapter;
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
    public final static String FEED_ITEM_READ = "me.abrahanfer.geniusfeed" +
            ".FeedItemRead";
    final static public String EARL_TAG = "EarlFeedUtil";
    final static public String FEED_ACTIVITY_TAG = "FeedActivity";
    private com.einmalfel.earl.Feed mFeed;
    private URL feedLink;
    private String mFeedPk;
    private ProgressBar mProgressBar;
    private List<FeedItemRead> mSourceItems;
    private Feed mFeedAPI;
    private RecyclerView mListFeedItems;
    private RecyclerView mFeedList;

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



        if (feedLink != null) {
            // Set up progressBar
            mProgressBar = (ProgressBar) findViewById(R.id.pbLoading);
            mProgressBar.setVisibility(ProgressBar.VISIBLE);

            getFeedItemsFromFeed(feedLink);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_actions, menu);
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
                ArrayList<FeedItemRead> > {
            private Exception exception;

            @Override
            protected ArrayList<FeedItemRead> doInBackground(URL... url) {
                InputStream inputStream = null;
                com.einmalfel.earl.Feed feed = null;
                Log.i(EARL_TAG, "Mirando link" + url);
                try {
                    inputStream = feedLink.openConnection().getInputStream();
                }catch (IOException exception) {
                    Log.d(EARL_TAG, "Exception IO");
                    return new ArrayList<FeedItemRead>();
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
                ArrayList<FeedItemRead> feedItems = new ArrayList<>();
                if (RSSFeed.class.isInstance(feed)) {
                    RSSFeed rssFeed = (RSSFeed) feed;
                    for (RSSItem rssItem : rssFeed.items) {
                        String title = rssItem.getTitle();
                        Log.i(EARL_TAG, "RSS title: " + (title == null ? "N/A" : title));
                        FeedItemRead feedItemRead = new FeedItemRead(false, false, new FeedItemRSS(rssItem));
                        feedItems.add(feedItemRead);
                    }
                }else {
                    if (AtomFeed.class.isInstance(feed)){
                        AtomFeed atomFeed = (AtomFeed) feed;
                        for (AtomEntry atomEntry: atomFeed.entries) {
                            String title = atomEntry.getTitle();
                            Log.i(EARL_TAG, "Atom title: " + (title == null ? "N/A" : title));
                            FeedItemRead feedItemRead = new FeedItemRead(false, false, new FeedItemAtom(atomEntry));
                            feedItems.add(feedItemRead);
                        }
                    }
                }
                mSourceItems = feedItems;
                return (ArrayList<FeedItemRead>) mSourceItems;
            }

            protected void onPostExecute(ArrayList<FeedItemRead> feedItemReads){
                TextView textView =(TextView) findViewById(R.id.feedTextView);
                if (mFeed != null) {
                    textView.setText(mFeed.getTitle());
                }

                // Sort FeedItem objects by publication date
                Collections.sort(feedItemReads, new
                        Comparator<FeedItemRead>()   {
                            @Override
                            public int compare(FeedItemRead feedItemRead1,
                                               FeedItemRead feedItemRead2)
                            {

                                return  feedItemRead1.getFeed_item()
                                        .getPublicationDate()
                                        .compareTo(feedItemRead2.getFeed_item()
                                                   .getPublicationDate()) * -1;
                            }
                        });

               /* ListView listFeedItems =(ListView) findViewById(R.id.listFeedItems);
                FeedItemsArrayAdapter feedItemsArrayAdapter = new
                        FeedItemsArrayAdapter(getApplicationContext(),
                                         feedItemReads);



                listFeedItems.setAdapter(feedItemsArrayAdapter);*/

                RecyclerView feedItemsList = (RecyclerView) findViewById(R.id.feed_items_list);
                feedItemsList.setHasFixedSize(true);

                // use a linear layout manager
                LinearLayoutManager layoutManager = new LinearLayoutManager(getBaseContext());
                feedItemsList.setLayoutManager(layoutManager);

                RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(feedItemsList.getContext(),
                                                                                       DividerItemDecoration
                                                                                               .VERTICAL_LIST);

                feedItemsList.addItemDecoration(itemDecoration);

                FeedItemsArrayAdapter feedItemsArrayAdapter
                        = new FeedItemsArrayAdapter(feedItemReads);

                mProgressBar.setVisibility(ProgressBar
                                                   .INVISIBLE);
                //listFeeds.setAdapter(feedArrayAdapter);

                feedItemsList.setAdapter(feedItemsArrayAdapter);


                Log.d(FEED_ACTIVITY_TAG, "Get all feed items " +
                        "completed");
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                // setupListFeeds();
                // Call to get list fo feedItemRead from API
                getFeedItemReadList(new ArrayList<FeedItemRead>(), 1);
            }
        }

        new RetrieveFeedItems().execute(feedLink);
    }
    public void getFeedItemReadList(final List<FeedItemRead> feedItemList, final int page) {
        final String token = Authentication.getCredentials().getToken();

        mProgressBar.setVisibility(ProgressBar.VISIBLE);
        // Set listview as invisible
        mListFeedItems =(RecyclerView) findViewById(R.id.feed_items_list);
        mListFeedItems.setVisibility(ListView.INVISIBLE);
        GeniusFeedService service = NetworkServiceBuilder.createService(GeniusFeedService.class, token);

        Call<FeedItemReadDRResponse> call = service.getFeedItemReads(mFeedPk, page);

        call.enqueue(new Callback<FeedItemReadDRResponse>() {
            @Override
            public void onResponse(Call<FeedItemReadDRResponse> call, Response<FeedItemReadDRResponse> response) {
                if (response.isSuccessful()) {
                    List<FeedItemRead> feedItemReadList =  response.body().getResults();
                    // Sync feeditemreads from API with new items of Feed Source
                    for(Iterator<FeedItemRead> iterator = feedItemReadList.iterator(); iterator.hasNext();) {
                        FeedItemRead item = iterator.next();
                        feedItemList.add(item);
                    }

                    if(response.body().getNext() != null) {
                        getFeedItemReadList(feedItemList, page + 1);
                    }else{
                        List<FeedItemRead> itemsToPost = new ArrayList<FeedItemRead>();
                        for(Iterator<FeedItemRead> iterator = mSourceItems.iterator(); iterator.hasNext();) {
                            FeedItemRead item = iterator.next();
                            if(!feedItemList.contains(item)) {
                                itemsToPost.add(item);
                            }else{
                                int index = feedItemList.indexOf(item);
                                item.setPk(feedItemList.get(index).getPk());
                                item.setRead(feedItemList.get(index).getRead());
                                item.setFav(feedItemList.get(index).getFav());
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
       /* final ListView listFeeds = (ListView) findViewById(R.id.listFeedItems);



        Log.e("Mirando Feed Activity", "ListView activity " + listFeeds);
        listFeeds.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        Log.d(FEED_ACTIVITY_TAG, "Feed Item click" + position);
                        Intent intent = new Intent(getApplicationContext(), FeedItemActivity
                                .class);
                        FeedItemRead feedItemRead =(FeedItemRead) listFeeds
                                .getAdapter()
                                .getItem(position);
                        feedItemRead.getFeed_item().setFeed(mFeedAPI);
                        intent.putExtra(FEED_ITEM_TYPE,
                                        FeedItemAtom.class.isInstance(feedItemRead.getFeed_item()) ? "Atom" : "RSS");
                        intent.putExtra(FEED_ITEM, feedItemRead.getFeed_item());
                        intent.putExtra(FEED_ITEM_READ, feedItemRead);


                        startActivity(intent);
                    }
                }
        );*/

        RecyclerView feedItemsListView = (RecyclerView) findViewById(R.id.feed_items_list);

        ItemClickSupport.addTo(feedItemsListView).setOnItemClickListener(
                new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        Intent intent = new Intent(getApplicationContext(), FeedItemActivity
                                .class);
                        FeedItemRead feedItemRead = mSourceItems.get(position);
                        feedItemRead.getFeed_item().setFeed(mFeedAPI);
                        intent.putExtra(FEED_ITEM_TYPE,
                                        FeedItemAtom.class.isInstance(feedItemRead.getFeed_item()) ? "Atom" : "RSS");
                        intent.putExtra(FEED_ITEM, feedItemRead.getFeed_item());
                        intent.putExtra(FEED_ITEM_READ, feedItemRead);


                        startActivity(intent);
                    }
                }
        );
    }

    public void postAllNewItemsAsNonRead(List<FeedItemRead> feedItems) {
        final String token = Authentication.getCredentials().getToken();

       GeniusFeedService service = NetworkServiceBuilder.createService(GeniusFeedService.class, token);
        if (feedItems.size() > 0) {
            FeedItemRead firstToPost = feedItems.get(0);
            feedItems.remove(0);
            postOneFeedItemRead(service,firstToPost,feedItems);
        }else{
            mProgressBar.setVisibility(ProgressBar.INVISIBLE);
            // Set listview as visible
            mListFeedItems.setVisibility(ListView.VISIBLE);

            /*ListView listFeedItems = (ListView) findViewById(R.id.listFeedItems);
            ((ArrayAdapter)listFeedItems.getAdapter()).notifyDataSetChanged();*/
            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.feed_items_list);
            recyclerView.getAdapter().notifyDataSetChanged();

        }
    }

    private void postOneFeedItemRead(final GeniusFeedService service, FeedItemRead item, final List<FeedItemRead>
            feedItemsToPost) {

        item.getFeed_item().setFeed(mFeedAPI);
        Call<FeedItemRead> call = service.createFeedItemRead(item);

        call.enqueue(new Callback<FeedItemRead>() {
            @Override
            public void onResponse(Call<FeedItemRead> call, Response<FeedItemRead> response) {
                if(response.isSuccessful()){
                    for(Iterator<FeedItemRead> iterator = mSourceItems.iterator(); iterator.hasNext();) {
                        FeedItemRead item = iterator.next();
                        FeedItemRead newItem = response.body();
                        if (item.equals(newItem)) {
                            item.setPk(newItem.getPk());
                        }
                    }
                }else{
                    //bad code response
                }
                if(feedItemsToPost.size() > 0) {
                    FeedItemRead feedItem = feedItemsToPost.get(0);
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
