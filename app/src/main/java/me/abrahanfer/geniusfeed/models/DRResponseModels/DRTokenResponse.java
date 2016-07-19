package me.abrahanfer.geniusfeed.models.DRResponseModels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Class modeling TokenAuthenticationResponse.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class DRTokenResponse {
    private String token;
    //private String[] non_fields_errros;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
