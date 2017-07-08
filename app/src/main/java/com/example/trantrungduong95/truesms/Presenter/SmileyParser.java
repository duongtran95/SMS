package com.example.trantrungduong95.truesms.Presenter;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;

import com.example.trantrungduong95.truesms.R;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//A class for annotating a CharSequence with spans to convert textual emoticons to graphical ones.
public class SmileyParser {

    //Singleton stuff.
    private static SmileyParser sInstance;

    // Get the single instance.
    public static SmileyParser getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SmileyParser(context);
        }
        return sInstance;
    }

    private Context mContext;

    //Smiley texts.
    private String[] mSmileyTexts;

    //Smiley pattern.
    private Pattern mPattern;

    //Map pattern to resource.
    private HashMap<String, Integer> mSmileyToRes;

    //Default constructor.
    private SmileyParser(Context context) {
        mContext = context;
        mSmileyTexts = mContext.getResources().getStringArray(R.array.emoticons);
        mSmileyToRes = buildSmileyToRes();
        mPattern = buildPattern();
    }

    // Smiley resources keys.
    private int[] DEFAULT_SMILEY_RES_IDS = {R.mipmap.emo_im_angel, // 0
            R.mipmap.emo_im_cool, // 1
            R.mipmap.emo_im_cool, // 2
            R.mipmap.emo_im_crying, // 3
            R.mipmap.emo_im_crying, // 4
            R.mipmap.emo_im_foot_in_mouth, // 5
            R.mipmap.emo_im_happy, // 6
            R.mipmap.emo_im_happy, // 7
            R.mipmap.emo_im_kissing, // 8
            R.mipmap.emo_im_kissing, // 9
            R.mipmap.emo_im_laughing, // 10
            R.mipmap.emo_im_laughing, // 11
            R.mipmap.emo_im_lips_are_sealed, // 12
            R.mipmap.emo_im_lips_are_sealed, // 13
            R.mipmap.emo_im_lips_are_sealed, // 14
            R.mipmap.emo_im_money_mouth, // 15
            R.mipmap.emo_im_sad, // 16
            R.mipmap.emo_im_sad, // 17
            R.mipmap.emo_im_surprised, // 18
            R.mipmap.emo_im_tongue_sticking_out, // 19
            R.mipmap.emo_im_tongue_sticking_out, // 20
            R.mipmap.emo_im_tongue_sticking_out, // 21
            R.mipmap.emo_im_undecided, // 22
            R.mipmap.emo_im_winking, // 23
            R.mipmap.emo_im_winking, // 24
            R.mipmap.emo_im_wtf, // 25
            R.mipmap.emo_im_yelling, // 26
    };

    /**
     * Builds the {@link HashMap} I use for mapping the string version of a smiley (e.g. ":-)") to
     * a resource ID for the icon version.
     *
     * @return {@link HashMap}
     */
    private HashMap<String, Integer> buildSmileyToRes() {
        if (DEFAULT_SMILEY_RES_IDS.length != mSmileyTexts.length) {
            // Throw an exception if someone updated DEFAULT_SMILEY_RES_IDS
            // and failed to update arrays.xml
            throw new IllegalStateException("Smiley resource ID/text mismatch");
        }

        HashMap<String, Integer> smileyToRes = new HashMap<>(
                mSmileyTexts.length);
        for (int i = 0; i < mSmileyTexts.length; i++) {
            smileyToRes.put(mSmileyTexts[i], DEFAULT_SMILEY_RES_IDS[i]);
        }

        return smileyToRes;
    }

    //Builds the regular expression we use to find smileys in link addSmileySpans.

    private Pattern buildPattern() {
        // Set the StringBuilder capacity with the assumption that the average
        // smiley is 3 characters long.
        StringBuilder patternString = new StringBuilder(mSmileyTexts.length * 3);

        // Build a regex that looks like (:-)|:-(|...), but escaping the smilies
        // properly so they will be interpreted literally by the regex matcher.
        patternString.append('(');
        for (String s : mSmileyTexts) {
            patternString.append(Pattern.quote(s));
            patternString.append('|');
        }
        // Replace the extra '|' with a ')'
        patternString.replace(patternString.length() - 1, patternString.length(), ")");

        return Pattern.compile(patternString.toString());
    }

    /**
     * Adds ImageSpans to a CharSequence that replace textual emoticons such as :-) with a graphical
     * version.
     *
     * @param text A CharSequence possibly containing emoticons
     * @return A CharSequence annotated with ImageSpans covering any recognized emoticons.
     */
    public CharSequence addSmileySpans(CharSequence text) {
        SpannableStringBuilder builder = new SpannableStringBuilder(text);

        Matcher matcher = mPattern.matcher(text);
        while (matcher.find()) {
            int resId = mSmileyToRes.get(matcher.group());
            builder.setSpan(new ImageSpan(mContext, resId), matcher.start(), matcher.end(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return builder;
    }
}
