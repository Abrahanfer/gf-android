package me.abrahanfer.geniusfeed.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;

/**
 * Created by abrahan on 11/10/15.
 */
public class Authentication {
    private String username;
    private String token;
    private static Authentication singleton;

    public Authentication(String username){
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Nullable
    public static Authentication getCredentials(){
        //Get from database credentials
        if(singleton == null)
            return null;
        else
            return singleton;
    }

    public static Boolean setCredentials(Authentication
                                                 authentication) {
        //Return results of operation
        singleton = authentication;

        return Boolean.TRUE;
    }
}
