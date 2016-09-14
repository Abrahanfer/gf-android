package me.abrahanfer.geniusfeed;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.realm.Realm;
import io.realm.RealmResults;
import me.abrahanfer.geniusfeed.models.Feed;
import me.abrahanfer.geniusfeed.models.FeedItem;
import me.abrahanfer.geniusfeed.models.realmModels.FeedItemReadRealm;
import me.abrahanfer.geniusfeed.models.realmModels.FeedItemRealm;
import me.abrahanfer.geniusfeed.utils.FavFeedArrayAdapter;

/**
 * Created by abrahan on 14/09/16.
 */

public class FavFeedItemListFragment extends Fragment {
    private View mBaseView;
    private Activity mActivity;
    private RealmResults<FeedItemReadRealm> mFavFeedItemsResults;

    @BindView(R.id.fav_feed_item_list)
    RecyclerView favFeedItemList;

    private Unbinder unbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBaseView = inflater.inflate(R.layout.fav_feed_list_fragment, container, false);

        unbinder = ButterKnife.bind(this, mBaseView);

        favFeedItemList.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(mBaseView.getContext());
        favFeedItemList.setLayoutManager(layoutManager);

        return mBaseView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = getActivity();
        // Get All fav feed_items from DB
        showFavFeedItem();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void showFavFeedItem() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<FeedItemReadRealm> feedItemReadRealms = realm.where(FeedItemReadRealm.class).findAll();
        mFavFeedItemsResults = feedItemReadRealms;
        FavFeedArrayAdapter favFeedArrayAdapter = new FavFeedArrayAdapter(feedItemReadRealms);
        favFeedItemList.setAdapter(favFeedArrayAdapter);
        setupListFeeds();
    }

    public void setupListFeeds() {

        RecyclerView feedListView = (RecyclerView) mBaseView.findViewById(R.id.feeds_list);

        ItemClickSupport.addTo(favFeedItemList).setOnItemClickListener(
                new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        Intent intent = new Intent(
                                mActivity.getApplicationContext(),
                                FavFeedItemActivity.class);

                        FeedItemReadRealm feedItemReadRealm = mFavFeedItemsResults.get(position);
                        FeedItemRealm feedItemRealm = feedItemReadRealm.getFeed_item();
                        FeedItem feedItem = new FeedItem(feedItemRealm);
                        intent.putExtra(FeedActivity.FEED_ITEM, feedItem);
                        intent.putExtra("CONTENT", feedItemRealm.getContent());

                        startActivity(intent);
                    }
                }
        );
    }
}
