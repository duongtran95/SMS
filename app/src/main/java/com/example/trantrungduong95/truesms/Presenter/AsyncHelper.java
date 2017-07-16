package com.example.trantrungduong95.truesms.Presenter;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.trantrungduong95.truesms.CustomAdapter.ConversationsAdapter;
import com.example.trantrungduong95.truesms.MainActivity;
import com.example.trantrungduong95.truesms.Model.Conversation;

import java.util.concurrent.RejectedExecutionException;

public class AsyncHelper extends AsyncTask<Void, Void, Void> {

    //link ConversationsAdapter to invalidate on new data.
    private static ConversationsAdapter adapter = null;

    //link Context.
    private Context context;

    //link Conversation.
    private Conversation conv;

    //Changed anything?
    private boolean changed = false;

    //Fill link Conversation.

    private AsyncHelper(Context c, Conversation con) {
        context = c;
        conv = con;
    }

     //Fill Conversations data. If needed: spawn threads.
     //sync fetch of information
    public static void fillConversation(Context context, Conversation conv, boolean sync) {
        //fillConversation
        if (context == null || conv == null || conv.getThreadId() < 0) {
            return;
        }
        AsyncHelper helper = new AsyncHelper(context, conv);
        if (sync) {
            helper.doInBackground((Void) null);
        } else {
            try {
                helper.execute((Void) null);
            } catch (RejectedExecutionException e) {
                Log.e("rejected execution", e.getMessage());
            }
        }
    }

    @Override
    protected Void doInBackground(Void... arg0) {
        if (conv == null) {
            return null;
        }
        Log.d("doInBackground()","");
        try {
            changed = conv.getContact().update(context, true, MainActivity.showContactPhoto);
        } catch (NullPointerException e) {
            Log.e("error updating contact", e.getMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if (changed && adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    //Set link ConversationsAdapter to invalidate data after refreshing.
    public static void setAdapter(ConversationsAdapter a) {
        adapter = a;
    }
}
