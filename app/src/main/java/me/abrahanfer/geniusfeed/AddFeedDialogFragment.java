package me.abrahanfer.geniusfeed;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.einmalfel.earl.AtomCategory;
import com.einmalfel.earl.AtomFeed;
import com.einmalfel.earl.EarlParser;
import com.einmalfel.earl.RSSCategory;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;

import me.abrahanfer.geniusfeed.models.Category;
import me.abrahanfer.geniusfeed.models.DRResponseModels.FeedDRResponse;
import me.abrahanfer.geniusfeed.models.FeedItemRead;
import me.abrahanfer.geniusfeed.models.Feed;
import me.abrahanfer.geniusfeed.utils.Authentication;
import me.abrahanfer.geniusfeed.utils.network.FeedSourceGetter;
import me.abrahanfer.geniusfeed.utils.network.GeniusFeedService;
import me.abrahanfer.geniusfeed.utils.network.NetworkServiceBuilder;
import me.abrahanfer.geniusfeed.utils.network.bodyclass.FeedBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static me.abrahanfer.geniusfeed.FeedActivity.EARL_TAG;

/**
 * Created by abrahan on 9/08/16.
 */

public class AddFeedDialogFragment extends DialogFragment {

    private com.einmalfel.earl.Feed mFeedInfo;
    private FeedListUpdater mUpdateHelper;
    private Activity mActivity;

    public FeedListUpdater getUpdateHelper() {
        return mUpdateHelper;
    }

    public void setUpdateHelper(FeedListUpdater updateHelper) {
        this.mUpdateHelper = updateHelper;
    }

    static AddFeedDialogFragment newInstance() {
        AddFeedDialogFragment dialog = new AddFeedDialogFragment();

        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View layout = inflater.inflate(R.layout.add_feed_dialog, null);
        builder.setView(layout)
               // Add action buttons
               .setPositiveButton(R.string.add_feed_confirm, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int id) {
                       EditText editText = (EditText) layout.findViewById(R.id.urlFeedSource);
                       addFeedSource(editText.getText().toString());

                   }
               })
               .setNegativeButton(R.string.add_feed_cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       AddFeedDialogFragment.this.getDialog().cancel();
                   }
               });

        return builder.create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = getActivity();
    }

    public void addFeedSource(String stringURL) {

        final MainActivity activity = (MainActivity) getActivity();


        final Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                MainActivity act = (MainActivity) mActivity;
                act.stopProgressDialog();
                act.showAlertMessages(message.what);
            }
        };

        // Check for URL format
        if(stringURL.trim().length() > 0 && true) {
            FeedSourceGetter feedSourceGetter;
            try {
                feedSourceGetter = new FeedSourceGetter(new URL(stringURL));
                MainActivity act = (MainActivity) mActivity;
                act.showProgressDialog();
                feedSourceGetter.getSource(new FeedSourceGetter.FeedSourceGetterListener() {
                    @Override
                    public void onSuccess(URL feedSourceURL) {
                        // Make request to API to create new feed
                        createNewFeed(feedSourceURL);
                    }

                    @Override
                    public void onError() {
                        Log.e("ERROR", "Get feed Source");
                        // Show feedback to user through handle
                        handler.sendEmptyMessage(0);
                    }
                });
            } catch (MalformedURLException e) {
                Log.e("ERROR", "Get feed Source 2");
                handler.sendEmptyMessage(1);
            }
        }
    }

    public void createNewFeed(final URL feedSourceURL) {
        // TODO USe Earl to get data from feed like Title
        class RetrieveFeedInfo extends AsyncTask<Void, Void,
                com.einmalfel.earl.Feed> {


            @Override
            protected com.einmalfel.earl.Feed doInBackground(Void... params) {
                InputStream inputStream = null;
                com.einmalfel.earl.Feed feed = null;
                try {
                    inputStream = feedSourceURL.openConnection().getInputStream();
                } catch(IOException exception) {
                    Log.d(EARL_TAG, "Exception IO");
                    return feed;
                }

                try {
                    feed = EarlParser.parseOrThrow(inputStream, 0);
                } catch(XmlPullParserException xmlExcepcion){
                    Log.d(EARL_TAG, "Exception XML Pasrser");
                } catch(IOException ioException){
                    Log.d(EARL_TAG, "Exception IO");
                } catch(DataFormatException dataException) {
                    Log.d(EARL_TAG, "Exception data format");
                }

                mFeedInfo = feed;
                return feed;
            }

            protected void onPostExecute(com.einmalfel.earl.Feed feed) {
                Log.i(EARL_TAG, "Mirando titulo del nuevo Feed " + feed.getTitle());

                if (feed == null) {
                    showAlertMessageForError(0);
                    return;
                }

                String feedTitle = feed.getTitle();

                String token = Authentication.getCredentials().getToken();
                GeniusFeedService service = NetworkServiceBuilder.createService(GeniusFeedService.class, token);
                List<String> categoriesNames = new ArrayList<String>();
                if(feed.getClass() == com.einmalfel.earl.AtomFeed.class) {
                    com.einmalfel.earl.AtomFeed atomFeed = (com.einmalfel.earl.AtomFeed) feed;
                   Log.e("Categories", "ATOM Mirando cat "  + atomFeed.categories);
                    List<AtomCategory> categoriesList = atomFeed.categories;

                    for (AtomCategory cat : categoriesList) {
                        String name = cat.label != null ? cat.label : cat.term;
                        categoriesNames.add(name);
                    }
                } else if (feed.getClass() == com.einmalfel.earl.RSSFeed.class) {
                    com.einmalfel.earl.RSSFeed rssFeed = (com.einmalfel.earl.RSSFeed) feed;
                    Log.e("Categories", "RSS Mirando cat "  + rssFeed.categories);
                    List<RSSCategory> categoriesList = rssFeed.categories;

                    for (RSSCategory cat : categoriesList) {
                        categoriesNames.add(cat.value);
                    }
                }

                // Create categories from spec feed file
                List<Category> categories = new ArrayList<Category>();
                for (String name : categoriesNames) {
                    categories.add(new Category(name));
                }

                // Add step to show Dialog fragment to add more categories or remove the defaults
                MainActivity act = (MainActivity) mActivity;
                act.stopProgressDialog();
                act.showAddCategoriesDialog(categories, feed, mUpdateHelper);
                dismiss();
            }
        }
        new RetrieveFeedInfo().execute();
    }

    private void showAlertMessageForError(int errorCode) {
        // Print alert on mainThread
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.error_creating_feed)
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
