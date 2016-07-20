package me.abrahanfer.geniusfeed.utils.network;

import java.io.IOException;

import me.abrahanfer.geniusfeed.utils.Constants;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by abrahan on 20/07/16.
 */

public class NetworkServiceBuilder {

    public static final String API_BASE_URL = Constants.getHostByEnviroment();
    public static final String TOKEN_STRING = "Token";

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    private static Retrofit.Builder builder = new Retrofit.Builder().baseUrl(API_BASE_URL)
                                                                    .addConverterFactory(GsonConverterFactory.create());

    public static <S> S createService(Class<S> serviceClass) {
        return createService(serviceClass, null);
    }

    public static <S> S createService(Class<S> serviceClass, final String authToken) {



        if (authToken != null) {
            httpClient.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Interceptor.Chain chain) throws IOException {
                    Request original = chain.request();

                    // Request customization: add request headers
                    Request.Builder requestBuilder = original.newBuilder().header("Authorization", TOKEN_STRING + " " +
                                                                                  authToken)
                                                             .method(original.method(), original.body());

                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }
            });
        }

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        // set your desired log level
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClient.addInterceptor(logging);
        OkHttpClient client = httpClient.build();
        Retrofit retrofit = builder.client(client).build();
        return retrofit.create(serviceClass);
    }
}
