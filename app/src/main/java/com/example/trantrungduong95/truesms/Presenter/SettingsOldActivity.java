package com.example.trantrungduong95.truesms.Presenter;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.example.trantrungduong95.truesms.CustomAdapter.DrawDialog;
import com.example.trantrungduong95.truesms.MainActivity;
import com.example.trantrungduong95.truesms.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.PatternSyntaxException;

//Setting
public class SettingsOldActivity extends PreferenceActivity implements IPreferenceContainer {
    //Preference's name: vibrate on receive.
    public static String PREFS_VIBRATE = "receive_vibrate";

    //Preference's name: sound on receive.

    public static String PREFS_SOUND = "receive_sound";

    //Preference's name: led color.
    private static String PREFS_LED_COLOR = "receive_led_color";

    //Preference's name: led flash.
    private static String PREFS_LED_FLASH = "receive_led_flash";

    //Preference's name: vibrator pattern.
    private static String PREFS_VIBRATOR_PATTERN = "receive_vibrate_mode";

    //Preference's name: enable notifications.
    public static String PREFS_NOTIFICATION_ENABLE = "notification_enable";

    //Preference's name: popup
    public static String PREFS_POPUP = "receive_popup";

    //Preference's name: hide sender/text in notifications.
    public static String PREFS_NOTIFICATION_PRIVACY = "receive_privacy";

    //Preference's name: icon for notifications.
    private static String PREFS_NOTIFICATION_ICON = "notification_icon";

    //Prefernece's name: show contact's photo.
    public static String PREFS_CONTACT_PHOTO = "show_contact_photo";

    //Preference's name: show emoticons in messagelist.
    public static String PREFS_EMOTICONS = "show_emoticons";

    //Preference's name: bubbles for incoming messages.
    private static String PREFS_BUBBLES_IN = "bubbles_in";

    //Preference's name: bubbles for outgoing messages.
    private static String PREFS_BUBBLES_OUT = "bubbles_out";

    //Preference's name: show full date and time.
    public static String PREFS_FULL_DATE = "show_full_date";

    //Preference's name: hide restore.
    public static String PREFS_HIDE_RESTORE = "hide_restore";

    //Preference's name: hide widget's label.
    public static String PREFS_HIDE_WIDGET_LABEL = "hide_widget_label";

    //Preference's name: hide delete all threads.
    public static String PREFS_HIDE_DELETE_ALL_THREADS = "hide_delete_all_threads";

    //Preference's name: hide message count.
    public static String PREFS_HIDE_MESSAGE_COUNT = "hide_message_count";

    //Preference's name: theme.
    private static String PREFS_THEME = "theme";

    //Theme: black.
    private static String THEME_BLACK = "black";

    //Preference's name: text size.
    private static String PREFS_TEXTSIZE = "textsizen";

    //Preference's name: text color.
    private static String PREFS_TEXTCOLOR = "textcolor";

    //Preference's name: ignore text color for list ov threads.
    private static String PREFS_TEXTCOLOR_IGNORE_CONV = "text_color_ignore_conv";

    //Preference's name: enable autosend.
    public static String PREFS_ENABLE_AUTOSEND = "enable_autosend";

    //Preference's name: mobile_only.
    public static String PREFS_MOBILE_ONLY = "mobile_only";

    //Preference's name: edit_short_text.
    public static String PREFS_EDIT_SHORT_TEXT = "edit_short_text";

    //Preference's name: show target app.
    public static String PREFS_SHOWTARGETAPP = "show_target_app";

    //Preference's name: backup of last sms.
    public static String PREFS_BACKUPLASTTEXT = "backup_last_sms";

    //Preference's name: decode decimal ncr.
    public static String PREFS_DECODE_DECIMAL_NCR = "decode_decimal_ncr";

    //Preference's name: activate sender.
    public static String PREFS_ACTIVATE_SENDER = "activate_sender";

    //Preference's name: forward sms sender.
    public static String PREFS_FORWARD_SMS_CLEAN = "forwarded_sms_clean";

    //Preference's name: prefix regular expression.
    private static String PREFS_REGEX = "regex";

//    /Preference's name: prefix replace.
    private static String PREFS_REPLACE = "replace";

    //Number of regular expressions.
    private static int PREFS_REGEX_COUNT = 3;

    //Default color.
    private static int BLACK = 0xff000000;

    //Drawable resources for notification icons.
    private static int[] NOTIFICAION_IMG = new int[]{R.drawable.stat_notify_sms_purple,
            R.drawable.stat_notify_sms_blue, R.drawable.stat_notify_sms_red,
            R.drawable.stat_notify_sms_black, R.drawable.stat_notify_sms_green,
            R.drawable.stat_notify_sms_yellow};

    //String resources for notification icons.
    private static int[] NOTIFICAION_STR = new int[]{R.string.notification_default_,
            R.string.notification_blue_, R.string.notification_red_,
            R.string.notification_black_, R.string.notification_green_,
            R.string.notification_yellow_,};
    //Todo purple
    //Drawable resources for bubbles.
    private static int[] BUBBLES_IMG = new int[]{0, R.drawable.gray_dark,
            R.drawable.gray_light, R.drawable.bubble_old_green_left,
            R.drawable.bubble_old_green_right, R.drawable.bubble_old_turquoise_left,
            R.drawable.bubble_old_turquoise_right, R.drawable.bubble_blue_left,
            R.drawable.bubble_blue_right, R.drawable.bubble_blue2_left,
            R.drawable.bubble_blue2_right, R.drawable.bubble_brown_left,
            R.drawable.bubble_brown_right, R.drawable.bubble_gray_left,
            R.drawable.bubble_gray_right, R.drawable.bubble_green_left,
            R.drawable.bubble_green_right, R.drawable.bubble_green2_left,
            R.drawable.bubble_green2_right, R.drawable.bubble_orange_left,
            R.drawable.bubble_orange_right, R.drawable.bubble_pink_left,
            R.drawable.bubble_pink_right, R.drawable.bubble_purple_left,
            R.drawable.bubble_purple_right, R.drawable.bubble_white_left,
            R.drawable.bubble_white_right, R.drawable.bubble_yellow_left,
            R.drawable.bubble_yellow_right,};

    //String resources for bubbles.
    private static int[] BUBBLES_STR = new int[]{R.string.bubbles_nothing,
            R.string.bubbles_plain_dark_gray, R.string.bubbles_plain_light_gray,
            R.string.bubbles_old_green_left, R.string.bubbles_old_green_right,
            R.string.bubbles_old_turquoise_left, R.string.bubbles_old_turquoise_right,
            R.string.bubbles_blue_left, R.string.bubbles_blue_right, R.string.bubbles_blue2_left,
            R.string.bubbles_blue2_right, R.string.bubbles_brown_left,
            R.string.bubbles_brown_right, R.string.bubbles_gray_left, R.string.bubbles_gray_right,
            R.string.bubbles_green_left, R.string.bubbles_green_right,
            R.string.bubbles_green2_left, R.string.bubbles_green2_right,
            R.string.bubbles_orange_left, R.string.bubbles_orange_right,
            R.string.bubbles_pink_left, R.string.bubbles_pink_right, R.string.bubbles_purple_left,
            R.string.bubbles_purple_right, R.string.bubbles_white_left,
            R.string.bubbles_white_right, R.string.bubbles_yellow_left,
            R.string.bubbles_yellow_right,};

    // Listen to clicks on "notification icon" preferences.x
    private static class OnNotificationIconClickListener implements OnPreferenceClickListener {
        private Context ctx;

        //Default constructor.
        private OnNotificationIconClickListener(Context context) {
            ctx = context;
        }

        @Override
        public boolean onPreferenceClick(final Preference preference) {
            Builder b = new Builder(ctx);
            int l = NOTIFICAION_STR.length;
            String[] cols = new String[]{"icon", "text"};
            ArrayList<HashMap<String, Object>> rows
                    = new ArrayList<>();
            for (int i = 0; i < l; i++) {
                HashMap<String, Object> m = new HashMap<>(2);
                m.put(cols[0], NOTIFICAION_IMG[i]);
                m.put(cols[1], ctx.getString(NOTIFICAION_STR[i]));
                rows.add(m);
            }
            b.setAdapter(new SimpleAdapter(ctx, rows, R.layout.notification_icons_item, cols,
                            new int[]{android.R.id.icon, android.R.id.text1}),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            preference.getEditor().putInt(preference.getKey(), which).commit();
                        }
                    });
            b.setNegativeButton(android.R.string.cancel, null);
            b.show();
            return true;
        }
    }

    //Listen to clicks on "bubble" preferences.
    private static class OnBubblesClickListener implements OnPreferenceClickListener {
        private Context ctx;

        //Default constructor.
        public OnBubblesClickListener(Context context) {
            ctx = context;
        }

        @Override
        public boolean onPreferenceClick(final Preference preference) {
            Builder b = new Builder(ctx);
            int l = BUBBLES_STR.length;
            String[] cols = new String[]{"icon", "text"};
            ArrayList<HashMap<String, Object>> rows
                    = new ArrayList<>();
            for (int i = 0; i < l; i++) {
                HashMap<String, Object> m = new HashMap<>(2);
                m.put(cols[0], BUBBLES_IMG[i]);
                m.put(cols[1], ctx.getString(BUBBLES_STR[i]));
                rows.add(m);
            }
            b.setAdapter(new SimpleAdapter(ctx, rows, R.layout.bubbles_item, cols, new int[]{android.R.id.icon, android.R.id.text1}),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            preference.getEditor().putInt(preference.getKey(), which).commit();
                        }
                    });
            b.setNegativeButton(android.R.string.cancel, null);
            b.show();
            return true;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs_appearance_behavior);
        addPreferencesFromResource(R.xml.prefs_about);

        registerOnPreferenceClickListener(this);
    }


     //Register link OnPreferenceClickListener.
    // param pc link IPreferenceContainer.

    public static void registerOnPreferenceClickListener(final IPreferenceContainer pc) {
        Preference p = pc.findPreference(PREFS_NOTIFICATION_ICON);
        if (p != null) {
            p.setOnPreferenceClickListener(new OnNotificationIconClickListener(pc.getContext()));
        }

        Preference pbi = pc.findPreference(PREFS_BUBBLES_IN);
        Preference pbo = pc.findPreference(PREFS_BUBBLES_OUT);
        if (pbi != null || pbo != null) {
            OnBubblesClickListener obcl = new OnBubblesClickListener(pc.getContext());

            if (pbi != null) {
                pbi.setOnPreferenceClickListener(obcl);
            }
            if (pbo != null) {
                pbo.setOnPreferenceClickListener(obcl);
            }
        }

        p = pc.findPreference(PREFS_TEXTCOLOR);
        if (p != null) {
            p.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    final SharedPreferences prefs = PreferenceManager
                            .getDefaultSharedPreferences(pc.getContext());

                    int c = prefs.getInt(PREFS_TEXTCOLOR, 0);
                    if (c == 0) {
                        c = BLACK;
                    }

                    DrawDialog dialog = new DrawDialog(pc.getContext(), c,
                            new DrawDialog.OnListener() {
                                @Override
                                public void onOk(DrawDialog dialog, int color) {
                                    prefs.edit().putInt(PREFS_TEXTCOLOR, color).apply();
                                }

                                @Override
                                public void onCancel(DrawDialog dialog) {
                                    // nothing to do
                                }

                                public void onReset(DrawDialog dialog) {
                                    prefs.edit().putInt(PREFS_TEXTCOLOR, 0).apply();
                                }
                            });

                    dialog.show();
                    return true;
                }
            });
        }
    }

    //Get Theme from Preferences.
    public static int getTheme(Context context) {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
        String s = p.getString(PREFS_THEME, null);
        if (s != null && THEME_BLACK.equals(s)) {
            return R.style.Theme_TrueSMS;
        } else {
            return R.style.Theme_TrueSMS_Light;
        }
    }

    //Get text's size from Preferences.
    public static int getTextsize(Context context) {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
        String s = p.getString(PREFS_TEXTSIZE, null);
        return Utils.parseInt(s, 0);
    }

    //Get text's color from Preferences.
    public static int getTextcolor(Context context) {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
        if (context instanceof MainActivity && p.getBoolean(PREFS_TEXTCOLOR_IGNORE_CONV, false)) {
            return 0;
        }
        int ret = p.getInt(PREFS_TEXTCOLOR, 0);
        Log.d("text color: ", ret+"");
        return ret;
    }

    //Get LED color pattern from Preferences.
    public static int getLEDcolor(Context context) {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
        String s = p.getString(PREFS_LED_COLOR, "65280");
        return Integer.parseInt(s);
    }

    //Get LED flash pattern from Preferences.
    public static int[] getLEDflash(Context context) {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
        String s = p.getString(PREFS_LED_FLASH, "500_2000");
        String[] ss = s.split("_");
        int[] ret = new int[2];
        ret[0] = Integer.parseInt(ss[0]);
        ret[1] = Integer.parseInt(ss[1]);
        return ret;
    }

    //Get vibrator pattern from Preferences.
    public static long[] getVibratorPattern(Context context) {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
        String s = p.getString(PREFS_VIBRATOR_PATTERN, "0");
        String[] ss = s.split("_");
        int l = ss.length;
        long[] ret = new long[l];
        for (int i = 0; i < l; i++) {
            ret[i] = Long.parseLong(ss[i]);
        }
        return ret;
    }

    //Get drawable resource for notification icon.
    public static int getNotificationIcon(Context context) {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
        int i = p.getInt(PREFS_NOTIFICATION_ICON, R.drawable.stat_notify_sms_purple);
        if (i >= 0 && i < NOTIFICAION_IMG.length) {
            return NOTIFICAION_IMG[i];
        }
        return R.drawable.stat_notify_sms_purple;
    }

    //Get drawable resource for bubble for incoming messages.
    public static int getBubblesIn(Context context) {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
        int i = p.getInt(PREFS_BUBBLES_IN, R.drawable.bubble_old_turquoise_left);
        if (i >= 0 && i < BUBBLES_IMG.length) {
            return BUBBLES_IMG[i];
        }
        return R.drawable.bubble_old_turquoise_left;
    }

    //Get drawable resource for bubble for outgoing messages.
    public static int getBubblesOut(Context context) {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
        int i = p.getInt(PREFS_BUBBLES_OUT, R.drawable.bubble_old_green_right);
        if (i >= 0 && i < BUBBLES_IMG.length) {
            return BUBBLES_IMG[i];
        }
        return R.drawable.bubble_old_green_right;
    }

    //Get text's size from Preferences.
    public static boolean decodeDecimalNCR(Context context) {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
        boolean b = p.getBoolean(PREFS_DECODE_DECIMAL_NCR, true);
        Log.d("decode decimal ncr: ", b+"");
        return b;
    }

    //Get the emoticons show state
    public static boolean showEmoticons(Context context) {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
        return p.getBoolean(PREFS_EMOTICONS, true);
    }

    //Fix a number with regex load from link SharedPreferences.
    public static String fixNumber(Context context, String number) {
        String ret = number;
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
        for (int i = 1; i <= PREFS_REGEX_COUNT; i++) {
            String regex = p.getString(PREFS_REGEX + i, null);
            if (!TextUtils.isEmpty(regex)) {
                try {
                    Log.d("search for '", regex+ "' in "+ ret);
                    ret = ret.replaceAll(regex, p.getString(PREFS_REPLACE + i, ""));
                    Log.d("new number: ", ret);
                } catch (PatternSyntaxException e) {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }
        return ret;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in Action Bar clicked; go home
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public Context getContext() {
        return this;
    }
}
