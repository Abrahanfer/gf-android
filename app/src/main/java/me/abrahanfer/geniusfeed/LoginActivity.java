package me.abrahanfer.geniusfeed;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import me.abrahanfer.geniusfeed.utils.Authentication;
import me.abrahanfer.geniusfeed.utils.Constants;
import me.abrahanfer.geniusfeed.utils.GeniusFeedContract;
import me.abrahanfer.geniusfeed.utils.network.bodyclass.LoginBundle;
import me.abrahanfer.geniusfeed.utils.network.bodyclass.Token;
import me.abrahanfer.geniusfeed.utils.network.GeniusFeedService;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {

    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.color_primary_dark));

        // Set up progressBar
        mProgressBar = (ProgressBar) findViewById(R.id.pbLoading);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setCredentials(View view) {
        // Check if no view has focus:
        View viewAux = this.getCurrentFocus();
        if (viewAux != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(viewAux.getWindowToken(), 0);
        }

        // TODO Set username from EditText view
        final EditText usernameEdit = (EditText) findViewById(R.id.editTextUsername);
        final EditText passwordEdit = (EditText) findViewById(R.id.editTextPassword);

        String username = usernameEdit.getText().toString();
        String password = passwordEdit.getText().toString();

        if (username != null && username.trim().length() > 0 &&
                password != null && password.trim().length() > 0) {
            requestToken(username,password);
        } else {
            // TODO toast bad credentials
        }

    }

    public void requestToken(String username, String password) {


        // Turn to invisible all login view components
        final EditText usernameEdit = (EditText) findViewById(R.id.editTextUsername);
        final EditText passwordEdit = (EditText) findViewById(R.id.editTextPassword);

        final Button buttonLogin = (Button) findViewById(R.id.buttonLogin);
        final Button buttonSignUp = (Button) findViewById(R.id.buttonSignUp);

        usernameEdit.setVisibility(EditText.INVISIBLE);
        passwordEdit.setVisibility(EditText.INVISIBLE);
        buttonLogin.setVisibility(Button.INVISIBLE);
        buttonSignUp.setVisibility(Button.INVISIBLE);

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
                    usernameEdit.setVisibility(EditText.VISIBLE);
                    passwordEdit.setVisibility(EditText.VISIBLE);
                    buttonLogin.setVisibility(Button.VISIBLE);
                    buttonSignUp.setVisibility(Button.VISIBLE);
                    Authentication authentication = Authentication.getCredentials();

                    authentication.setToken(token);

                    saveCredentialsToDB();
                } else {
                    mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                    usernameEdit.setVisibility(EditText.VISIBLE);
                    passwordEdit.setVisibility(EditText.VISIBLE);
                    buttonLogin.setVisibility(Button.VISIBLE);
                    buttonSignUp.setVisibility(Button.VISIBLE);
                    // error response, no access to resource?
                    showAlertDialogWithError(0);
                }
            }

            @Override
            public void onFailure(Call<Token> call, Throwable t) {
                // something went completely south (like no internet connection)
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                usernameEdit.setVisibility(EditText.VISIBLE);
                passwordEdit.setVisibility(EditText.VISIBLE);
                buttonLogin.setVisibility(Button.VISIBLE);
                buttonSignUp.setVisibility(Button.VISIBLE);
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
        final TextView usernameLabel = (TextView) findViewById(R.id.loginLabelUsername);
        final EditText usernameEdit = (EditText) findViewById(R.id.editTextUsername);
        final TextView passwordLabel = (TextView) findViewById(R.id.loginLabelPassword);
        final EditText passwordEdit = (EditText) findViewById(R.id.editTextPassword);

        final Button buttonLogin = (Button) findViewById(R.id.buttonLogin);
        final Button buttonSignUp = (Button) findViewById(R.id.buttonSignUp);

        usernameEdit.setVisibility(EditText.INVISIBLE);
        passwordEdit.setVisibility(EditText.INVISIBLE);
        buttonLogin.setVisibility(Button.INVISIBLE);
        buttonSignUp.setVisibility(Button.INVISIBLE);

        // ProgressBar
        mProgressBar.setVisibility(ProgressBar.VISIBLE);

        // Get DBHelper
        final GeniusFeedContract.GeniusFeedDbHelper mDbHelper =
                new GeniusFeedContract.GeniusFeedDbHelper(getApplicationContext());



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


                usernameEdit.setVisibility(EditText.VISIBLE);
                passwordEdit.setVisibility(EditText.VISIBLE);
                buttonLogin.setVisibility(Button.VISIBLE);
                buttonSignUp.setVisibility(Button.VISIBLE);
                // Load Intent
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        }

        new WriteUserCrendetialsToDB().execute();
    }

    private void showAlertDialogWithError(int errorCode) {
        // Print alert on mainThread
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
}
