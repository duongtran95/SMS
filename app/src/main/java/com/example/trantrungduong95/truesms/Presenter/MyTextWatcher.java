package com.example.trantrungduong95.truesms.Presenter;

import android.content.Context;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

public class MyTextWatcher implements TextWatcher {
    //Minimum length for showing sms length.
    private static int TEXT_LABLE_MIN_LEN = 50;

    private Context context;

    private TextView tvTextLabel;

    //Constructor.
    public MyTextWatcher(Context ctx, TextView label) {
        context = ctx;
        tvTextLabel = label;
    }

    public void afterTextChanged(Editable s) {
        int len = s.length();
        if (len == 0) {
            tvTextLabel.setVisibility(View.GONE);
        } else {
            if (len > TEXT_LABLE_MIN_LEN) {
                try {
                    int[] l = SmsMessage.calculateLength(s.toString(), false);
                    tvTextLabel.setText(l[0] + "/" + l[2]);
                    tvTextLabel.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    //error calculating message length
                }
            } else {
                tvTextLabel.setVisibility(View.GONE);
            }
        }
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged( CharSequence s, int start, int before, int count) {
    }
}
