package me.abrahanfer.geniusfeed.utils.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by abrahan on 14/09/16.
 */

public class ConnectivityEventsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Handle event receive
        Log.e("EVENT RECEIVER", "Se recive evento");
    }
}
