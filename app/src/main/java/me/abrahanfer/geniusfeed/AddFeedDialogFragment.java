package me.abrahanfer.geniusfeed;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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

        if(stringURL.trim().length() > 0) {

            FeedSourceGetter feedSourceGetter;
            try { // "http://android-developers.blogspot.com.es/"
                feedSourceGetter = new FeedSourceGetter(new URL(stringURL));
                feedSourceGetter.getSource(new FeedSourceGetter.FeedSourceGetterListener() {
                    @Override
                    public void onSuccess(URL feedSourceURL) {
                        Log.d("SUCCESS", "Get feed Source " + feedSourceURL.toString());
                        // TODO Make request to API to create new feed
                        Log.e("depuración", "Entramos aqui 20");
                        createNewFeed(feedSourceURL);
                    }

                    @Override
                    public void onError() {
                        Log.e("ERROR", "Get feed Source");
                    }
                });
            } catch (MalformedURLException e) {
                Log.e("ERROR", "Get feed Source 2");
            }
        }
    }

    public void createNewFeed(final URL feedSourceURL) {
        // TODO USe Earl to get data from feed like Title
        class RetrieveFeedInfo extends AsyncTask<Void, Void,
                com.einmalfel.earl.Feed> {


            @Override
            protected com.einmalfel.earl.Feed doInBackground(Void... params) {
                Log.e("depuración", "Entramos aqui 10 ");
                InputStream inputStream = null;
                com.einmalfel.earl.Feed feed = null;
                Log.e("depuración", "Entramos aqui 12 ");
                try {
                    inputStream = feedSourceURL.openConnection().getInputStream();
                }catch (IOException exception) {
                    Log.d(EARL_TAG, "Exception IO");
                    Log.e("depuración", "Entramos aqui 12 ");
                    return feed;
                }
                Log.e("depuración", "Entramos aqui 13 ");
                try {
                    feed = EarlParser.parseOrThrow(inputStream, 0);
                    Log.e("depuración", "Entramos aqui 14 ");
                }catch (XmlPullParserException xmlExcepcion){
                    Log.d(EARL_TAG, "Exception XML Pasrser");
                }catch (IOException ioException){
                    Log.d(EARL_TAG, "Exception IO");
                }catch (DataFormatException dataException) {
                    Log.d(EARL_TAG, "Exception data format");
                }

                Log.e("depuración", "Entramos aqui 15 ");
                Log.i(EARL_TAG, "Processing feed: " + feed.getTitle());
                Log.e("depuración", "Entramos aqui 16 ");
                mFeedInfo = feed;
                return feed;
            }

            protected void onPostExecute(com.einmalfel.earl.Feed feed) {
                Log.i(EARL_TAG, "Mirando titulo del nuevo Feed " + feed.getTitle());



                String feedTitle = feed.getTitle();

                String token = Authentication.getCredentials().getToken();
                GeniusFeedService service = NetworkServiceBuilder.createService(GeniusFeedService.class, token);

                Call<Feed> call = service.createFeedSource(new FeedBody(feedTitle, feedSourceURL));

                call.enqueue(new Callback<Feed>() {
                    @Override
                    public void onResponse(Call<Feed> call, Response<Feed> response) {
                        Log.e("depuración", "Entramos aqui 1 " + response.code());
                        if (response.isSuccessful()) {
                            Log.e("depuración", "Entramos aqui 2");
                            Log.d("RETROFIT RESPONSE", "Response ok " + response.body().getPk());
                        }
                    }

                    @Override
                    public void onFailure(Call<Feed> call, Throwable t) {
                        // something went completely south (like no internet connection)
                        Log.e("CreateFeed RETROFIT", t.getMessage());
                    }
                });

            }
        }
        new RetrieveFeedInfo().execute();
    }
}
