package me.abrahanfer.geniusfeed.utils.network.bodyclass;

/**
 * Created by abrahan on 24/08/16.
 */

public class RegisterBundle {
    private String email;
    private String username;
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public RegisterBundle(String email, String username, String password){
        this.email = email;
        this.username = username;
        this.password = password;
    }
}
