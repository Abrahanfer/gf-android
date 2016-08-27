package me.abrahanfer.geniusfeed;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.einmalfel.earl.Feed;

import java.util.List;

import me.abrahanfer.geniusfeed.models.Category;

/**
 * Created by abrahan on 27/08/16.
 */

public class AddCategoriesDialog extends DialogFragment {

    private com.einmalfel.earl.Feed feedInfo;
    private List<Category> categories;
    private Activity mActivity;

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
        builder.setView(layout)
               // Add action buttons
               .setPositiveButton(R.string.add_categories_button, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int id) {
                      AddCategoriesDialog.this.getDialog().cancel();
                   }
               })
               .setNegativeButton(R.string.cancel_add_categories, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       AddCategoriesDialog.this.getDialog().cancel();
                   }
               });

        return builder.create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = getActivity();
    }
}
