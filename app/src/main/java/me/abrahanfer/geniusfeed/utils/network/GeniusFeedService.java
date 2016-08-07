package me.abrahanfer.geniusfeed.utils.network;

import me.abrahanfer.geniusfeed.models.DRResponseModels.FeedDRResponse;
import me.abrahanfer.geniusfeed.models.DRResponseModels.FeedItemReadDRResponse;
import me.abrahanfer.geniusfeed.models.FeedItemRead;
import me.abrahanfer.geniusfeed.utils.network.bodyclass.FIReadUpdateBody;
import me.abrahanfer.geniusfeed.utils.network.bodyclass.LoginBundle;
import me.abrahanfer.geniusfeed.utils.network.bodyclass.Token;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * HTTP API REST as service interface
 */

public interface GeniusFeedService {
    // User and login endpoints
    @POST("/auth/login/")
    Call<Token> getLoginToken(@Body LoginBundle loginBundle);


    // Feeds Endpoints
    @GET("/feeds/")
    Call<FeedDRResponse> getFeeds(@Query("page") int page);

    // Get Feeds Items Read from Feed
    @GET("/feed_item_reads/")
    Call<FeedItemReadDRResponse> getFeedItemReads(@Query("feed") String pk, @Query("page") int page);

    // Create new Feed Item Read
    @POST("/feed_item_reads/")
    Call<FeedItemRead> createFeedItemRead(@Body FeedItemRead feedItemRead);

    // Mark as read a feed item read
    @PATCH("/feed_item_reads/{pk}/")
    Call<FeedItemRead> partialUpdateFeedItemRead(@Path("pk") long pk, @Body FIReadUpdateBody fiReadUpdateBody);
}