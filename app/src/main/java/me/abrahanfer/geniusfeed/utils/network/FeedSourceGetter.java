package me.abrahanfer.geniusfeed.utils.network;

import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import me.abrahanfer.geniusfeed.models.FeedItemRead;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by abrahan on 7/08/16.
 */

public class FeedSourceGetter {

    private URL URLSource;

    public FeedSourceGetter(URL URLSource){
        this.URLSource = URLSource;
    }

    public interface FeedSourceGetterListener {
        public void onSuccess(URL feedSourceURL);

        public void onError();
    }

    public void getSource(final FeedSourceGetterListener callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(URLSource.toString()).build();
        Response response;

        // Get data from URL
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String contentType = response.header("Content-Type");
                    String[] parts = contentType.split(";");
                    // Check content type
                    switch (parts[0]) {
                        // Else check if MIME type is HTML
                        case "text/html":
                            // Search for application/rss+xml or application/atom+xml inside HTML
                            try {
                                String responseBody = response.body().string();
                                searchLinkFeedSource(responseBody, callback);
                            } catch (IOException e) {
                                callback.onError();
                            }
                            break;
                        case "application/rss+xml":
                        case "application/atom+xml":
                            // Correct content-type, return URL
                            callback.onSuccess(URLSource);
                            break;
                        default:
                            callback.onError();
                    }
                }
            }
        });
    }

    public void searchLinkFeedSource(String html, FeedSourceGetterListener callback) {
        Document document = Jsoup.parse(html);

        Elements linkTags = document.head().getElementsByTag("link");
        for (Element linkTag : linkTags) {
            if(linkTag.attr("type").equals("application/atom+xml") || linkTag.attr("type").equals
                    ("application/rss+xml")) {
                // Check absolute or relative url
                String feedSourceStringUrl = linkTag.attr("href");
                URL feedSourceURL;
                try {
                    feedSourceURL = new URL(feedSourceStringUrl);
                    callback.onSuccess(feedSourceURL);
                }catch (MalformedURLException exception) {
                    // Maybe relative URL
                    String baseUrl = URLSource.toString();
                    String slashAux = "/";
                    if(baseUrl.endsWith(slashAux))
                        slashAux = "";
                    try {
                        callback.onSuccess(new URL(baseUrl + slashAux + feedSourceStringUrl));
                    } catch (MalformedURLException e) {
                        callback.onError();
                    }
                }

                return;
            }
        }
        callback.onError();
    }


}
