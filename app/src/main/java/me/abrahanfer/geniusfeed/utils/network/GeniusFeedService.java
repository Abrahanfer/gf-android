package me.abrahanfer.geniusfeed.utils.network;

import org.json.JSONObject;

import java.util.List;

import me.abrahanfer.geniusfeed.models.DRResponseModels.FeedDRResponse;
import me.abrahanfer.geniusfeed.models.Feed;
import me.abrahanfer.geniusfeed.utils.LoginBundle;
import me.abrahanfer.geniusfeed.utils.Token;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * HTTP API REST as service interface
 */

public interface GeniusFeedService {
    // User and login endpoints
    @POST("/auth/login/")
    Call<Token> getLoginToken(@Body LoginBundle loginBundle);

    // Feeds Endpoints
    @GET("/feeds")
    Call<FeedDRResponse> getFeeds();
}
