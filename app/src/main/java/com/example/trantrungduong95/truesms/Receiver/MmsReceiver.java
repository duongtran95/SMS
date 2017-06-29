package com.example.trantrungduong95.truesms.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.trantrungduong95.truesms.Presenter.DefaultAndPermission;
import com.example.trantrungduong95.truesms.Receiver.SmsReceiver;

//Listen for new mms.

public class MmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (DefaultAndPermission.isDefaultApp(context)) {
            SmsReceiver.handleOnReceive(this, context, intent);
        }
    }
}
