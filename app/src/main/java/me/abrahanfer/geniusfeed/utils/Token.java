package me.abrahanfer.geniusfeed.utils;

/**
 * Created by abrahan on 19/07/16.
 */

public class Token {
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    private String token;

    public Token(String token){
        this.token = token;
    }
}
