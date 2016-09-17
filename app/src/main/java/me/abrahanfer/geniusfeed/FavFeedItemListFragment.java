package me.abrahanfer.geniusfeed;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
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

        // Add Swipe gesture to items
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                                                   ItemTouchHelper.RIGHT) {
                    public boolean onMove(RecyclerView recyclerView,
                                          RecyclerView.ViewHolder viewHolder,
                                          RecyclerView.ViewHolder target) {

                        return false;
                    }

                    public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {

                        // Removing fav item from realm
                        Realm realm = Realm.getDefaultInstance();

                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                // remove from Realm
                                mFavFeedItemsResults.deleteFromRealm(viewHolder.getAdapterPosition());
                                // remove from Recycler view
                                favFeedItemList.getAdapter().notifyItemRemoved(viewHolder.getAdapterPosition());
                            }
                        });


                    }

                    @Override
                    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                            // Get RecyclerView item from the ViewHolder
                            View itemView = viewHolder.itemView;

                            Paint p = new Paint();
                            Bitmap trashIcon;
                            float height = (float) itemView.getBottom() - (float) itemView.getTop();
                            float width = height / 3;

                            if (dX > 0) {
                                /* Set your color for positive displacement */
                                trashIcon = BitmapFactory.decodeResource(
                                        mActivity.getBaseContext().getResources(), R.drawable
                                                .ic_delete_black_36dp);

                                p.setColor(Color.parseColor("#f44336")); // App RED
                                // Draw Rect with varying right side, equal to displacement dX
                                c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX,
                                           (float) itemView.getBottom(), p);

                                // Set the image icon for swipe background
                                RectF icon_dest = new RectF((float) itemView.getLeft() + width , (float) itemView.getTop() + width, (float) itemView.getLeft()+ 2*width, (float)itemView.getBottom() - width);
                                ColorFilter whiteFilter = new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                                p.setColorFilter(whiteFilter);
                                c.drawBitmap(trashIcon,null,icon_dest,p);
                            }

                            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                        }
                    }
                });
        itemTouchHelper.attachToRecyclerView(favFeedItemList);

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
