package me.abrahanfer.geniusfeed;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.AttrRes;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.BatchUpdateException;
import java.util.Iterator;
import java.util.List;

import me.abrahanfer.geniusfeed.models.DRResponseModels.FeedItemReadDRResponse;
import me.abrahanfer.geniusfeed.models.Feed;
import me.abrahanfer.geniusfeed.models.FeedItemAtom;
import me.abrahanfer.geniusfeed.models.FeedItemRSS;
import me.abrahanfer.geniusfeed.models.FeedItemRead;
import me.abrahanfer.geniusfeed.utils.Authentication;
import me.abrahanfer.geniusfeed.utils.Constants;
import me.abrahanfer.geniusfeed.utils.network.GeniusFeedService;
import me.abrahanfer.geniusfeed.utils.network.NetworkServiceBuilder;
import me.abrahanfer.geniusfeed.utils.network.bodyclass.FIReadUpdateBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedItemActivity extends AppCompatActivity {

    private String feedItemType;
    private String mFeedParentURL;
    private String mFeedParentPK;
    private FeedItemRead mFeedItemRead;
    private Feed mFeed;

    // Defining interface for CallbackError
    interface FeedItemReadErrorCallback {
        public void onError();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_item);

        // Set title in collapsingToolbarLayout
        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle("Entry");

        // Set toolbar as support action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        feedItemType = getIntent()
                .getStringExtra(FeedActivity.FEED_ITEM_TYPE);

        mFeedItemRead = getIntent()
                .getParcelableExtra(FeedActivity.FEED_ITEM_READ);

        // Set as read the item
        mFeedItemRead.setRead(true);
        final String token = Authentication.getCredentials().getToken();
        GeniusFeedService service = NetworkServiceBuilder.createService(GeniusFeedService.class, token);

        Call<FeedItemRead> call = service.partialUpdateFeedItemRead(mFeedItemRead.getPk(), new
                FIReadUpdateBody(mFeedItemRead.getRead(), mFeedItemRead.getFav()));


        call.enqueue(new Callback<FeedItemRead>() {
            @Override
            public void onResponse(Call<FeedItemRead> call, Response<FeedItemRead> response) {
                if(response.isSuccessful()) {
                    Log.d("SUCCESS RESPONSE", response.body().toString());
                } else {

                }
            }

            @Override
            public void onFailure(Call<FeedItemRead> call, Throwable t) {
                Log.e("FAILURE RESPONSE", t.toString());
            }
        });

        if(feedItemType.equalsIgnoreCase("Atom")){
            FeedItemAtom feedItemAtom = (FeedItemAtom) mFeedItemRead.getFeed_item();
            mFeedParentURL = feedItemAtom.getFeed().getLink().toString();
            mFeedParentPK = feedItemAtom.getFeed().getPk();
            mFeed = feedItemAtom.getFeed();
           if(feedItemAtom.getType().equalsIgnoreCase("html")) {
                /*TextView textView = (TextView) findViewById(R.id.feedItemTextView);
                textView.setText(Html.fromHtml(feedItemAtom.getValue()));*/
               WebView webView = (WebView) findViewById(R.id.feedItemWebView);
               webView.setVisibility(WebView.VISIBLE);
               webView.loadDataWithBaseURL(null, wrapHtml(webView.getContext(), feedItemAtom.getValue()),
                                           "text/html", "UTF-8", null);
            }
        }else{
            if(feedItemType.equalsIgnoreCase("RSS")){
                FeedItemRSS feedItemRSS = getIntent()
                        .getParcelableExtra(FeedActivity.FEED_ITEM);
                mFeedParentURL = feedItemRSS.getFeed().getLink().toString();
                mFeedParentPK = feedItemRSS.getFeed().getPk();
                mFeed = feedItemRSS.getFeed();
                /*TextView textView = (TextView) findViewById(R.id.feedItemTextView);
                textView.setText(Html.fromHtml(feedItemRSS.getDescription()));*/
                WebView webView = (WebView) findViewById(R.id.feedItemWebView);
                webView.setVisibility(WebView.VISIBLE);
                webView.loadDataWithBaseURL(null, wrapHtml(webView.getContext(), feedItemRSS.getDescription()),
                                            "text/html", "UTF-8", null);
                Log.e("Mirando esto", "Enclosures " + feedItemRSS.getEnclosureLength().toString());
                if(feedItemRSS.getEnclosureLength().intValue() > 0){
                    ImageView imageView = (ImageView) findViewById(R.id.feedItemImageView);
                    try {
                        URL url = new URL(feedItemRSS.getEnclosureURL());
                        Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                        imageView.setImageBitmap(bmp);
                        imageView.setVisibility(ImageView.VISIBLE);
                    }catch (MalformedURLException e){
                        Log.e("FEED_ITEM_ACTIVITY", "Malformed URL");
                    }catch (IOException e){
                        Log.e("FEED_ITEM_ACTIVITY", "IOException");
                    }
                }
            }
        }

        Button viewOnSource = (Button) findViewById(R.id.viewOnSource);
        viewOnSource.setVisibility(Button.VISIBLE);
        viewOnSource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewOnSourceAction();
            }
        });
    }

    private void viewOnSourceAction() {
        String url = mFeedItemRead.getFeed_item().getLink();
        if (url.startsWith("http://") || url.startsWith("https://")) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }
    }

    private String wrapHtml(Context context, String html) {
        return context.getString(R.string.html_wrap, html);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_actions_feed_item, menu);

        final FeedItemRead itemRead = mFeedItemRead;


        for (int i = 0; i < menu.size(); i++) {
            if (menu.getItem(i).getItemId() == R.id.favoriteAction && mFeedItemRead.getFav())  {
                menu.getItem(i).setIcon(R.drawable.ic_favorite_white_24dp);
            }
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == android.R.id.home) {
            Intent upIntent = NavUtils.getParentActivityIntent(this);
            upIntent.putExtra(FeedListFragment.FEED_PK, mFeedParentPK);
            upIntent.putExtra(FeedListFragment.FEED_LINK, mFeedParentURL);
            // set Feed as extra FeedListFragment.FEED_API
            upIntent.putExtra(FeedListFragment.FEED_API, mFeed);
            NavUtils.navigateUpTo(this, upIntent);

            return true;
        }

        if (id == R.id.favoriteAction) {
            // Set ToolbarButton as checked/unchecked
            if (mFeedItemRead.getFav()){
                item.setIcon(R.drawable.ic_favorite_border_white_24dp);
            } else {
                item.setIcon(R.drawable.ic_favorite_white_24dp);
            }

            // Send request to mark feedItemRead as fav/unfav
            mFeedItemRead.setFav(!mFeedItemRead.getFav());
            putChangesForFeedItemRead(new FeedItemReadErrorCallback() {
                @Override
                public void onError() {
                    // Launch snackbar to info error
                    mFeedItemRead.setFav(!mFeedItemRead.getFav());
                    if (mFeedItemRead.getFav()){
                        item.setIcon(R.drawable.ic_favorite_white_24dp);
                    } else {
                        item.setIcon(R.drawable.ic_favorite_border_white_24dp);
                    }
                }
            });
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



                    FeedItemRead feedItemRead =  response.getBody();

                    System.out.println("GOOD REQUEST!!!");

                    return feedItemRead;
                }

                protected void onPostExecute(FeedItemRead feedItemRead){

                }
            }

            String url = Constants.getHostByEnviroment() +
                    "/feed_item_reads/" +
                    pkFeedItem +
                    ".json";

            new RetrieveFeedItem().execute(url);
        }

    private void putChangesForFeedItemRead(final FeedItemReadErrorCallback errorCallback) {
        final String token = Authentication.getCredentials().getToken();

        GeniusFeedService service = NetworkServiceBuilder.createService(GeniusFeedService.class, token);

        Call<FeedItemRead> call = service.partialUpdateFeedItemRead(mFeedItemRead.getPk() ,new FIReadUpdateBody
                (mFeedItemRead.getRead(),
                                                                                         mFeedItemRead.getFav()));

        call.enqueue(new Callback<FeedItemRead>() {
            @Override
            public void onResponse(Call<FeedItemRead> call, Response<FeedItemRead> response) {
                if (response.isSuccessful()) {
                    // All ok
                } else {
                    // Feedback for bad code response
                    errorCallback.onError();
                }
            }

            @Override
            public void onFailure(Call<FeedItemRead> call, Throwable t) {
                // Send Feedback to user
                errorCallback.onError();
            }
        });
    }
}
