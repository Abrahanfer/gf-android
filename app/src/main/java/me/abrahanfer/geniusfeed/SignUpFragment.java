package me.abrahanfer.geniusfeed;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import me.abrahanfer.geniusfeed.models.DRResponseModels.FeedDRResponse;
import me.abrahanfer.geniusfeed.utils.Authentication;
import me.abrahanfer.geniusfeed.utils.Constants;
import me.abrahanfer.geniusfeed.utils.GeniusFeedContract;
import me.abrahanfer.geniusfeed.utils.network.GeniusFeedService;
import me.abrahanfer.geniusfeed.utils.network.NetworkServiceBuilder;
import me.abrahanfer.geniusfeed.utils.network.bodyclass.LoginBundle;
import me.abrahanfer.geniusfeed.utils.network.bodyclass.RegisterBundle;
import me.abrahanfer.geniusfeed.utils.network.bodyclass.Token;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by abrahan on 23/08/16.
 */

public class SignUpFragment extends Fragment {

    private View mBaseView;
    private Activity mActivity;

    @BindView(R.id.pbLoading) ProgressBar mProgressBar;
    @BindView(R.id.editTextEmail) EditText mEditTextEmail;
    @BindView(R.id.editTextPassword) EditText mEditTextPassword;
    @BindView(R.id.editTextRepeatPassword) EditText mEditTextRepeatPassword;
    @BindView(R.id.buttonSignUp) Button mButtonSignUp;
    @BindView(R.id.buttonLogin) Button mButtonLogin;

    private Unbinder unbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBaseView = inflater.inflate(R.layout.signup_fragment, container, false);
        unbinder = ButterKnife.bind(this, mBaseView);

        mEditTextEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!validateEmail(s.toString())) {
                    mButtonSignUp.setEnabled(false);
                    mEditTextEmail.setError(getResources().getString(R.string.email_error_validation));
                } else {
                    mButtonSignUp.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mEditTextPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!validatePassword(s.toString())) {
                    mButtonSignUp.setEnabled(false);
                    mEditTextPassword.setError(getResources().getString(R.string.password_error_validation));
                } else {
                    mButtonSignUp.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mEditTextRepeatPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!validatePassword(s.toString())
                    || !mEditTextPassword.getText().toString().equals(s.toString())) {
                    mButtonSignUp.setEnabled(false);
                    mEditTextRepeatPassword.setError(
                            getResources().getString(R.string.repeat_password_error_validation));
                } else {
                    mButtonSignUp.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

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

    // Callback for buttons
    @OnClick(R.id.buttonSignUp)
    public void signUp() {

        showProgressBar();

        GeniusFeedService service = NetworkServiceBuilder.createService(GeniusFeedService.class);
        final String email = mEditTextEmail.getText().toString();
        final String username = email;
        final String password = mEditTextPassword.getText().toString();
        Call<ResponseBody> call = service.createNewUser(new RegisterBundle(email, username, password));

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()) {
                    // Go On gettting token
                    Log.d("New user created", "Ok!!!!!!");
                    requestToken(username, password);
                } else {
                    // TODO Feedback to user
                    dismissProgressBar();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                dismissProgressBar();
                //TODO feedback to user
            }
        });
    }

    public void requestToken(String username, String password) {

        // Turn to invisible all login view components
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
                    // Set Token to DB
                    String token = response.body().getAuth_token();
                    Authentication authentication = Authentication.getCredentials();
                    authentication.setToken(token);

                    saveCredentialsToDB();
                } else {
                    // error response, no access to resource?
                    dismissProgressBar();
                    showAlertDialogWithError(0);
                }
            }

            @Override
            public void onFailure(Call<Token> call, Throwable t) {
                // something went completely south (like no internet connection)
                dismissProgressBar();
                showAlertDialogWithError(0);
            }
        });
    }

    public void saveCredentialsToDB() {
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


                // Load Intent
                Intent intent = new Intent(mActivity.getApplicationContext(), MainActivity.class);
                startActivity(intent);
                // Return to normal state
                dismissProgressBar();
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


    @OnClick(R.id.buttonLogin)
    public void changeToLogin() {
        LoginActivity loginActivity = (LoginActivity) mActivity;
        loginActivity.changeToLoginFragment();
    }

    // Validations utils functions
    private boolean validateEmail(String email) {
        String EMAIL_PATTERN = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$";

        Pattern pattern = Pattern.compile(EMAIL_PATTERN, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private boolean validatePassword(String password) {
        String PASSWORD_PATTERN = "[A-Za-z0-9._%+-;]{6,}$";

        Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    // Showing/Hiding progress bar
    private void showProgressBar() {
        mEditTextEmail.setVisibility(EditText.INVISIBLE);
        mEditTextPassword.setVisibility(EditText.INVISIBLE);
        mEditTextRepeatPassword.setVisibility(EditText.INVISIBLE);

        mButtonSignUp.setVisibility(Button.INVISIBLE);
        mButtonLogin.setVisibility(Button.INVISIBLE);

        mProgressBar.setVisibility(ProgressBar.VISIBLE);
    }

    private void dismissProgressBar() {
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        mEditTextEmail.setVisibility(EditText.VISIBLE);
        mEditTextPassword.setVisibility(EditText.VISIBLE);
        mEditTextRepeatPassword.setVisibility(EditText.VISIBLE);

        mButtonSignUp.setVisibility(Button.VISIBLE);
        mButtonLogin.setVisibility(Button.VISIBLE);
    }
}
