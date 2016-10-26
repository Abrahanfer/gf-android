package me.abrahanfer.geniusfeed;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.internal.view.SupportMenuItem;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.net.URL;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import me.abrahanfer.geniusfeed.models.Category;
import me.abrahanfer.geniusfeed.utils.Authentication;
import me.abrahanfer.geniusfeed.utils.GeniusFeedContract;
import me.abrahanfer.geniusfeed.utils.network.ConnectivityEventsReceiver;

public class MainActivity extends AppCompatActivity implements NetworkStatusFeedbackInterface, SearchView.OnQueryTextListener {
    private DrawerLayout mDrawerLayout;
    private Toolbar mToolbar;
    private NavigationView mNvDrawer;
    private ProgressDialog mProgressDialog;
    private Fragment mCurrentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Register BroadcastReceiver to monitoring network status
        registerReceiver(
                new ConnectivityEventsReceiver(),
                new IntentFilter(
                        ConnectivityManager.CONNECTIVITY_ACTION));

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
        changeLoginMenuItem();

        View headerLayout = mNvDrawer.getHeaderView(0);

        selectDrawerItem(mNvDrawer.getMenu().getItem(0));

        // Realm configuration
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(this).build();
        Realm.setDefaultConfiguration(realmConfig);
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
        mCurrentFragment = null;
        Boolean mainTimeFrameFeeds = true;
        Boolean feedListFragment = true;
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
            // Show fav feed items
            case R.id.nav_fav_feed_list:
                fragmentClass = FavFeedItemListFragment.class;
                feedListFragment = false;
                break;
            // Show fav feed items
            case R.id.recommended_feeds:
                fragmentClass = RecommendedFeedsFragment.class;
                feedListFragment = false;
                break;
            case R.id.nav_login:
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                return;
            case R.id.nav_logout:
                // Remove credentials from database
                removeLocalDataFromDB();
                return;
            default:
                fragmentClass = FeedListFragment.class;
        }

        try {
            mCurrentFragment = (Fragment) fragmentClass.newInstance();
            if (feedListFragment)
                ((FeedListFragment) mCurrentFragment).setTimeframeFeeds(mainTimeFrameFeeds);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                       .replace(R.id.flContent, mCurrentFragment).commit();

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

        final MenuItem searchItem = menu.findItem(R.id.searchAction);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        // Passing query to fragment children
        return ((SearchView.OnQueryTextListener) mCurrentFragment).onQueryTextChange(query);

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        // Passing query to fragment children
        return ((SearchView.OnQueryTextListener) mCurrentFragment).onQueryTextSubmit(query);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                // Change login/logout menu item
                changeLoginMenuItem();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void changeLoginMenuItem() {
        setHeaderInfo();

        // Check about login info
        Authentication credentials = Authentication.getCredentials();
        MenuItem loginMenuItem = (MenuItem) mNvDrawer.getMenu().findItem(R.id.nav_login);
        MenuItem logoutMenuItem = (MenuItem) mNvDrawer.getMenu().findItem(R.id.nav_logout);
        if (credentials != null) {
            if (loginMenuItem != null)
                loginMenuItem.setVisible(false);
            if (logoutMenuItem != null)
                logoutMenuItem.setVisible(true);
        } else {
            if (loginMenuItem != null)
                loginMenuItem.setVisible(true);
            if (logoutMenuItem != null)
                logoutMenuItem.setVisible(false);
        }
    }


    public void showAddCategoriesDialog(List<Category> categories, com.einmalfel.earl.Feed feedInfo, FeedListUpdater
            updateHelper, URL feedSourceURL) {
        //FragmentTransaction ft = getFragmentManager().beginTransaction();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        AddCategoriesDialog newFragment = AddCategoriesDialog.newInstance();
        newFragment.setCategories(categories);
        newFragment.setFeedInfo(feedInfo);
        newFragment.setUpdateHelper(updateHelper);
        newFragment.setFeedSourceURL(feedSourceURL);

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
        if(mProgressDialog != null)
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
            case 3:
                alertMessage = R.string.recommended_feeds_alert_msg;
                break;
            case 5:
                alertMessage = R.string.network_disconnected;
                break;
            case 10:
                alertMessage = R.string.logout_successfully;
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

    public void setHeaderInfo() {
        mNvDrawer = (NavigationView) findViewById(
                R.id.navigationView);
        TextView headerTextView = (TextView) mNvDrawer.findViewById(R.id.headerTextView);
        if(Authentication.getCredentials() != null && headerTextView != null)
            headerTextView.setText(Authentication.getCredentials().getUsername());
    }

    public void removeLocalDataFromDB() {

        // Get DBHelper
        final GeniusFeedContract.GeniusFeedDbHelper mDbHelper =
                new GeniusFeedContract.GeniusFeedDbHelper(getApplicationContext());

        class GetReadableDatabase extends AsyncTask<Void, Void, SQLiteDatabase> {
            @Override
            protected SQLiteDatabase doInBackground(Void... params) {
                return mDbHelper.getWritableDatabase();
            }

            protected void onPostExecute(SQLiteDatabase dataBase) {

                // Define projector
                String[] projection = {
                        GeniusFeedContract.User.COLUMN_NAME_USER_ID,
                        GeniusFeedContract.User.COLUMN_NAME_USERNAME,
                        GeniusFeedContract.User.COLUMN_NAME_TOKEN
                };

                Cursor c = dataBase.query(
                        GeniusFeedContract.User.TABLE_NAME,
                        projection,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "1"
                );
                Log.e("Problem with delete", "c.getCount " + c.getCount());
                if (c.getCount() > 0) {
                    dataBase.delete(GeniusFeedContract.User.TABLE_NAME, null, null);
                    Authentication.deleteCredentials();
                }

                // Get to login view
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }

        }

        new GetReadableDatabase().execute();
    }
}
