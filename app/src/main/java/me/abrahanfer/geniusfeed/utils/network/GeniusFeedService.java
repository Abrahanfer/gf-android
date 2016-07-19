package me.abrahanfer.geniusfeed.utils.network;

import org.json.JSONObject;

import me.abrahanfer.geniusfeed.utils.LoginBundle;
import me.abrahanfer.geniusfeed.utils.Token;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * HTTP API REST as service interface
 */

public interface GeniusFeedService {
    @POST("/login-token-auth")
    Call<Token> getLoginToken(@Body LoginBundle loginBundle);
}
