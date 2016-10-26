package me.abrahanfer.geniusfeed.utils;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import me.abrahanfer.geniusfeed.thirdparty.java.com.cunoraz.tagview.Tag;
import me.abrahanfer.geniusfeed.thirdparty.java.com.cunoraz.tagview.TagView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.abrahanfer.geniusfeed.R;
import me.abrahanfer.geniusfeed.models.Category;
import me.abrahanfer.geniusfeed.models.Feed;
import me.abrahanfer.geniusfeed.models.FeedItemRead;
import me.abrahanfer.geniusfeed.utils.network.GeniusFeedService;
import me.abrahanfer.geniusfeed.utils.network.NetworkServiceBuilder;
import me.abrahanfer.geniusfeed.utils.network.bodyclass.FeedBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by abrahanfer on 30/09/15.
 */
public class FeedArrayAdapter extends RecyclerView.Adapter<FeedArrayAdapter.ViewHolder> {

    private ArrayList<Feed> mFeedArrayList;
    private Map<String, List<FeedItemRead>> mFeedArrayData;
    private Boolean enableTimeFrameMark = false;

    public void setEnableTimeFrameMark(Boolean enableTimeFrameMark) {
        this.enableTimeFrameMark = enableTimeFrameMark;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View mContainerView;

        public ViewHolder(View v){
            super(v);
            mContainerView = v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public FeedArrayAdapter(ArrayList<Feed> feedArrayList, Map<String, List<FeedItemRead>> feedArrayData) {
        mFeedArrayList = feedArrayList;
        mFeedArrayData = feedArrayData;
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
    public void onBindViewHolder(ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Feed feedElement = mFeedArrayList.get(position);
        List<Category> categories = feedElement.getCategory_set();


        TextView titleText =(TextView) holder.mContainerView.findViewById(R.id.textFeedTitle);
        titleText.setText(feedElement.getTitle());

        if(hadFeedItemUnread(feedElement)) {
            ImageView image = (ImageView) holder.mContainerView.findViewById(R.id.feedAvatar);
            image.setVisibility(ImageView.VISIBLE);
        }else{
            ImageView image = (ImageView) holder.mContainerView.findViewById(R.id.feedAvatar);
            image.setVisibility(ImageView.INVISIBLE);
        }

        TagView tagView = (TagView) holder.mContainerView.findViewById(R.id.tagsFeedCategories);
        List<Tag> tags = new ArrayList<Tag>();
        for(Category category : categories) {
            Tag tag = new Tag(category.getName());

            tag.layoutBorderColor = Color.parseColor("#9c27b0"); // R.color.color_primary;
            tag.layoutColor = Color.parseColor("#9c27b0");// R.color.color_primary;
            tag.layoutColorPress = Color.parseColor("#e1bee7");//R.color.color_primary_light;
            tag.tagTextColor = Color.parseColor("#ffffff");// R.color.color_text;
            tag.isDeletable = false;
            tag.radius = 1;

            tags.add(tag);
        }

        Log.e("Posicion y tags", "psocion " + position + " y tags " + tags);
        tagView.addTagsOnce(tags);
        View markTimeframeView =(View) holder.mContainerView.findViewById(R.id.color_mark_timeframe);
        markTimeframeView.setBackgroundColor(getColorForTimeframe(holder));
        markTimeframeView.setVisibility(enableTimeFrameMark ? View.VISIBLE : View.GONE);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mFeedArrayList.size();
    }

    private Boolean hadFeedItemUnread(Feed feed) {
        List<FeedItemRead> feedItemReads = mFeedArrayData.get(feed.getPk());

        for (FeedItemRead feedItemRead : feedItemReads) {
            if (!feedItemRead.getRead()) {
                return true;
            }
        }

        return false;
    }

    private int getColorForTimeframe(ViewHolder holder) {
        // TODO Select color based in timeframe and userpreferences
        return ContextCompat.getColor(holder.mContainerView.getContext(), R.color
                .color_red);
    }
}
