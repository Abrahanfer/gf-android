package me.abrahanfer.geniusfeed;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

public class MainActivity extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;
    private Toolbar mToolbar;
    private NavigationView mNvDrawer;

    public final static String FEED = "me.abrahanfer.geniusfeed" +
            ".FEED";
    public final static String LOGIN_CREDENTIALS =
            "me.abrahanfer.geniusfeed" + ".LOGIN_CREDENTIALS";
    // public final static String DOMAIN = "10.0.240.29";
    public final static String DOMAIN = "192.168.1.55";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set a Toolbar to replace the ActionBar.
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable
                                                           .ic_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerLayout = (DrawerLayout) findViewById(
                R.id.drawer_layout);

        // Find our drawer view
        mNvDrawer = (NavigationView) findViewById(
                R.id.navigationView);

        // Setup drawer view
        setupDrawerContent(mNvDrawer);





        View headerLayout = mNvDrawer.getHeaderView(0);
        // Custom methods to populate list of feeds
        // setupListFeeds();
        // testRequest();
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(
                            MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        Fragment fragment = null;
        Class fragmentClass;
        switch (menuItem.getItemId()) {
            case R.id.nav_feed_list:
                fragmentClass = FeedListFragment.class;
                break;
            default:
                fragmentClass = FeedListFragment.class;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                       .replace(R.id.flContent, fragment).commit();

        // Highlight the selected item has been done by NavigationView
        menuItem.setChecked(true);
        // Set action bar title
        setTitle(menuItem.getTitle());
        // Close the navigation drawer
        mDrawerLayout.closeDrawers();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void testRequest() {
        // Make a request to API
        // Instantiate the RequestQueue.

        class RetrieveFeeds extends AsyncTask<String, Void, Feed[]> {
            private Exception exception;

            @Override
            protected Feed[] doInBackground(String... urls) {

                Authentication authentication = Authentication
                        .getCredentials();
                if (authentication == null) {
                    Intent intent = new Intent(
                            getApplicationContext(),
                            LoginActivity.class);


                    startActivity(intent);
                    return new Feed[0];
                } else {
                    String username = authentication.getUsername();
                    String password = authentication.getPassword();


                    // Adding header for Basic HTTP Authentication
                    HttpAuthentication authHeader
                            = new HttpBasicAuthentication(username,
                                                          password);
                    HttpHeaders requestHeaders = new HttpHeaders();
                    requestHeaders.setAuthorization(authHeader);
                    HttpEntity<?> requestEntity
                            = new HttpEntity<Object>(requestHeaders);

                    RestTemplate restTemplate = new RestTemplate();

                    restTemplate.getMessageConverters()
                                .add(new MappingJackson2HttpMessageConverter());
                    try {
                        HttpEntity<FeedDRResponse> response
                                = restTemplate
                                .exchange(urls[0], HttpMethod.GET,
                                          requestEntity,
                                          FeedDRResponse.class);

                        FeedDRResponse result = response.getBody();
                        Feed[] feeds = result.getResults();
                        System.out.println(
                                "Array size: " + feeds.length);

                        return feeds;
                    } catch (RestClientException springException) {
                        System.out.println(
                                "Mirando la excepcion de Spring");
                        System.out.println(springException);
                        return new Feed[0];
                    }
                }
            }

            protected void onPostExecute(Feed[] feeds) {
                System.out.println(
                        "Mirando en onPostExecute 2" + feeds +
                                "Tantos feed");
                ArrayList<Feed> feedArrayList = new ArrayList<>(
                        Arrays.asList(feeds));

               /* ListView listFeeds = (ListView) findViewById(
                        R.id.listFeeds);
               / FeedArrayAdapter feedArrayAdapter
                        = new FeedArrayAdapter(
                        getApplicationContext(), feedArrayList);

                listFeeds.setAdapter(feedArrayAdapter);*/
            }
        }


        String url = "http://" + DOMAIN + "/feeds";

        new RetrieveFeeds().execute(url);
    }

    public void setupListFeeds() {
       /* final ListView listFeeds = (ListView) findViewById(
                R.id.listFeeds);

        listFeeds.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent,
                                            View view, int position,
                                            long id) {
                        System.out.println("Feed click" + position);
                        Intent intent = new Intent(
                                getApplicationContext(),
                                FeedActivity.class);
                        Feed feed = (Feed) listFeeds.getAdapter()
                                                    .getItem(
                                                            position);
                        intent.putExtra(FEED,
                                        feed.getLink().toString());

                        startActivity(intent);
                    }
                });*/
    }
}
