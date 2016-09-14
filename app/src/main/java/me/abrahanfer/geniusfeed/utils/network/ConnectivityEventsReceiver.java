package me.abrahanfer.geniusfeed.utils.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import java.util.Map;

import me.abrahanfer.geniusfeed.NetworkStatusFeedbackInterface;

/**
 * Created by abrahan on 14/09/16.
 */

public class ConnectivityEventsReceiver extends BroadcastReceiver {

    public static Boolean networkConnected = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        // Handle event receive
        handleChangeInNetwork(context, intent);
    }

    private void handleChangeInNetwork(Context context, Intent intent) {
        String keyNetwortkInfo = "networkInfo";
        String keyState = "state";
        Bundle extras = intent.getExtras();

        if (extras != null) {
            for (String key: extras.keySet()) {
                if (key.equals(keyNetwortkInfo)) {
                    NetworkInfo netInfo = (NetworkInfo)extras.get(key);
                    Log.e("Mirando el STATE", "" + netInfo.getDetailedState());
                    if (netInfo.getDetailedState().equals(NetworkInfo.DetailedState.CONNECTED) && !networkConnected) {
                        networkConnected = true;
                    } else if (netInfo.getDetailedState().equals(NetworkInfo.DetailedState.DISCONNECTED) && networkConnected) {
                        networkConnected = false;
                        showAlertInActivity(context);
                    }
                }
            }
        }
    }

    private void showAlertInActivity(Context context) {
        NetworkStatusFeedbackInterface activity = (NetworkStatusFeedbackInterface) context;
        activity.showAlertMessages(5);
    }
}
