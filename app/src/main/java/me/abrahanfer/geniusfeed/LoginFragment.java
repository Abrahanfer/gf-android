package me.abrahanfer.geniusfeed;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import me.abrahanfer.geniusfeed.utils.Authentication;
import me.abrahanfer.geniusfeed.utils.Constants;
import me.abrahanfer.geniusfeed.utils.GeniusFeedContract;
import me.abrahanfer.geniusfeed.utils.network.GeniusFeedService;
import me.abrahanfer.geniusfeed.utils.network.bodyclass.LoginBundle;
import me.abrahanfer.geniusfeed.utils.network.bodyclass.Token;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by abrahan on 23/08/16.
 */

public class LoginFragment extends Fragment {

    private View mBaseView;
    private Activity mActivity;
    @BindView(R.id.pbLoading) ProgressBar mProgressBar;
    @BindView(R.id.editTextUsername) EditText mEditTextUsername;
    @BindView(R.id.editTextPassword) EditText mEditTextPassword;
    @BindView(R.id.buttonLogin) Button mButtonLogin;
    @BindView(R.id.buttonSignUp) Button mButtonSignUp;

    private Unbinder unbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBaseView = inflater.inflate(R.layout.login_fragment, container, false);

        unbinder = ButterKnife.bind(this, mBaseView);

        return mBaseView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = getActivity();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.buttonLogin)
    public void setCredentials() {
        // Check if no view has focus:
        View viewAux = mActivity.getCurrentFocus();
        if (viewAux != null) {
            InputMethodManager imm = (InputMethodManager)mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(viewAux.getWindowToken(), 0);
        }

        // Get credentials from EditText views
        String username = mEditTextUsername.getText().toString();
        String password = mEditTextPassword.getText().toString();

        if (username != null && username.trim().length() > 0 &&
                password != null && password.trim().length() > 0) {
            requestToken(username,password);
        } else {
            // TODO toast bad credentials
        }

    }

    public void requestToken(String username, String password) {

        // Turn to invisible all login view components
        mEditTextUsername.setVisibility(EditText.INVISIBLE);
        mEditTextPassword.setVisibility(EditText.INVISIBLE);
        mButtonLogin.setVisibility(Button.INVISIBLE);
        mButtonSignUp.setVisibility(Button.INVISIBLE);

        mProgressBar.setVisibility(ProgressBar.VISIBLE);

        Authentication authentication = new Authentication(username);
        Authentication.setCredentials(authentication);

        // Make request to get token from API
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        // set your desired log level
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        // add your other interceptors …

        // add logging as last interceptor
        httpClient.addInterceptor(logging);  // <-- this is the important line!


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.getHostByEnviroment())
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        GeniusFeedService service = retrofit.create(GeniusFeedService.class);
        Call<Token> call = service.getLoginToken(new LoginBundle(username, password));
        call.enqueue(new Callback<Token>() {
            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {
                if (response.isSuccessful()) {
                    // tasks available

                    String token = response.body().getAuth_token();
                    Log.d("Login Token ", token);
                    mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                    mEditTextUsername.setVisibility(EditText.VISIBLE);
                    mEditTextPassword.setVisibility(EditText.VISIBLE);
                    mButtonLogin.setVisibility(Button.VISIBLE);
                    mButtonSignUp.setVisibility(Button.VISIBLE);
                    Authentication authentication = Authentication.getCredentials();

                    authentication.setToken(token);

                    saveCredentialsToDB();
                } else {
                    mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                    mEditTextUsername.setVisibility(EditText.VISIBLE);
                    mEditTextPassword.setVisibility(EditText.VISIBLE);
                    mButtonLogin.setVisibility(Button.VISIBLE);
                    mButtonSignUp.setVisibility(Button.VISIBLE);
                    // error response, no access to resource?
                    showAlertDialogWithError(0);
                }
            }

            @Override
            public void onFailure(Call<Token> call, Throwable t) {
                // something went completely south (like no internet connection)
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                mEditTextUsername.setVisibility(EditText.VISIBLE);
                mEditTextPassword.setVisibility(EditText.VISIBLE);
                mButtonLogin.setVisibility(Button.VISIBLE);
                mButtonSignUp.setVisibility(Button.VISIBLE);
                showAlertDialogWithError(0);
            }
        });
    /*class GetTokenRequest extends AsyncTask<String, Void, String> {
        private Exception exception;

        @Override
        protected String doInBackground(String... values) {
            Authentication authentication = new Authentication(values[0]);
            Authentication.setCredentials(authentication);

            // Create the request body as a MultiValueMap
            MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
            JSONObject bodyJson = new JSONObject();
            // TODO Put key in constants
            try {
                bodyJson.put("username", values[0]);
                bodyJson.put("password", values[1]);
            }catch (JSONException jsonException){
                // TODO Process error on login
                System.out.println("Mirando la excepcion de JSON");
                System.out.println(jsonException);
                return null;
            }

            // set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<?> requestEntity = new HttpEntity<Object>(bodyJson,headers);
            RestTemplate restTemplate = new RestTemplate();

            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            try {
                Log.e("LoginActivity", "Getting token");
                String urlToken = Constants.getHostByEnviroment() + Constants.AUTH_TOKEN;

                HttpEntity<DRTokenResponse> response = restTemplate
                        .exchange(urlToken, HttpMethod.POST, requestEntity, DRTokenResponse.class);

                DRTokenResponse result = response.getBody();
                String token = result.getToken();

                return token;
            } catch (RestClientException springException) {
                // TODO Process error on login
                System.out.println("Mirando la excepcion de Spring");
                System.out.println(springException);
                return null;
            }
        }

        protected void onPostExecute(String token) {
            System.out.println("Mirando el token" + token);

            mProgressBar.setVisibility(ProgressBar.INVISIBLE);
            Authentication authentication = Authentication.getCredentials();
            authentication.setToken(token);

            saveCredentialsToDB();
            }
        }*/

        //new GetTokenRequest().execute(username,password);
    }

    public void saveCredentialsToDB() {
        // Turn to invisible all login view components
        mEditTextUsername.setVisibility(EditText.INVISIBLE);
        mEditTextPassword.setVisibility(EditText.INVISIBLE);
        mButtonLogin.setVisibility(Button.INVISIBLE);
        mButtonSignUp.setVisibility(Button.INVISIBLE);

        // ProgressBar
        mProgressBar.setVisibility(ProgressBar.VISIBLE);

        // Get DBHelper
        final GeniusFeedContract.GeniusFeedDbHelper mDbHelper =
                new GeniusFeedContract.GeniusFeedDbHelper(mActivity.getApplicationContext());



        class WriteUserCrendetialsToDB extends AsyncTask<Void, Void, SQLiteDatabase> {

            @Override
            protected SQLiteDatabase doInBackground(Void... params) {
                return mDbHelper.getWritableDatabase();
            }

            protected void onPostExecute(SQLiteDatabase dataBase) {
                Authentication auth = Authentication.getCredentials();
                // Define projector
                String[] projection = {
                        GeniusFeedContract.User.COLUMN_NAME_USER_ID,
                        GeniusFeedContract.User.COLUMN_NAME_USERNAME,
                        GeniusFeedContract.User.COLUMN_NAME_TOKEN
                };

                Cursor c = dataBase.query(
                        GeniusFeedContract.User.TABLE_NAME,
                        projection,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "1"
                );

                if (c.getCount() > 0)
                    dataBase.delete(GeniusFeedContract.User.TABLE_NAME, "1", null);

                // Create a new map of values, where column names are the keys
                ContentValues values = new ContentValues();
                values.put(GeniusFeedContract.User.COLUMN_NAME_USERNAME, auth.getUsername());
                values.put(GeniusFeedContract.User.COLUMN_NAME_TOKEN, auth.getToken());
                // Insert the new row, returning the primary key value of the new row
                long newRowId;
                newRowId = dataBase.insert(GeniusFeedContract.User.TABLE_NAME, "null",
                                           //GeniusFeedContract.User.COLUMN_NAME_USER_ID,
                                           values);


                // Return to normal state
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);


                mEditTextUsername.setVisibility(EditText.VISIBLE);
                mEditTextPassword.setVisibility(EditText.VISIBLE);
                mButtonLogin.setVisibility(Button.VISIBLE);
                mButtonSignUp.setVisibility(Button.VISIBLE);
                // Load Intent
                Intent intent = new Intent(mActivity.getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        }

        new WriteUserCrendetialsToDB().execute();
    }

    private void showAlertDialogWithError(int errorCode) {
        // Print alert on mainThread
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setMessage(R.string.error_login_communication)
               .setCancelable(false)
               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // Dismiss dialog
                       //dialog.dismiss();
                   }
               });

        builder.create().show();
    }

    @OnClick(R.id.buttonSignUp)
    public void goToSignUpView() {
        LoginActivity loginActivity = (LoginActivity) mActivity;
        loginActivity.changeToSignUpFragment();
    }
}
