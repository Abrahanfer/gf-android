package me.abrahanfer.geniusfeed;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.internal.view.SupportMenuItem;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;


import org.springframework.http.HttpAuthentication;
import org.springframework.http.HttpBasicAuthentication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.abrahanfer.geniusfeed.models.Category;
import me.abrahanfer.geniusfeed.models.DRResponseModels.FeedDRResponse;
import me.abrahanfer.geniusfeed.models.DRResponseModels.FeedItemReadDRResponse;
import me.abrahanfer.geniusfeed.models.Feed;
import me.abrahanfer.geniusfeed.models.FeedItemRead;
import me.abrahanfer.geniusfeed.utils.FeedArrayAdapter;
import me.abrahanfer.geniusfeed.utils.Authentication;
import me.abrahanfer.geniusfeed.utils.network.FeedSourceGetter;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;
    private Toolbar mToolbar;
    private NavigationView mNvDrawer;
    private ProgressDialog mProgressDialog;

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

        selectDrawerItem(mNvDrawer.getMenu().getItem(0));
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
        Boolean mainTimeFrameFeeds = true;
        Class fragmentClass;
        switch (menuItem.getItemId()) {
            // Show only this timeframes feeds
            case R.id.nav_feed_list_timeframe:
                fragmentClass = FeedListFragment.class;
                break;
            // Show All Feeds
            case R.id.nav_feed_list:
                fragmentClass = FeedListFragment.class;
                mainTimeFrameFeeds = false;
                break;
            case R.id.nav_login:
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                return;
            default:
                fragmentClass = FeedListFragment.class;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
            ((FeedListFragment) fragment).setTimeframeFeeds(mainTimeFrameFeeds);
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

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_actions, menu);
        return true;
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


    public void showAddCategoriesDialog(List<Category> categories, com.einmalfel.earl.Feed feedInfo, FeedListUpdater
            updateHelper) {
        //FragmentTransaction ft = getFragmentManager().beginTransaction();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        AddCategoriesDialog newFragment = AddCategoriesDialog.newInstance();
        // Setting helper to update values after operation
        Category cat1 = new Category("category1");
        Category cat2 = new Category("category2");

        List<Category> categoriesAux = new ArrayList<Category>();
        categoriesAux.add(cat1);
        categoriesAux.add(cat2);

        newFragment.setCategories(categoriesAux);
        newFragment.setFeedInfo(feedInfo);
        newFragment.setUpdateHelper(updateHelper);

        newFragment.show(ft, "dialog");
    }

    public void showProgressDialog() {
        mProgressDialog = new ProgressDialog(this,R.style.ProgressDialog);
        String messageForDialog = getResources().getString(R.string.searching_feed_msg) + "...";
        mProgressDialog.setMessage(messageForDialog);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        mProgressDialog.show();
    }

    public void stopProgressDialog() {
        mProgressDialog.dismiss();
    }

    public void showAlertMessages(int errorCode) {
        int alertMessage;

        switch (errorCode) {
            case 0:
                alertMessage = R.string.error_creating_feed;
                break;
            case 1:
                alertMessage = R.string.error_url_malformed;
                break;
            default:
                alertMessage = R.string.error_creating_feed;
        }


        // Print alert on mainThread
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(alertMessage)
               .setCancelable(false)
               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // Dismiss dialog
                       //dialog.dismiss();
                   }
               });

        builder.create().show();
    }

}
