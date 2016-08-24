package me.abrahanfer.geniusfeed;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

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
        mBaseView = inflater.inflate(R.layout.signin_fragment, container, false);
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

    // Callback for buttons
    @OnClick(R.id.buttonSignUp)
    public void signUp() {

    }

    @OnClick(R.id.buttonLogin)
    public void changeToLogin() {
        LoginActivity loginActivity = (LoginActivity) mActivity;
        loginActivity.changeToLoginFragment();
    }
}
