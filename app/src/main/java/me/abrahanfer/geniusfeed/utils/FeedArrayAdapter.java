package me.abrahanfer.geniusfeed.utils;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import me.abrahanfer.geniusfeed.R;
import me.abrahanfer.geniusfeed.models.Feed;
import me.abrahanfer.geniusfeed.models.FeedItemRead;

/**
 * Created by abrahanfer on 30/09/15.
 */
public class FeedArrayAdapter extends RecyclerView.Adapter<FeedArrayAdapter.ViewHolder> {

    private ArrayList<Feed> mFeedArrayList;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View mContainerView;

        public ViewHolder(View v){
            super(v);
            mContainerView = v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public FeedArrayAdapter(ArrayList<Feed> feedArrayList) {
        mFeedArrayList = feedArrayList;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public FeedArrayAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                               .inflate(R.layout.feed, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        TextView titleText =(TextView) holder.mContainerView.findViewById(R.id.textFeedTitle);
        titleText.setText(mFeedArrayList.get(position).getTitle());
        if(position % 2 == 0) {
            ImageView image = (ImageView) holder.mContainerView.findViewById(R.id.feedAvatar);
            image.setVisibility(ImageView.VISIBLE);
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mFeedArrayList.size();
    }
}
