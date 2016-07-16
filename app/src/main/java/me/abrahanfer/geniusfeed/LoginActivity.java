package me.abrahanfer.geniusfeed;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.sql.BatchUpdateException;

import me.abrahanfer.geniusfeed.utils.Authentication;
import me.abrahanfer.geniusfeed.utils.GeniusFeedContract;

public class LoginActivity extends AppCompatActivity {

    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up progressBar
        mProgressBar = (ProgressBar) findViewById(R.id
                                                          .pbLoading);
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

    public void setCredentials(View view){
        Authentication authentication = new Authentication("test-user-1","test1");
        Authentication.setCredentials(authentication);

        saveCredentialsToDB();
    }

    public void saveCredentialsToDB() {
        // Turn to invisible all login view components
        final TextView usernameLabel = (TextView) findViewById(R.id.loginLabelUsername);
        final EditText usernameEdit = (EditText) findViewById(R.id.editTextUsername);
        final TextView passwordLabel = (TextView) findViewById(R.id.loginLabelPassword);
        final EditText passwordEdit = (EditText) findViewById(R.id.editTextPassword);

        final Button buttonLogin = (Button) findViewById(R.id.buttonLogin);

        usernameLabel.setVisibility(TextView.INVISIBLE);
        usernameEdit.setVisibility(EditText.INVISIBLE);
        passwordLabel.setVisibility(TextView.INVISIBLE);
        passwordEdit.setVisibility(EditText.INVISIBLE);
        buttonLogin.setVisibility(Button.INVISIBLE);

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
                Log.e("LoginActivity", "Error con barra de progreso 4");
                Authentication auth = Authentication.getCredentials();
                // Define projector
                String[] projection = {
                        GeniusFeedContract.User.COLUMN_NAME_USER_ID,
                        GeniusFeedContract.User.COLUMN_NAME_USERNAME,
                        GeniusFeedContract.User.COLUMN_NAME_PASSWORD
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
                Log.e("LoginActivity", "Error con barra de progreso 5");
                if (c.getCount() > 0) {
                    // TODO make delete query
                    dataBase.delete(GeniusFeedContract.User.TABLE_NAME, "1", null);
                }
                // TODO make insert query
                // Create a new map of values, where column names are the keys
                ContentValues values = new ContentValues();
                values.put(GeniusFeedContract.User.COLUMN_NAME_USERNAME, auth.getUsername());
                values.put(GeniusFeedContract.User.COLUMN_NAME_PASSWORD, auth.getPassword());
                Log.e("LoginActivity", "Error con barra de progreso 6");
                // Insert the new row, returning the primary key value of the new row
                long newRowId;
                newRowId = dataBase.insert(GeniusFeedContract.User.TABLE_NAME, "null",
                                           //GeniusFeedContract.User.COLUMN_NAME_USER_ID,
                                           values);


                // Return to normal state
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);

                usernameLabel.setVisibility(TextView.VISIBLE);
                usernameEdit.setVisibility(EditText.VISIBLE);
                passwordLabel.setVisibility(TextView.VISIBLE);
                passwordEdit.setVisibility(EditText.VISIBLE);
                buttonLogin.setVisibility(Button.VISIBLE);
                // Load Intent
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        }

        new WriteUserCrendetialsToDB().execute();
    }
}
