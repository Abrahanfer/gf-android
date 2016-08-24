package me.abrahanfer.geniusfeed;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import me.abrahanfer.geniusfeed.utils.network.GeniusFeedService;
import me.abrahanfer.geniusfeed.utils.network.NetworkServiceBuilder;
import me.abrahanfer.geniusfeed.utils.network.bodyclass.RegisterBundle;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
                    mEditTextPassword.setError(getResources().getString(R.string.email_error_validation));
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
                Log.d("Mirando la cadena","S string: " + s.toString() + "Pasword anterior: " + mEditTextPassword.getText().toString());
                if(!validatePassword(s.toString())
                    || !mEditTextPassword.getText().toString().equals(s.toString())) {
                    mButtonSignUp.setEnabled(false);
                    mEditTextRepeatPassword.setError(getResources().getString(R.string.email_error_validation));
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
        String email = mEditTextEmail.getText().toString();
        String username = email;
        String password = mEditTextPassword.getText().toString();
        Call<ResponseBody> call = service.createNewUser(new RegisterBundle(email, username, password));

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                dismissProgressBar();
                if(response.isSuccessful()) {
                    // TODO Go On gettting token
                    Log.d("New user created", "Ok!!!!!!");
                } else {
                    // TODO Feedback to user
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                dismissProgressBar();
                //TODO feedback to user
            }
        });
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
