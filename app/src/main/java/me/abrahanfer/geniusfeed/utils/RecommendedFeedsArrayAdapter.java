package me.abrahanfer.geniusfeed.utils;

import android.app.Activity;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.wefika.flowlayout.FlowLayout;

import me.abrahanfer.geniusfeed.thirdparty.java.com.cunoraz.tagview.Tag;
import me.abrahanfer.geniusfeed.thirdparty.java.com.cunoraz.tagview.TagView;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.RealmResults;
import me.abrahanfer.geniusfeed.MainActivity;
import me.abrahanfer.geniusfeed.R;
import me.abrahanfer.geniusfeed.models.Category;
import me.abrahanfer.geniusfeed.models.Feed;
import me.abrahanfer.geniusfeed.models.realmModels.FeedItemReadRealm;
import me.abrahanfer.geniusfeed.utils.network.GeniusFeedService;
import me.abrahanfer.geniusfeed.utils.network.NetworkServiceBuilder;
import me.abrahanfer.geniusfeed.utils.network.bodyclass.FeedBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by abrahan on 17/09/16.
 */

public class RecommendedFeedsArrayAdapter extends RecyclerView.Adapter<RecommendedFeedsArrayAdapter.ViewHolder> {

    private ArrayList<Feed> recommendedFeedsArrayList;
    private ArrayList<Feed> recommendedFeedsAdded;
    private Activity mActivity;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View mContainerView;

        public ViewHolder(View v){
            super(v);
            mContainerView = v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public RecommendedFeedsArrayAdapter(ArrayList<Feed> feedArrayList) {
        recommendedFeedsArrayList = feedArrayList;
        recommendedFeedsAdded = new ArrayList<>();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecommendedFeedsArrayAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                               .inflate(R.layout.recommended_feed_item, parent, false);
        RecommendedFeedsArrayAdapter.ViewHolder vh = new RecommendedFeedsArrayAdapter.ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecommendedFeedsArrayAdapter.ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        final Feed selectedFeed = recommendedFeedsArrayList.get(position);

        TextView titleText =(TextView) holder.mContainerView.findViewById(R.id.textFeedTitle);
        titleText.setText(selectedFeed.getTitle());

        FlowLayout flowLayout = (FlowLayout) holder.mContainerView.findViewById(R.id.tagsFeedCategoriesCustom);
        flowLayout.removeAllViews();
        for(Category category : selectedFeed.getCategory_set()) {
            TextView textView = new TextView(holder.mContainerView.getContext());
            textView.setText(category.getName());
            textView.setBackgroundColor(holder.mContainerView.getResources().getColor(R.color.color_primary));
            textView.setTextColor(holder.mContainerView.getResources().getColor(R.color.color_text));
            FlowLayout.LayoutParams params = new FlowLayout.LayoutParams(FlowLayout.LayoutParams.WRAP_CONTENT,
                                                                         FlowLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(10,10,10,10);
            textView.setPadding(10,10,10,10);

            flowLayout.addView(textView, params);
        }


        // Action for touching recommended feeds add button
        final ImageButton addRecommendedFeedButton =
                (ImageButton) holder.mContainerView.findViewById(R.id.recommendedFeedAvatar);
        addRecommendedFeedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addRecommendedFeedButton.isSelected() == false) {
                    addRecommendedFeedButton.setSelected(true);
                    // Add feed to user account
                    addRecommendedFeed(selectedFeed);
                    recommendedFeedsAdded.add(selectedFeed);
                }
            }
        });

        if (recommendedFeedsAdded.contains(selectedFeed)) {
            addRecommendedFeedButton.setSelected(true);
        } else {
            addRecommendedFeedButton.setSelected(false);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return recommendedFeedsArrayList.size();
    }

    private void addRecommendedFeed(Feed selectedFeed) {
        String token = Authentication.getCredentials().getToken();
        GeniusFeedService service = NetworkServiceBuilder.createService(GeniusFeedService.class, token);
        URL urlLink;

        urlLink = selectedFeed.getLink();
        List<Category> categories = selectedFeed.getCategory_set();
        Call<me.abrahanfer.geniusfeed.models.Feed> call = service.createFeedSource(new FeedBody(selectedFeed.getTitle(),
                                                                                                    urlLink, categories));

        call.enqueue(new Callback<Feed>() {
            @Override
            public void onResponse(Call<me.abrahanfer.geniusfeed.models.Feed> call, Response<Feed> response) {
                if (response.isSuccessful()) {
                    // TODO feedback to user
                } else {
                    // Feedback to user for fail on server
                    MainActivity act = (MainActivity) mActivity;
                    act.stopProgressDialog();
                    act.showAlertMessages(0);
                }
            }

            @Override
            public void onFailure(Call<me.abrahanfer.geniusfeed.models.Feed> call, Throwable t) {
                // Feedback to user for fail to communication
                MainActivity act = (MainActivity) mActivity;
                act.stopProgressDialog();
                act.showAlertMessages(0);
            }
        });

    }

    public void setActivity(Activity activity) {
        mActivity = activity;
    }
}
