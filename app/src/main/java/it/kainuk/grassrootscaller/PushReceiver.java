package it.kainuk.grassrootscaller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

public class PushReceiver extends BroadcastReceiver {

    MainActivity activity;

    public PushReceiver(MainActivity activity) {
        this.activity = activity;
    }



    @Override
    public void onReceive(Context context, Intent intent) {
        String action;

        if (intent.getStringExtra("action") != null) {
            action = intent.getStringExtra("action");
        } else {
            action = "No Action";
        }
        if (action.equalsIgnoreCase("call")) {
            String phone = intent.getStringExtra("phone");
            activity.callMessage(phone);
        }
        if (action.equalsIgnoreCase("sms")) {
            String phone = intent.getStringExtra("phone");
            String message = intent.getStringExtra("message");
            activity.sendSMS(phone,message);
        }
    }
}
