package me.abrahanfer.geniusfeed.utils;

import android.content.Context;
import android.media.Image;
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

import me.abrahanfer.geniusfeed.FeedActivity;
import me.abrahanfer.geniusfeed.R;
import me.abrahanfer.geniusfeed.models.Feed;
import me.abrahanfer.geniusfeed.models.FeedItem;
import me.abrahanfer.geniusfeed.models.FeedItemRead;
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

    public void updateFeedItemRead(FeedItemRead feedItemRead) {
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

                }
            }

            @Override
            public void onFailure(Call<FeedItemRead> call, Throwable t) {
                Log.e("FAILURE RESPONSE", t.toString());
            }
        });
    }
}
