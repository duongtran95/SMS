package com.example.trantrungduong95.truesms.Receiver;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Toast;

import com.example.trantrungduong95.truesms.BuildConfig;
import com.example.trantrungduong95.truesms.CustomAdapter.ConversationsAdapter;
import com.example.trantrungduong95.truesms.Model.Contact;
import com.example.trantrungduong95.truesms.Presenter.DefaultAndPermission;
import com.example.trantrungduong95.truesms.MainActivity;
import com.example.trantrungduong95.truesms.Model.Conversation;
import com.example.trantrungduong95.truesms.Model.Message;
import com.example.trantrungduong95.truesms.Presenter.ComposeActivity;
import com.example.trantrungduong95.truesms.Presenter.ConversationActivity;
import com.example.trantrungduong95.truesms.Presenter.Fragment_.Fragment_Filterd;
import com.example.trantrungduong95.truesms.Presenter.NofReceiver;
import com.example.trantrungduong95.truesms.Presenter.PopupActivity;
import com.example.trantrungduong95.truesms.Presenter.SettingsOldActivity;
import com.example.trantrungduong95.truesms.Presenter.SpamDB;
import com.example.trantrungduong95.truesms.Presenter.Utils;
import com.example.trantrungduong95.truesms.Presenter.WidgetProvider;
import com.example.trantrungduong95.truesms.Presenter.isRun;
import com.example.trantrungduong95.truesms.R;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//Listen for new sms.

@SuppressWarnings("deprecation")
public class SmsReceiver extends BroadcastReceiver {

    private static String TAG = "";
    //Uri to get messages from.
    private static Uri URI_SMS = Uri.parse("content://sms/");

    //Uri to get messages from.
    private static Uri URI_MMS = Uri.parse("content://mms/");

    //Intent.action for receiving SMS.
    @SuppressLint("InlinedApi")
    private static String ACTION_SMS_OLD = Telephony.Sms.Intents.SMS_RECEIVED_ACTION;

    @SuppressLint("InlinedApi")
    private static String ACTION_SMS_NEW = Telephony.Sms.Intents.SMS_DELIVER_ACTION;

    //Intent.action for receiving MMS.
    @SuppressLint("InlinedApi")
    private static String ACTION_MMS_OLD = Telephony.Sms.Intents.WAP_PUSH_RECEIVED_ACTION;

    @SuppressLint("InlinedApi")
    private static String ACTION_MMS_MEW = Telephony.Sms.Intents.WAP_PUSH_DELIVER_ACTION;

    //An unreadable MMS body.
    private static String MMS_BODY = "<MMS>";

    //Index: thread id.
    private static int ID_TID = 0;

    //Index: count.
    private static int ID_COUNT = 1;

    //Sort the newest message first.
    private static String SORT = Calls.DATE + " DESC";

    //Delay for spinlock, waiting for new messages.
    private static long SLEEP = 500;


    //Number of maximal spins.
    private static int MAX_SPINS = 15;

    //ID for new message notification.
    private static int NOTIFICATION_ID_NEW = 1;

    //Last unread message's date.
    private static long lastUnreadDate = 0L;

    //Last unread message's body.
    private static String lastUnreadBody = null;

    //Red lights.
    public static int RED = 0xFFFF0000;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (DefaultAndPermission.isDefaultApp(context)) {
            handleOnReceive(this, context, intent);
        }
    }

    @SuppressLint("NewApi")
    private static boolean shouldHandleSmsAction(Context context, String action) {
        return ACTION_SMS_NEW.equals(action) || ACTION_SMS_OLD.equals(action) && ( // handle old action only if:
                Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT // -> is < android 4.4
                        || !BuildConfig.APPLICATION_ID // or not default app
                        .equals(Telephony.Sms.getDefaultSmsPackage(context)));
    }

    public static void handleOnReceive(BroadcastReceiver receiver, Context context, Intent intent) {
        String action = intent.getAction();
        Log.d("onReceive(context), ", action);
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        wakelock.acquire();
        //"got wakelock"
        Log.d("got intent: ", action);
        try {
            Log.d("sleep", SLEEP+"");
            Thread.sleep(SLEEP);
        } catch (InterruptedException e) {
            Log.d("interrupted in spinlock", e.getMessage());
            e.printStackTrace();
        }
        String text;
        if (ComposeActivity.MESSAGE_SENT_ACTION.equals(action)) {
            handleSent(context, intent, receiver.getResultCode());
        } else {
            boolean silent = false;

            if (shouldHandleSmsAction(context, action)) {
                Bundle b = intent.getExtras();
                assert b != null;
                Object[] messages = (Object[]) b.get("pdus");
                SmsMessage[] smsMessage = new SmsMessage[messages.length];
                int lengthMsg = messages.length;
                for (int i = 0; i < lengthMsg; i++) {
                    smsMessage[i] = SmsMessage.createFromPdu((byte[]) messages[i]);
                }
                text = null;
                if (lengthMsg > 0) {
                    // concatenate multipart SMS body
                    StringBuilder sbt = new StringBuilder();
                    for (int i = 0; i < lengthMsg; i++) {
                        sbt.append(smsMessage[i].getMessageBody());
                    }
                    text = sbt.toString();

                    //todo ! Check in blacklist db - filter spam
                    String addr = smsMessage[0].getDisplayOriginatingAddress();

/*                    String[] a = SpamDB.getBlacklist(context);
                    int i = a.length;
                    while (i > 0) {
                        Log.d("t1000", a[i - 1]);
                        if (!a[i - 1].equals(s)) {

                        }
                    }
                    i--;
                    }*/

                    // length body > 50 and number whitout in contacts.
                    if (text.length()>0 && !checkNumberExits(context,addr)) {
                        int i =0;
                        String body = Utils.removeAccent(text);
                        ArrayList<String> arrayList = new ArrayList<>();
                        StringTokenizer st = new StringTokenizer(body);
                        while (st.hasMoreTokens()) {
                            arrayList.add(st.nextToken());
                        }
                        if (arrayList.size() !=0) {
                            for (int a = 0; a < arrayList.size(); a++) {
                                if (Fragment_Filterd.ReadFromfile("filter.txt", context).toLowerCase().contains(arrayList.get(a).toLowerCase())){
                                    i++;
                                }
                            }
                        }
                        if (i >3){
                            silent = true;
                            Toast.makeText(context, context.getString(R.string.filter)+"", Toast.LENGTH_SHORT).show();
                        }
                    }

                //Todo popup
                    SharedPreferences prefs1 = PreferenceManager.getDefaultSharedPreferences(context);
                    if (isRun.isApplicationBroughtToBackground(context)){
                        //backround
                        if (prefs1.getBoolean(SettingsOldActivity.PREFS_POPUP, false)) {
                            Intent intent1 = new Intent(context, PopupActivity.class);
                            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent1.putExtra("body", text);
                            intent1.putExtra("addr", addr);
                            context.startActivity(intent1);
                        }
                    }
                    else {
                        //forgeround
                    }

                    // this code is used to strip a forwarding agent and display the orginated number as sender
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    if (prefs.getBoolean(SettingsOldActivity.PREFS_FORWARD_SMS_CLEAN, false) && text.contains(":")) {
                        Pattern smsPattern = Pattern.compile("([0-9a-zA-Z+]+):");
                        Matcher m = smsPattern.matcher(text);
                        if (m.find()) {
                            addr = m.group(1);
                            //found forwarding sms number
                            Log.d("found forwardin number", addr+"");
                            // now strip the sender from the message
                            Pattern textPattern = Pattern.compile("^[0-9a-zA-Z+]+: (.*)");
                            Matcher m2 = textPattern.matcher(text);
                            if (text.contains(":") && m2.find()) {
                                text = m2.group(1);
                                Log.d(TAG, "stripped the message");
                            }
                        }
                    }

                    if (SpamDB.isBlacklisted(context, smsMessage[0].getOriginatingAddress())) {
                        Log.d("Message from ", addr+ " filtered.");
                        Toast.makeText(context, context.getString(R.string.block)+"", Toast.LENGTH_SHORT).show();
                        silent = true;
                    } else {
                        Log.d("Message from ", addr+ " NOT filtered.");
                    }

                    if (action.equals(ACTION_SMS_NEW)) {
                        // API19+: save message to the database
                        ContentValues values = new ContentValues();
                        values.put("address", addr);
                        values.put("body", text);
                        context.getContentResolver().insert(Uri.parse("content://sms/inbox"), values);
                        Log.d("Insert SMS into db: ", addr+ ", "+ text);
                    }
                }
                updateNotificationsWithNewText(context, text, silent);
            } else if (ACTION_MMS_OLD.equals(action) || ACTION_MMS_MEW.equals(action)) {
                text = MMS_BODY;
                // TODO API19+ MMS code
                updateNotificationsWithNewText(context, text, silent);
            }
        }
        wakelock.release();
        //wakelock released
    }

    private static boolean checkNumberExits(Context context, String s){
        ArrayList<Contact> contacts = new ArrayList<Contact>();
        contacts = getAllContacts(context);
        for (int i = 0;i<contacts.size();i++){
            if (contacts.get(i).getNumber().equals(s))
                return true;
        }
        return false;
    }

    private static ArrayList<Contact> getAllContacts(Context context) {
        Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        ArrayList<Contact> contacts = new ArrayList<Contact>();
        while (phones.moveToNext()) {
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            Contact contact = new Contact(phoneNumber, name);
            contacts.add(contact);
        }
        phones.close();
        return contacts;
    }

    private static void updateNotificationsWithNewText(Context context, String text, boolean silent) {
        if (silent) {
            //ignore notifications for silent text
            return;
        }

        Log.d("text: ", text);
        int count = MAX_SPINS;
        do {
            Log.d("spin: ", count+"");
            try {
                Log.d("sleep(", SLEEP+ ")");
                Thread.sleep(SLEEP);
            } catch (InterruptedException e) {
                Log.d("interrupted inspin lock", e.getMessage());
                e.printStackTrace();
            }
            --count;
        } while (updateNewMessageNotification(context, text) <= 0 && count > 0);

        if (count == 0) { // use messages as they are available
            updateNewMessageNotification(context, null);
        }
    }


    /**
     * Get unread SMS.
     *
     * @param cr   {@link ContentResolver} to query
     * @param text text of the last assumed unread message
     * @return [thread id (-1 if there are more), number of unread messages (-1 if text does not
     * match newest message)]
     */
    private static int[] getUnreadSMS(ContentResolver cr, String text) {
        Log.d("getUnreadSMS(cr, ", text+ ")");
        Cursor cursor = cr.query(URI_SMS, Message.PROJECTION, Message.SELECTION_READ_UNREAD, Message.SELECTION_UNREAD, SORT);

        //Cursor cursor = cr.query(URI_SMS, null, null, null, null);

        if (cursor == null || cursor.isClosed() || cursor.getCount() == 0 || !cursor
                .moveToFirst()) {
            if (text != null) { // try again!
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
                return new int[]{-1, -1};
            } else {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
                return new int[]{0, 0};
            }
        }
        String t = cursor.getString(Message.INDEX_BODY);
        if (text != null && (t == null || !t.startsWith(text))) {
            if (!cursor.isClosed()) {
                cursor.close();
            }
            return new int[]{-1, -1}; // try again!
        }
        long d = cursor.getLong(Message.INDEX_DATE);
        if (d > lastUnreadDate) {
            lastUnreadDate = d;
            lastUnreadBody = t;
        }
        int tid = cursor.getInt(Message.INDEX_THREADID);
        while (cursor.moveToNext() && tid > -1) {
            // check if following messages are from the same thread
            if (tid != cursor.getInt(Message.INDEX_THREADID)) {
                tid = -1;
            }
        }
        int count = cursor.getCount();
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return new int[]{tid, count};
    }

    /**
     * Get unread MMS.
     *
     * @param cr   {@link ContentResolver} to query
     * @param text text of the last assumed unread message
     * @return [thread id (-1 if there are more), number of unread messages]
     */
    private static int[] getUnreadMMS(ContentResolver cr, String text) {
        Log.d("getUnreadMMS(cr, ", text+ ")");
        Cursor cursor = cr.query(URI_MMS, Message.PROJECTION_READ, Message.SELECTION_READ_UNREAD,
                Message.SELECTION_UNREAD, null);
        if (cursor == null || cursor.isClosed() || cursor.getCount() == 0 || !cursor
                .moveToFirst()) {
            if (MMS_BODY.equals(text)) {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
                return new int[]{-1, -1}; // try again!
            } else {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
                return new int[]{0, 0};
            }
        }
        int tid = cursor.getInt(Message.INDEX_THREADID);
        long d = cursor.getLong(Message.INDEX_DATE);
        if (d < MainActivity.MIN_DATE) {
            d *= MainActivity.MILLIS;
        }
        if (d > lastUnreadDate) {
            lastUnreadDate = d;
            lastUnreadBody = null;
        }
        while (cursor.moveToNext() && tid > -1) {
            // check if following messages are from the same thread
            if (tid != cursor.getInt(Message.INDEX_THREADID)) {
                tid = -1;
            }
        }
        int count = cursor.getCount();
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return new int[]{tid, count};
    }

    /**
     * Get unread messages (MMS and SMS).
     *
     * @param cr   {@link ContentResolver} to query
     * @param text text of the last assumed unread message
     * @return [thread id (-1 if there are more), number of unread messages (-1 if text does not
     * match newest message)]
     */
    private static int[] getUnread(ContentResolver cr, String text) {
        try {
            Log.d("getUnread(cr, ", text+ ")");
            lastUnreadBody = null;
            lastUnreadDate = 0L;
            String t = text;
            if (MMS_BODY.equals(t)) {
                t = null;
            }
            int[] retSMS = getUnreadSMS(cr, t);
            if (retSMS[ID_COUNT] == -1) {
                // return to retry
                return new int[]{-1, -1};
            }
            int[] retMMS = getUnreadMMS(cr, text);
            if (retMMS[ID_COUNT] == -1) {
                // return to retry
                return new int[]{-1, -1};
            }
            int[] ret = new int[]{-1, retSMS[ID_COUNT] + retMMS[ID_COUNT]};
            if (retMMS[ID_TID] <= 0 || retSMS[ID_TID] == retMMS[ID_TID]) {
                ret[ID_TID] = retSMS[ID_TID];
            } else if (retSMS[ID_TID] <= 0) {
                ret[ID_TID] = retMMS[ID_TID];
            }
            return ret;
        } catch (SQLiteException e) {
            //unable to get unread messages
            Log.e("unable to get unread", e.getMessage());
            return new int[]{-1, 0};
        }
    }

    /**
     * Update new message {@link Notification}.
     *
     * @param context {@link Context}
     * @param text    text of the last assumed unread message
     * @return number of unread messages
     */
    public static int updateNewMessageNotification(Context context, String text) {
        Log.d("updNewMsgNoti(", context+ ","+ text+ ")");
        NotificationManager mNotificationMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean enableNotifications = prefs.getBoolean(SettingsOldActivity.PREFS_NOTIFICATION_ENABLE, true);
        boolean privateNotification = prefs.getBoolean(SettingsOldActivity.PREFS_NOTIFICATION_PRIVACY, false);
        boolean showPhoto = !privateNotification && prefs.getBoolean(SettingsOldActivity.PREFS_CONTACT_PHOTO, true);
        if (!enableNotifications) {
            mNotificationMgr.cancelAll();
            //no notification needed!
        }
        int[] status = getUnread(context.getContentResolver(), text);
        int tcount = status[ID_COUNT];
        int tid = status[ID_TID];

        // FIXME l is always -1..
        Log.d( "l: ", tcount+"");
        if (tcount < 0) {
            return tcount;
        }

        if (enableNotifications && (text != null || tcount == 0)) {
            mNotificationMgr.cancel(NOTIFICATION_ID_NEW);
        }
        Uri uri;
        PendingIntent pIntent;
        if (tcount == 0) {
            Intent i = new Intent(context, MainActivity.class);
            // add pending intent
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            pIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        } else {
            NotificationCompat.Builder nb = new NotificationCompat.Builder(context);
            boolean showNotification = true;
            Intent i;
            if (tid >= 0) {
                uri = Uri.parse(ConversationActivity.URI + tid);
                i = new Intent(Intent.ACTION_VIEW, uri, context, ConversationActivity.class);
                pIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

                if (enableNotifications) {
                    Conversation conv = Conversation.getConversation(context, tid, true);
                    if (conv != null) {
                        String a;
                        if (privateNotification) {
                            if (tcount == 1) {
                                a = context.getString(R.string.new_message_);
                            } else {
                                a = context.getString(R.string.new_messages_);
                            }
                        } else {
                            a = conv.getContact().getDisplayName();
                        }
                        showNotification = true;
                        nb.setSmallIcon(SettingsOldActivity.getNotificationIcon(context));
                        nb.setTicker(a);
                        nb.setWhen(lastUnreadDate);
                        if (tcount == 1) {
                            String body;
                            if (privateNotification) {
                                body = context.getString(R.string.new_message);
                            } else {
                                body = lastUnreadBody;
                            }
                            if (body == null) {
                                body = context.getString(R.string.mms_conversation);
                            }
                            nb.setContentTitle(a);
                            nb.setContentText(body);
                            nb.setContentIntent(pIntent);
                            // add long text
                            nb.setStyle(new NotificationCompat.BigTextStyle().bigText(body));

                            // add actions
                            Intent nextIntent = new Intent(NofReceiver.ACTION_MARK_READ);
                            nextIntent.putExtra(NofReceiver.EXTRA_MURI, uri.toString());
                            PendingIntent nextPendingIntent = PendingIntent.getBroadcast(context, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                            nb.addAction(R.drawable.ic_menu_mark, context.getString(R.string.mark_read_), nextPendingIntent);
                            nb.addAction(R.drawable.ic_menu_compose, context.getString(R.string.reply), pIntent);
                        } else {
                            nb.setContentTitle(a);
                            nb.setContentText(context.getString(R.string.new_messages, tcount));
                            nb.setContentIntent(pIntent);
                        }
                        if (showPhoto )// just for the speeeeed)
                        {
                            try {
                                conv.getContact().update(context, false, true);
                            } catch (NullPointerException e) {
                                Log.e("updating contact failed", e.getMessage());
                            }
                            Drawable d = conv.getContact().getAvatar(context, null);
                            if (d instanceof BitmapDrawable) {
                                Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
                                // 24x24 dp according to android iconography  ->
                                // http://developer.android.com/design/style/iconography.html#notification
                                int px = Math.round(TypedValue
                                        .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64,
                                                context.getResources().getDisplayMetrics()));
                                nb.setLargeIcon(Bitmap.createScaledBitmap(bitmap, px, px, false));
                            }
                        }
                    }
                }
            } else {
                uri = Uri.parse(ConversationActivity.URI);
                i = new Intent(Intent.ACTION_VIEW, uri, context, MainActivity.class);
                pIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

                if (enableNotifications) {
                    showNotification = true;
                    nb.setSmallIcon(SettingsOldActivity.getNotificationIcon(context));
                    nb.setTicker(context.getString(R.string.new_messages_));
                    nb.setWhen(lastUnreadDate);
                    nb.setContentTitle(context.getString(R.string.new_messages_));
                    nb.setContentText(context.getString(R.string.new_messages, tcount));
                    nb.setContentIntent(pIntent);
                    nb.setNumber(tcount);
                }
            }
            // add pending intent
            i.setFlags(i.getFlags() | Intent.FLAG_ACTIVITY_NEW_TASK);

            if (enableNotifications && showNotification) {
                int[] ledFlash = SettingsOldActivity.getLEDflash(context);
                nb.setLights(SettingsOldActivity.getLEDcolor(context), ledFlash[0], ledFlash[1]);
                SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
                if (text != null) {
                    boolean vibrate = p.getBoolean(SettingsOldActivity.PREFS_VIBRATE, false);
                    String s = p.getString(SettingsOldActivity.PREFS_SOUND, null);
                    Uri sound;
                    if (s == null || s.length() <= 0) {
                        sound = null;
                    } else {
                        sound = Uri.parse(s);
                    }
                    if (vibrate) {
                        long[] pattern = SettingsOldActivity.getVibratorPattern(context);
                        if (pattern.length == 1 && pattern[0] == 0) {
                            nb.setDefaults(Notification.DEFAULT_VIBRATE);
                        } else {
                            nb.setVibrate(pattern);
                        }
                    }
                    nb.setSound(sound);
                }
            }
            Log.d("uri: ", uri+"");
            mNotificationMgr.cancel(NOTIFICATION_ID_NEW);
            if (enableNotifications && showNotification) {
                try {
                    mNotificationMgr.notify(NOTIFICATION_ID_NEW, nb.getNotification());
                } catch (IllegalArgumentException e) {
                    Log.e("illegal notification: ", nb+" "+ e.getMessage());
                }
            }
        }
        Log.d("return ", tcount+ " (2)");
        //noinspection ConstantConditions
        AppWidgetManager.getInstance(context).updateAppWidget(new ComponentName(context, WidgetProvider.class), WidgetProvider.getRemoteViews(context, tcount, pIntent));
        return tcount;
    }

    //Update failed message notification.
    private static void updateFailedNotification(Context context, Uri uri) {
        Log.d("updateFailedNotf: ", uri+"");
        Cursor c = context.getContentResolver().query(uri, Message.PROJECTION_SMS, null, null, null);
        if (c != null && c.moveToFirst()) {
            int id = c.getInt(Message.INDEX_ID);
            int tid = c.getInt(Message.INDEX_THREADID);
            String body = c.getString(Message.INDEX_BODY);
            long date = c.getLong(Message.INDEX_DATE);

            Conversation conv = Conversation.getConversation(context, tid, true);

            NotificationManager mNotificationMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
            boolean privateNotification = p.getBoolean(SettingsOldActivity.PREFS_NOTIFICATION_PRIVACY, false);
            Intent intent;
            if (conv == null) {
                intent = new Intent(Intent.ACTION_VIEW, null, context, ComposeActivity.class);
            } else {
                intent = new Intent(Intent.ACTION_VIEW, conv.getUri(), context,
                        ConversationActivity.class);
            }
            intent.putExtra(Intent.EXTRA_TEXT, body);

            String title = context.getString(R.string.error_sending_failed);

            int[] ledFlash = SettingsOldActivity.getLEDflash(context);
            NotificationCompat.Builder b = new NotificationCompat.Builder(context)
                    .setSmallIcon(android.R.drawable.stat_sys_warning)
                    .setTicker(title)
                    .setWhen(date)
                    .setAutoCancel(true)
                    .setLights(RED, ledFlash[0], ledFlash[1])
                    .setContentIntent(PendingIntent.getActivity(context, 0,
                            intent, PendingIntent.FLAG_CANCEL_CURRENT));
            String text;
            if (privateNotification) {
                title += "!";
                text = "";
            } else if (conv == null) {
                title += "!";
                text = body;
            } else {
                title += ": " + conv.getContact().getDisplayName();
                text = body;
            }
            b.setContentTitle(title);
            b.setContentText(text);
            String s = p.getString(SettingsOldActivity.PREFS_SOUND, null);
            if (!TextUtils.isEmpty(s)) {
                b.setSound(Uri.parse(s));
            }
            boolean vibrate = p.getBoolean(SettingsOldActivity.PREFS_VIBRATE, false);
            if (vibrate) {
                long[] pattern = SettingsOldActivity.getVibratorPattern(context);
                if (pattern.length > 1) {
                    b.setVibrate(pattern);
                }
            }

            mNotificationMgr.notify(id, b.build());
        }
        if (c != null && !c.isClosed()) {
            c.close();
        }
    }

    //Handle sent message.
    private static void handleSent(Context context, Intent intent,
                                   int resultCode) {
        Uri uri = intent.getData();
        Log.d("sent message: ", uri+ ", rc: "+ resultCode);
        if (uri == null) {
            //handleSent(null)
            return;
        }

        if (resultCode == Activity.RESULT_OK) {
            ContentValues cv = new ContentValues(1);
            cv.put(ComposeActivity.TYPE, Message.SMS_OUT);
            context.getContentResolver().update(uri, cv, null, null);
        } else {
            updateFailedNotification(context, uri);
        }
    }
}
