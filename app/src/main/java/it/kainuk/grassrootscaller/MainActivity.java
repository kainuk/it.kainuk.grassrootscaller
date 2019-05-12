package it.kainuk.grassrootscaller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import me.pushy.sdk.Pushy;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 1;
    private static final int CALL_PHONE_PERMISSION_CODE = 2;

    private TextView mDeviceToken;
    private TextView mMessageView;

    private boolean smsPermitted = false;
    private boolean callPermitted = false;


    private BroadcastReceiver receiver ;
    private RegisterForPushNotificationsAsync registerForPushNotificationsAsync ;

    private void redrawUi(){
        findViewById(R.id.btPermitCall).setVisibility(callPermitted?View.GONE:View.VISIBLE);
        findViewById(R.id.btPermitSendSMS).setVisibility(smsPermitted?View.GONE:View.VISIBLE);
    }

    public  void askCallPermission(View view){
        requestPermissions(new String[]{Manifest.permission.CALL_PHONE},CALL_PHONE_PERMISSION_CODE);
    }

    public  void askSmsPermission(View view){
        requestPermissions(new String[]{Manifest.permission.SEND_SMS},SMS_PERMISSION_CODE);
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                              @NonNull int[] grantResults){
        if(requestCode==SMS_PERMISSION_CODE && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            this.smsPermitted=true;
        }
        if(requestCode==CALL_PHONE_PERMISSION_CODE && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            this.callPermitted=true;
        }
        this.redrawUi();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();
        redrawUi();
        receiver = new PushReceiver (this);
        registerForPushNotificationsAsync = new RegisterForPushNotificationsAsync(this,MainActivity.this);
        mDeviceToken = findViewById(R.id.deviceToken);
        mMessageView = findViewById(R.id.messageView);
        if (registerForPushNotificationsAsync.getDeviceToken() == null) {
            registerForPushNotificationsAsync.execute();
        }
        else {
            Pushy.listen(this);
            updateUI();
        }
    }

    /** Called when the user taps the Send button */
    public void callMessage(String phone) {
        if(this.callPermitted) {
            try {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + phone));
                startActivity(callIntent);
            } catch (SecurityException ex) {
                Log.e("GRC", ex.toString());
            }
            mMessageView.setText("Called "+phone);
        } else {
            mMessageView.setText("Not permitted Call "+phone);
        }
    }

    /** Called when the user taps the Send button */
    public void disconnectMessage(String phone) {
        if(this.callPermitted) {
            try {
                Intent callIntent = new Intent(Intent.CALL);
                callIntent.setData(Uri.parse("tel:" + phone));
                startActivity(callIntent);
            } catch (SecurityException ex) {
                Log.e("GRC", ex.toString());
            }
            mMessageView.setText("Called "+phone);
        } else {
            mMessageView.setText("Not permitted Call "+phone);
        }
    }

    /** Called when the user taps the Send button */
    public void sendSMS(String phone,String message) {
        if(this.smsPermitted) {
            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phone, null, message, null, null);
            } catch (Exception ex) {
                Log.e("GRC", ex.toString());
            }
            mMessageView.setText("SMS for "+phone+"\n"+message);
        } else {
            mMessageView.setText("Not Permitted SMS for "+phone+"\n"+message);
        }
    }

    public void updateUI() {
        // Get device token from SharedPreferences
        String deviceToken = registerForPushNotificationsAsync.getDeviceToken();
        // Registration failed?
        if (deviceToken == null) {
            mDeviceToken.setText(R.string.registrationFailed);
            // Stop execution
            return;
        }
        mDeviceToken.setText(deviceToken);
        // Write device token to logcat
        Log.d("Pushy", "Device token: " + deviceToken);
    }

    private void checkPermissions(){
        smsPermitted =  (checkSelfPermission(Manifest.permission.SEND_SMS)==PackageManager.PERMISSION_GRANTED);
        callPermitted = (checkSelfPermission(Manifest.permission.CALL_PHONE)==PackageManager.PERMISSION_GRANTED);
    }

    public void onResume() {
        super.onResume();
        checkPermissions();
        IntentFilter filter = new IntentFilter();
        filter.addAction("pushy.me");
        this.registerReceiver(this.receiver, filter);
    }

    public void onPause() {
        super.onPause();
        this.unregisterReceiver(this.receiver);
    }
}
