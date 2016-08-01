package me.abrahanfer.geniusfeed.utils.network.bodyclass;

/**
 * Created by abrahan on 19/07/16.
 */

public class Token {
    public String getAuth_token() {
        return auth_token;
    }

    public void setAuth_token(String auth_token) {
        this.auth_token = auth_token;
    }

    private String auth_token;

    public Token(String auth_token){
        this.auth_token = auth_token;
    }
}
