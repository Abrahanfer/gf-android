package me.abrahanfer.geniusfeed.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.media.Image;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmModel;
import io.realm.RealmResults;
import me.abrahanfer.geniusfeed.FeedActivity;
import me.abrahanfer.geniusfeed.R;
import me.abrahanfer.geniusfeed.models.Category;
import me.abrahanfer.geniusfeed.models.Feed;
import me.abrahanfer.geniusfeed.models.FeedItem;
import me.abrahanfer.geniusfeed.models.FeedItemAtom;
import me.abrahanfer.geniusfeed.models.FeedItemRSS;
import me.abrahanfer.geniusfeed.models.FeedItemRead;
import me.abrahanfer.geniusfeed.models.realmModels.CategoryRealm;
import me.abrahanfer.geniusfeed.models.realmModels.FeedItemReadRealm;
import me.abrahanfer.geniusfeed.models.realmModels.FeedItemRealm;
import me.abrahanfer.geniusfeed.models.realmModels.FeedRealm;
import me.abrahanfer.geniusfeed.utils.network.GeniusFeedService;
import me.abrahanfer.geniusfeed.utils.network.NetworkServiceBuilder;
import me.abrahanfer.geniusfeed.utils.network.bodyclass.FIReadUpdateBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by abrahan on 19/03/16.
 */
public class FeedItemsArrayAdapter extends RecyclerView.Adapter<FeedItemsArrayAdapter.ViewHolder> {

    private ArrayList<FeedItemRead> mFeedItemReadList;
    protected Activity activity;

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View mContainerView;

        public ViewHolder(View v){
            super(v);
            mContainerView = v;
        }
    }

    public FeedItemsArrayAdapter(ArrayList<FeedItemRead>
            feedItems){
        mFeedItemReadList = feedItems;
    }

   /* @Override
    public View getView(int position, View convertView, ViewGroup parent){
        FeedItemRead feedItemRead = getItem(position);

        //if (convertView == null){
            if(feedItemRead.getRead()) {
                Log.e("Entra alguno aqui?", "Mirando el log");
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.feed_item, parent, false);
            }else{
                Log.e("Entra alguno aqui? 2", "Mirando el log");
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.feed_item_unread,parent,false);
            }
        //}

        TextView titleText =(TextView) convertView.findViewById(R.id
                                                                        .textFeedItemTitle);
        TextView linkText =(TextView) convertView.findViewById(R.id
                                                                       .textFeedItemLink);

        titleText.setText(feedItemRead.getFeed_item().getTitle());
        linkText.setText(feedItemRead.getFeed_item().getLink());

        return convertView;
    }*/

    // Create new views (invoked by the layout manager)
    @Override
    public FeedItemsArrayAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                               .inflate(R.layout.feed_item, parent, false);
        FeedItemsArrayAdapter.ViewHolder vh = new FeedItemsArrayAdapter.ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(FeedItemsArrayAdapter.ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        final FeedItemRead feedItemRead = mFeedItemReadList.get(position);

        final ImageButton readButton = (ImageButton) holder.mContainerView.findViewById(R.id.feedAvatarRead);
        readButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readButton.setSelected(!readButton.isSelected());

                if (readButton.isSelected()){
                    // Mark as read feedItemRead
                    feedItemRead.setRead(true);
                    updateFeedItemRead(feedItemRead);
                }else{
                    // Mark as unread feedItemRead
                    feedItemRead.setRead(false);
                    updateFeedItemRead(feedItemRead);
                }
            }
        });

        // Setting favourite Button berhaviour

        final ImageButton favButton = (ImageButton) holder.mContainerView.findViewById(R.id.favouriteButton);
        favButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                favButton.setSelected(!favButton.isSelected());

                if (favButton.isSelected()) {
                    // Mark as favourite feedItemRead
                    feedItemRead.setFav(true);
                    updateFeedItemRead(feedItemRead);
                    // Save in local
                    saveFavFeedItemRead(feedItemRead);
                } else {
                    // Mark as not favourite feedItemRead
                    feedItemRead.setFav(false);
                    updateFeedItemRead(feedItemRead);
                }
            }
        });


        TextView titleText = (TextView) holder.mContainerView.findViewById(R.id.textFeedItemTitle);
        titleText.setText(mFeedItemReadList.get(position).getFeed_item().getTitle());

        TextView pubDateText = (TextView) holder.mContainerView.findViewById(R.id.textFeedItemPubDate);
        Date pubDate = mFeedItemReadList.get(position).getFeed_item().getPublicationDate();
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        if(pubDate != null)
           pubDateText.setText(dateFormat.format(pubDate));

        if (feedItemRead.getRead()) {
            readButton.setSelected(true);
        } else {
            readButton.setSelected(false);
        }

        if (feedItemRead.getFav()) {
            favButton.setSelected(true);
        } else {
            favButton.setSelected(false);
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mFeedItemReadList.size();
    }

    public void updateFeedItemRead(final FeedItemRead feedItemRead) {
        final String token = Authentication.getCredentials().getToken();
        GeniusFeedService service = NetworkServiceBuilder.createService(GeniusFeedService.class, token);

        Call<FeedItemRead> call = service.partialUpdateFeedItemRead(feedItemRead.getPk(), new FIReadUpdateBody
                (feedItemRead.getRead(), feedItemRead.getFav()));


        call.enqueue(new Callback<FeedItemRead>() {
            @Override
            public void onResponse(Call<FeedItemRead> call, Response<FeedItemRead> response) {
                if(response.isSuccessful()) {
                    Log.d("SUCCESS RESPONSE", response.body().toString());
                } else {
                    showAlertDialogWithError(0);
                }
            }

            @Override
            public void onFailure(Call<FeedItemRead> call, Throwable t) {
                Log.e("FAILURE RESPONSE", t.toString());
                showAlertDialogWithError(0);
            }
        });
    }

    private void saveFavFeedItemRead(final FeedItemRead feedItemRead) {
        FeedItem feedItem = feedItemRead.getFeed_item();
        final Feed feed = feedItem.getFeed();

        // GetRealm instance
        Realm realm = Realm.getDefaultInstance();
        RealmResults<FeedItemRealm> feedItemResults= realm.where(FeedItemRealm.class).equalTo("pk", feedItem.getPk())
                                                      .findAll();
        if (feedItemResults.size() > 0) {
            saveFavFeedItemReadForFeedItem(feedItemRead, feedItemResults.get(0));
        } else {
            RealmResults<FeedRealm> feedResults= realm.where(FeedRealm.class).equalTo("pk", feed.getPk())
                                                              .findAll();
            if (feedResults.size() > 0) {
                saveFavFeedItemReadForFeed(feedItemRead, feedResults.get(0));
            } else {
                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        FeedRealm feedRealm = realm.createObject(FeedRealm.class);
                        feedRealm.setPk(feed.getPk());
                        feedRealm.setLinkURL(feed.getLink().toString());
                        feedRealm.setTitle(feed.getTitle());

                        for(Category category : feed.getCategory_set()) {
                            RealmResults<CategoryRealm> categoryResults = realm.where(CategoryRealm.class).equalTo
                                    ("name",
                                                                                                                  category
                                                                                                                          .getName())

                                                                              .findAll();
                            CategoryRealm categoryRealm;
                            if (categoryResults.size() == 0) {
                                categoryRealm = realm.createObject(CategoryRealm.class);
                                categoryRealm.setName(category.getName());
                            } else {
                                categoryRealm = categoryResults.get(0);
                            }

                            feedRealm.getCategory_set().add(categoryRealm);
                        }
                    }
                }, new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        Realm realm = Realm.getDefaultInstance();
                        RealmResults<FeedRealm> feedResults= realm.where(FeedRealm.class).equalTo("pk", feed.getPk())
                                                                  .findAll();
                        saveFavFeedItemReadForFeed(feedItemRead, feedResults.get(0));
                    }
                });
            }
        }
    }

    private void saveFavFeedItemReadForFeed(final FeedItemRead feedItemRead, final FeedRealm feedRealm) {
        // GetRealm instance
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                FeedItem feedItem = feedItemRead.getFeed_item();
                FeedItemRealm feedItemRealm = realm.createObject(FeedItemRealm.class);
                feedItemRealm.setPk(feedItem.getPk());
                feedItemRealm.setTitle(feedItem.getTitle());
                feedItemRealm.setLink(feedItem.getLink());
                feedItemRealm.setPublicationDate(feedItem.getPublicationDate());
                feedItemRealm.setItem_id(feedItem.getItem_id());

                // Save content too
                if (FeedItemAtom.class.isInstance(feedItem)) {
                    FeedItemAtom feedItemAtom = (FeedItemAtom) feedItem;
                    feedItemRealm.setContent(feedItemAtom.getValue());
                } else {
                    FeedItemRSS feedItemRSS = (FeedItemRSS) feedItem;
                    feedItemRealm.setContent(feedItemRSS.getDescription());
                }

                Feed feed = feedItemRead.getFeed_item().getFeed();
                RealmResults<FeedRealm> feedResults= realm.where(FeedRealm.class).equalTo("pk", feed.getPk())
                                                                  .findAll();

                feedItemRealm.setFeed(feedResults.get(0));
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                Realm realm = Realm.getDefaultInstance();
                FeedItem feedItem = feedItemRead.getFeed_item();
                RealmResults<FeedItemRealm> feedItemResults= realm.where(FeedItemRealm.class).equalTo("pk", feedItem.getPk())
                                                                  .findAll();
                saveFavFeedItemReadForFeedItem(feedItemRead, feedItemResults.get(0));
            }
        });
    }

    private void saveFavFeedItemReadForFeedItem(final FeedItemRead feedItemRead, final FeedItemRealm feedItemRealm) {
        // GetRealm instance
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                FeedItemReadRealm feedItemReadRealm = realm.createObject(FeedItemReadRealm.class);
                feedItemReadRealm.setPk(feedItemRead.getPk());
                feedItemReadRealm.setFav(feedItemRead.getFav());
                feedItemReadRealm.setRead(feedItemRead.getRead());
                feedItemReadRealm.setUpdate_date(feedItemRead.getUpdate_date());
                feedItemReadRealm.setUser(feedItemRead.getUser());

                FeedItem feedItem = feedItemRead.getFeed_item();
                RealmResults<FeedItemRealm> feedItemResults= realm.where(FeedItemRealm.class).equalTo("pk", feedItem.getPk())
                                                                  .findAll();
                feedItemReadRealm.setFeed_item(feedItemResults.get(0));
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                Log.e("REALM!!!", "ALL OK!!!! SAVE COMPLETE!!!");
            }
        });

    }

    private void showAlertDialogWithError(int errorCode) {
        // Print alert on mainThread
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(R.string.error_updating_feed_item)
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
