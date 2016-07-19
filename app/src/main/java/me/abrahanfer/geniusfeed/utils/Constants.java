package me.abrahanfer.geniusfeed.utils;

/**
 * Class to store constants for all APP
 */

public class Constants {
    public final static String PRODUCTION_ENV = "";
    public final static String DEVELOPMENT_ENV = "http://192.168.1.55";

    public final static String ENV = "DEV";

    public static String getHostByEnviroment() {
        if(Constants.ENV.equalsIgnoreCase("DEV")){
            return Constants.DEVELOPMENT_ENV;
        }else{
            return Constants.PRODUCTION_ENV;
        }
    }

    public static final String AUTH_TOKEN = "/login-token-auth";


}
