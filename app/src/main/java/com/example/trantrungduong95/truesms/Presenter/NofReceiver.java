package com.example.trantrungduong95.truesms.Presenter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.example.trantrungduong95.truesms.MainActivity;

//Receives intents from notifications.
public class NofReceiver extends BroadcastReceiver {
    public static String ACTION_MARK_READ = "com.example.trantrungduong95.truesms.Presenter.MARK_READ";

    public static String EXTRA_MURI = "com.example.trantrungduong95.truesms.Presenter.MURI_KEY";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (ACTION_MARK_READ.equals(intent.getAction())) {
            try {
                Bundle extras = intent.getExtras();
                if (extras == null) {
                    //empty extras
                    return;
                }
                // remember that we have to add the package here ..
                String muri = extras.getString(EXTRA_MURI);

                MainActivity.markRead(context, Uri.parse(muri), 1);

            } catch (Exception e) {
                //unable to mark message read
            }
        }
    }
}
