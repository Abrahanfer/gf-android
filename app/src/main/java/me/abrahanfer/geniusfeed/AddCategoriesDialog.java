package me.abrahanfer.geniusfeed;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;

import com.cunoraz.tagview.Tag;
import com.einmalfel.earl.Feed;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import me.abrahanfer.geniusfeed.models.Category;
import me.abrahanfer.geniusfeed.utils.Authentication;
import me.abrahanfer.geniusfeed.utils.network.GeniusFeedService;
import me.abrahanfer.geniusfeed.utils.network.NetworkServiceBuilder;
import me.abrahanfer.geniusfeed.utils.network.bodyclass.FeedBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.cunoraz.tagview.TagView;

/**
 * Created by abrahan on 27/08/16.
 */

public class AddCategoriesDialog extends DialogFragment {

    private com.einmalfel.earl.Feed feedInfo;
    private List<Category> categories;
    private Activity mActivity;
    private Unbinder unbinder;
    private FeedListUpdater mUpdateHelper;

    private URL feedSourceURL;
    @BindView(R.id.autocompleteView)
    AutoCompleteTextView autoCompleteTextView;
    @BindView(R.id.addCategoryButton)
    ImageButton addCategoryButton;

    private TagView tagView;

    public Feed getFeedInfo() {
        return feedInfo;
    }

    public void setFeedInfo(Feed feedInfo) {
        this.feedInfo = feedInfo;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public FeedListUpdater getUpdateHelper() {
        return mUpdateHelper;
    }

    public void setUpdateHelper(FeedListUpdater updateHelper) {
        this.mUpdateHelper = updateHelper;
    }

    public URL getFeedSourceURL() {
        return feedSourceURL;
    }

    public void setFeedSourceURL(URL feedSourceURL) {
        this.feedSourceURL = feedSourceURL;
    }

    static AddCategoriesDialog newInstance() {
        AddCategoriesDialog dialog = new AddCategoriesDialog();

        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View layout = inflater.inflate(R.layout.add_categories_dialog, null);

        unbinder = ButterKnife.bind(this, layout);

        builder.setView(layout)
               // Add action buttons
               .setPositiveButton(R.string.add_categories_button, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int id) {
                       createFeed();
                   }
               })
               .setNegativeButton(R.string.cancel_add_categories, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       AddCategoriesDialog.this.getDialog().cancel();
                   }
               });

        tagView = (TagView) layout.findViewById(R.id.tagView);

        tagView.setOnTagDeleteListener(new TagView.OnTagDeleteListener() {
            @Override
            public void onTagDeleted(TagView tagView, Tag tag, int i) {
                onTagDeletedCallback(tagView,tag,i);
            }
        });

        List<Tag> tags = new ArrayList<Tag>();
        for(Category category : categories) {
            Tag tag = new Tag(category.getName());

            tag.layoutBorderColor = Color.parseColor("#9c27b0"); // R.color.color_primary;
            tag.layoutColor = Color.parseColor("#9c27b0");// R.color.color_primary;
            tag.layoutColorPress = Color.parseColor("#e1bee7");//R.color.color_primary_light;
            tag.tagTextColor = Color.parseColor("#ffffff");// R.color.color_text;
            tag.isDeletable = true;
            tag.radius = 1;

            tags.add(tag);
        }

        tagView.addTags(tags);

        return builder.create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = getActivity();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public void onTagDeletedCallback(final TagView view, final Tag tag, final int position) {
        String categoryName = tag.text;
        categories.remove(new Category(categoryName));
        view.remove(position);
    }

    @OnClick(R.id.addCategoryButton)
    public void addCategory() {
        String categoryText = autoCompleteTextView.getText().toString().trim();

        if(categoryText.length() > 0) {
            // Add new category to categories list
            Category newCategory = new Category(categoryText);
            if(!categories.contains(newCategory)) {
                // Cleaning textView
                autoCompleteTextView.setText("");

                categories.add(newCategory);

                // Add view for this new category
                Tag tag = new Tag(newCategory.getName());

                tag.layoutBorderColor = Color.parseColor("#9c27b0"); // R.color.color_primary;
                tag.layoutColor = Color.parseColor("#9c27b0");// R.color.color_primary;
                tag.layoutColorPress = Color.parseColor("#e1bee7");//R.color.color_primary_light;
                tag.tagTextColor = Color.parseColor("#ffffff");// R.color.color_text;
                tag.isDeletable = true;
                tag.radius = 1;

                tagView.addTag(tag);
            }
        }
    }

    public void createFeed() {
        // Finally add feed with categories
        String token = Authentication.getCredentials().getToken();
        GeniusFeedService service = NetworkServiceBuilder.createService(GeniusFeedService.class, token);
        URL urlLink;
        urlLink = feedSourceURL;

        Call<me.abrahanfer.geniusfeed.models.Feed> call = service.createFeedSource(new FeedBody(feedInfo.getTitle(),
                                                                                            urlLink, categories));

        call.enqueue(new Callback<me.abrahanfer.geniusfeed.models.Feed>() {
            @Override
            public void onResponse(Call<me.abrahanfer.geniusfeed.models.Feed> call, Response<me.abrahanfer.geniusfeed.models.Feed> response) {
                if (response.isSuccessful()) {
                    // Call to fragment to notify changes to adapter
                    mUpdateHelper.updateFeedData(response.body());
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
}
