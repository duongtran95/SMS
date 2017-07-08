package com.example.trantrungduong95.truesms.Presenter;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Telephony;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.example.trantrungduong95.truesms.BuildConfig;
import com.example.trantrungduong95.truesms.R;

public class DefaultAndPermission extends Application {
    //Tag
    private static String TAG = "TrueSMS";

    //Projection for checking link Cursor.
    private String[] PROJECTION = new String[]{"_id"};

    @Override
    public void onCreate() {
        try {
            Class.forName("android.os.AsyncTask");
        } catch (Throwable ignore) {
        }

        super.onCreate();
        if (hasPermission(this, Manifest.permission.READ_SMS)) {
            SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
            int state = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
            try {
                Cursor c = getContentResolver().query(ComposeActivity.URI_SENT, PROJECTION, null, null, "_id LIMIT 1");
                if (c == null) {
                    Log.i(TAG, "disable .Sender: cursor=null");
                } else if (SmsManager.getDefault() == null) {
                    Log.i(TAG, "disable .Sender: SmsManager=null");
                } else {
                    state = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
                    Log.d(TAG, "enable .Sender");
                }
                if (c != null && !c.isClosed()) {
                    c.close();
                }
            } catch (IllegalArgumentException | SQLiteException e) {
                Log.e("disable .Sender: ", e.getMessage());
            }
            getPackageManager().setComponentEnabledSetting(new ComponentName(this, ComposeActivity.class), state, PackageManager.DONT_KILL_APP);
        } else {
        Log.d(TAG, "ignore .Sender state, READ_SMS permission is missing to check default app");
        }
    }

    // Get an link OnClickListener for stating an Activity for given link Intent.
    public static OnClickListener getOnClickStartActivity(final Context context, final Intent intent) {
        if (intent == null) {
            return null;
        }
        return new OnClickListener() {
            @Override
            public void onClick(final View v) {
                try {
                    context.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Log.w(TAG, "activity not found", e);
                    Toast.makeText(context, "no activity for data: " + intent.getType(),
                            Toast.LENGTH_LONG).show();
                }
            }
        };
    }

    public static boolean isDefaultApp(Context context) {
        // there is no default sms app before android 4.4
        if (Build.VERSION.SDK_INT < 19) {
            return true;
        }
        try {
            String smsPackage = Telephony.Sms.getDefaultSmsPackage(context);
            return smsPackage == null || smsPackage.equals(BuildConfig.APPLICATION_ID);
        } catch (SecurityException e) {
            Log.e(TAG, "failed to query default SMS app", e);
            return true;
        }
    }

    public static boolean hasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean requestPermission(final Activity activity, final String permission, final int requestCode, int message, DialogInterface.OnClickListener onCancelListener) {
        Log.i(TAG, "requesting permission: " + permission);
        if (!hasPermission(activity, permission)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                new AlertDialog.Builder(activity)
                        .setTitle(R.string.permissions_)
                        .setMessage(message)
                        .setCancelable(false)
                        .setNegativeButton(android.R.string.cancel, onCancelListener)
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface,int i) {
                                        ActivityCompat.requestPermissions(activity,
                                                new String[]{permission}, requestCode);
                                    }
                                })
                        .show();
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
            }
            return false;
        } else {
            return true;
        }
    }
}
