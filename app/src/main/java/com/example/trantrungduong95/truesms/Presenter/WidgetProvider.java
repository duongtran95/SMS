package com.example.trantrungduong95.truesms.Presenter;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.RemoteViews;

import com.example.trantrungduong95.truesms.R;
import com.example.trantrungduong95.truesms.Receiver.SmsReceiver;

//A widget provider.
public class WidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        SmsReceiver.updateNewMessageNotification(context, null);
    }

    //Get link RemoteViews. Count number of unread messages
    public static RemoteViews getRemoteViews(Context context, int count,
                                             PendingIntent pIntent) {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
        views.setTextViewText(R.id.text1, String.valueOf(count));
        if (count == 0) {
            views.setViewVisibility(R.id.text1, View.GONE);
        } else {
            views.setViewVisibility(R.id.text1, View.VISIBLE);
        }
        if (p.getBoolean(SettingsOldActivity.PREFS_HIDE_WIDGET_LABEL, false)) {
            views.setViewVisibility(R.id.label, View.GONE);
        } else {
            views.setViewVisibility(R.id.label, View.VISIBLE);
        }
        if (pIntent != null) {
            views.setOnClickPendingIntent(R.id.widget, pIntent);
            //set pending intent
        }
        return views;
    }
}
