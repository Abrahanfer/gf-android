package me.abrahanfer.geniusfeed;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.einmalfel.earl.EarlParser;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.zip.DataFormatException;

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

    public void addFeedSource(String stringURL) {

        final MainActivity activity = (MainActivity) getActivity();


        final Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {

                String alertMessage;
                if(message.what == 0){
                    alertMessage = getString(R.string.error_creating_feed);
                } else {
                    alertMessage = getString(R.string.error_url_malformed);
                }

                // Print alert on mainThread
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
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
        };



        // Check for URL format
        if(stringURL.trim().length() > 0 && true) {
            FeedSourceGetter feedSourceGetter;
            try {
                feedSourceGetter = new FeedSourceGetter(new URL(stringURL));
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

                Call<Feed> call = service.createFeedSource(new FeedBody(feedTitle, feedSourceURL));

                call.enqueue(new Callback<Feed>() {
                    @Override
                    public void onResponse(Call<Feed> call, Response<Feed> response) {
                        if (response.isSuccessful()) {
                            // Call to fragment to notify changes to adapter
                            mUpdateHelper.updateFeedData(response.body());
                        } else {
                            // Feedback to user for fail on server
                            showAlertMessageForError(0);
                        }
                    }

                    @Override
                    public void onFailure(Call<Feed> call, Throwable t) {
                        // Feedback to user for fail to communication
                        showAlertMessageForError(0);
                    }
                });

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
