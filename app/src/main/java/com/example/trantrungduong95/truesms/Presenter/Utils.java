package com.example.trantrungduong95.truesms.Presenter;

/**
 * Created by ngomi_000 on 6/15/2017.
 */

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class Utils {

    /**
     * Tag for output.
     */
    private static String TAG = "Utils";

    /**
     * 10.
     */
    public int N_10 = 10;

    /**
     * 100.
     */
    public int N_100 = 100;

    /**
     * 1000.
     */
    public int N_1000 = 1000;

    /**
     * One hour in minutes.
     */
    public int HOUR_IN_MINUTES = 60;

    /**
     * One hour in seconds.
     */
    public int HOUR_IN_SECONDS = 60 * 60;

    /**
     * One hour in milliseconds.
     */
    public int HOUR_IN_MILLIS = 60 * 60 * 1000;

    /**
     * One minutes in seconds.
     */
    public int MINUTES_IN_SECONDS = 60;

    /**
     * One minutes in milliseconds.
     */
    public int MINUTES_IN_MILLIS = 60 * 1000;

    /**
     * One day in seconds.
     */
    public long DAY_IN_SECONDS = 60L * 60L * 24L;

    /**
     * One day in milliseconds.
     */
    public long DAY_IN_MILLIS = 60L * 60L * 24L * 1000L;

    /**
     * k aka 1024.
     */
    public int K = 1024;

    /**
     * M aka 1024 * 1024.
     */
    public int M = K * K;

    /**
     * Default Constructor.
     */
    private Utils() {

    }

    /**
     * Parse {@link Boolean}.
     *
     * @param value    value a {@link String}
     * @param defValue default value
     * @return parsed {@link Boolean}
     */
    public boolean parseBoolean(String value, boolean defValue) {
        boolean ret = defValue;
        if (value == null || value.length() == 0) {
            return ret;
        }
        try {
            ret = Boolean.parseBoolean(value);
        } catch (NumberFormatException e) {
            Log.w(TAG, "parseBoolean(" + value + ") failed: " + e.toString());
        }
        return ret;
    }

    /**
     * Parse {@link Integer}.
     *
     * @param value    value a {@link String}
     * @param defValue default value
     * @return parsed {@link Integer}
     */
    public static int parseInt(String value, int defValue) {
        int ret = defValue;
        if (value == null || value.length() == 0) {
            return ret;
        }
        try {
            ret = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            Log.w(TAG, "parseInt(" + value + ") failed: " + e.toString());
        }
        return ret;
    }

    /**
     * Parse {@link Long}.
     *
     * @param value    value a {@link String}
     * @param defValue default value
     * @return parsed {@link Long}
     */
    public long parseLong(String value, long defValue) {
        long ret = defValue;
        if (value == null || value.length() == 0) {
            return ret;
        }
        try {
            ret = Long.parseLong(value);
        } catch (NumberFormatException e) {
            Log.w(TAG, "parseLong(" + value + ") failed: " + e.toString());
        }
        return ret;
    }

    /**
     * Parse {@link Float}.
     *
     * @param value    value a {@link String}
     * @param defValue default value
     * @return parsed {@link Float}
     */
    public float parseFloat(String value, float defValue) {
        float ret = defValue;
        if (value == null || value.length() == 0) {
            return ret;
        }
        try {
            ret = Float.parseFloat(value);
        } catch (NumberFormatException e) {
            Log.w(TAG, "parseFloat(" + value + ") failed: " + e.toString());
        }
        return ret;
    }

    /**
     * Calculate MD5 Hash from String.
     *
     * @param s input
     * @return hash
     */
    public String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte[] messageDigest = digest.digest();
            // Create Hex String
            StringBuilder hexString = new StringBuilder(32);
            int b;
            for (byte aMessageDigest : messageDigest) {
                b = 0xFF & aMessageDigest;
                if (b < 0x10) {
                    hexString.append('0').append(Integer.toHexString(b));
                } else {
                    hexString.append(Integer.toHexString(b));
                }
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, null, e);
        }
        return "";
    }

    /**
     * Set locale read from preferences to context.
     *
     * @param context {@link Context}
     */
    public static void setLocale(Context context) {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
        String lc = p.getString("morelocale", null);
        if (TextUtils.isEmpty(lc)) {
            return;
        }
        Log.i(TAG, "set custom locale: " + lc);
        Locale locale = new Locale(lc);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        context.getResources().updateConfiguration(config, null);
    }

    /**
     * Start MoreLocale2 or fetch it from market if unavailable.
     *
     * @param context {@link Context}
     */
    public void startMoreLocale(Context context) {
        try {
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.setComponent(new ComponentName("jp.co.c_lis.ccl.morelocale",
                    "com.android.settings.morelocale.ui.MainActivity"));
            context.startActivity(i);
        } catch (ActivityNotFoundException e) {
            try {
                Log.e(TAG, "no morelocale2", e);
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri
                        .parse("market://details?id=" + "jp.co.c_lis.ccl.morelocale")));
            } catch (ActivityNotFoundException e1) {
                Log.e(TAG, "no market", e1);
                Toast.makeText(context, "no market", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Copy a file from source to destination.
     *
     * @param source      source
     * @param destination destination
     * @throws IOException File not found or any other IO Exception.
     */
    public void copyFile(String source, String destination) throws IOException {
        InputStream in = new FileInputStream(source);
        OutputStream out = new FileOutputStream(destination);
        byte[] buf = new byte[K];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    /**
     * Concatenate byte arrays.
     *
     * @param bytes array of byte arrays
     * @return byte array
     */
    public byte[] concatByteArrays(byte[][] bytes) {
        int rl = 0;
        for (byte[] aByte : bytes) {
            rl += aByte.length;
        }
        byte[] ret = new byte[rl];
        int pos = 0;
        for (byte[] b : bytes) {
            int bl = b.length;
            System.arraycopy(b, 0, ret, pos, bl);
            pos += bl;
        }
        return ret;
    }

    /**
     * Get the prefix from a telephone number (best approximation).
     *
     * @param number number
     * @return prefix
     */
    public String getPrefixFromTelephoneNumber(String number) {
        String prefix = null;

        if (number.startsWith("+10") || number.startsWith("+11")) {
            prefix = "+1";
        } else if (number.startsWith("+20") || number.startsWith("+27")) {
            prefix = number.substring(0, 3);
        } else if (number.startsWith("+2") || number.startsWith("+35") || number.startsWith("+37")
                || number.startsWith("+38") || number.startsWith("+42") || number.startsWith("+50")
                || number.startsWith("+59") || number.startsWith("+67") || number.startsWith("+68")
                || number.startsWith("+69") || number.startsWith("+85") || number.startsWith("+88")
                || number.startsWith("+96") || number.startsWith("+97") || number
                .startsWith("+99")) {
            prefix = number.substring(0, 4);
        } else if (number.startsWith("+3") || number.startsWith("+4") || number.startsWith("+5")
                || number.startsWith("+6") || number.startsWith("+8") || number.startsWith("+9")) {
            prefix = number.substring(0, 3);
        } else if (number.startsWith("+7")) {
            prefix = number.substring(0, 2);
        } else if (number.startsWith("+1")) {
            prefix = number.substring(0, 6);
        }

        return prefix;
    }

    public static String removeAccent(String s) {

        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("");
    }
}
