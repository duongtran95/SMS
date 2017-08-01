package com.example.trantrungduong95.truesms.Presenter;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.trantrungduong95.truesms.Model.ContactObserver;
import com.example.trantrungduong95.truesms.R;

public class ContactObserverService extends Service implements ContactObserver.OnUpdate {
    private ContactObserver contactObserver ;

    public ContactObserverService() {
    }

    @Override
    public void onCreate(){
        super.onCreate();
        contactObserver = new ContactObserver(this);
        Toast.makeText(this,"Service Started",Toast.LENGTH_SHORT).show();
        this.getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI,false,contactObserver);
    }

    @Override
    public void onDestroy() {
        if( contactObserver !=null  )
        {
            this.getContentResolver().unregisterContentObserver(contactObserver);
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()
        return START_STICKY;
    }

    @Override
    public void onUpdate(Uri uri) {
        Log.e("aaaaa",uri+"");
        NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setContentTitle("Contact Update Notifier")
                .setContentText(uri.getQuery())
                .setSmallIcon(R.drawable.icon);
        NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        manager.notify(1,builder.build());
    }
}
