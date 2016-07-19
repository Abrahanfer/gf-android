package me.abrahanfer.geniusfeed.utils;

/**
 * Created by abrahan on 19/07/16.
 */

public class LoginBundle {

    public String username;
    public String password;

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

    public LoginBundle(String username, String password){
        this.username = username;
        this.password = password;
    }


}
