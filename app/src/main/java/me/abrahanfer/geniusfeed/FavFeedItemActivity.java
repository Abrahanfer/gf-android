package me.abrahanfer.geniusfeed;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

import me.abrahanfer.geniusfeed.models.FeedItem;

/**
 * Created by abrahan on 14/09/16.
 */

public class FavFeedItemActivity extends AppCompatActivity implements NetworkStatusFeedbackInterface {

    private FeedItem feedItem;
    private String content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fav_feed_item);

        // Set title in collapsingToolbarLayout
        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle("Entry");
        collapsingToolbar.setTitleEnabled(false);

        // Set toolbar as support action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        feedItem = getIntent().getParcelableExtra(FeedActivity.FEED_ITEM);
        content = getIntent().getStringExtra("CONTENT");

        getSupportActionBar().setTitle(feedItem.getTitle());

        Button viewOnSource = (Button) findViewById(R.id.viewOnSource);
        viewOnSource.setVisibility(Button.VISIBLE);
        viewOnSource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewOnSourceAction();
            }
        });
        WebView webView = (WebView) findViewById(R.id.feedItemWebView);
        webView.setVisibility(WebView.VISIBLE);
        webView.loadDataWithBaseURL(null, wrapHtml(webView.getContext(), content),
                                    "text/html", "UTF-8", null);
    }


    private void viewOnSourceAction() {
        String url = feedItem.getLink();
        if (url.startsWith("http://") || url.startsWith("https://")) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }
    }

    private String wrapHtml(Context context, String html) {
        return context.getString(R.string.html_wrap, html);
    }

    public void showAlertMessages(int errorCode) {
        int alertMessage;

        switch (errorCode) {
            case 5:
                alertMessage = R.string.network_disconnected;
                break;
            default:
                alertMessage = R.string.network_disconnected;
        }


        // Print alert on mainThread
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(alertMessage)
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
