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
        // TODO Get data from URL
        class GetFeedSource extends AsyncTask<URL, Void, String > {
            private Exception exception;

            @Override
            protected String doInBackground(URL... url) {
                OkHttpClient client = new OkHttpClient();
                Log.e("FeedSourceGetter", "IOException launched 1");
                Request request = new Request.Builder().url(url[0].toString()).build();
                Log.e("FeedSourceGetter", "IOException launched 2");
                Response response;
                try {
                    Log.e("FeedSourceGetter", "IOException launched 3");
                    response = client.newCall(request).execute();

                    String contentType = response.header("Content-Type");
                    Log.e("FeedSourceGetter", "IOException launched 4 " + contentType);
                    String[] parts = contentType.split(";");
                    // TODO Check content type
                    switch (parts[0]) {
                        // TODO Else check if MIME type is HTML
                        case "text/html":
                            // TODO Search for application/rss+xml or application/atom+xml inside HTML
                            Log.e("FeedSourceGetter", "IOException launched 5");
                            try {
                                String responseBody = response.body().string();
                                return responseBody;
                            }catch (IOException e) {
                                callback.onError();
                            }
                            break;
                        case "application/rss+xml":
                        case "application/atom+xml":
                            // TODO Correct content-type, return URL
                            Log.e("FeedSourceGetter", "IOException launched 6");
                            callback.onSuccess(URLSource);
                            break;
                        default:
                            callback.onError();
                    }
                    return null;

                } catch (IOException e) {
                    Log.e("FeedSourceGetter", "IOException launched");
                    callback.onError();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String responseBody){
                searchLinkFeedSource(responseBody, callback);

            }
        }

        new GetFeedSource().execute(URLSource);
    }

    public void searchLinkFeedSource(String html, FeedSourceGetterListener callback) {
        Document document = Jsoup.parse(html);

       // Elements linkTags = document.head().getElementsByTag("link");
        Elements linkTags = document.head().getElementsByTag("link");
        Log.e("FeedSourceGetter", "IOException launched 7 " + document.toString());
        for (Element linkTag : linkTags) {
            Log.e("FeedSourceGetter", "IOException launched 8");
            if(linkTag.attr("type").equals("application/atom+xml") || linkTag.attr("type").equals
                    ("application/rss+xml")) {
                Log.e("FeedSourceGetter", "IOException launched 9");
                String baseUrl = URLSource.toString();
                Log.d("URL for feeds", "URL: " + linkTag.attr("href"));
                try {
                    callback.onSuccess(new URL(baseUrl + linkTag.attr("href")));
                } catch (MalformedURLException e) {
                    callback.onError();
                }
                return;
            }
        }
        callback.onError();
    }
}
