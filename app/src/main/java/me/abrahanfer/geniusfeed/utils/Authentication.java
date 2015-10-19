package me.abrahanfer.geniusfeed.utils;

import android.support.annotation.Nullable;

/**
 * Created by abrahan on 11/10/15.
 */
public class Authentication {
    private String username;
    private String password;
    private static Authentication singleton;

    public Authentication(String username, String password){
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Nullable
    public static Authentication getCredentials(){
        //Get from database credentials
        if(singleton == null)
            return null;
        else
            return singleton;
    }

    public static Boolean setCredentials(Authentication authentication){
        //Set authentication in DB
        //Return results of operation
        singleton = authentication;

        return Boolean.TRUE;
    }
}
