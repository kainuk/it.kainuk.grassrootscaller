package it.kainuk.grassrootscaller;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import me.pushy.sdk.Pushy;

class RegisterForPushNotificationsAsync extends AsyncTask<String, Void, Exception> {
    ProgressDialog mLoading;

    MainActivity activity;

    private SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(activity);
    }

    public RegisterForPushNotificationsAsync(MainActivity activity,Context context) {
        this.activity = activity;
    }

    private void saveDeviceToken(String deviceToken) {
        // Save token locally in app SharedPreferences
        getSharedPreferences().edit().putString("deviceToken", deviceToken).apply();

        // Your app should store the device token in your backend database
        //new URL("https://{YOUR_API_HOSTNAME}/register/device?token=" + deviceToken).openConnection();
    }

    public String getDeviceToken() {
        // Get token stored in SharedPreferences
        return getSharedPreferences().getString("deviceToken",null);
    }

    @Override
    protected Exception doInBackground(String... params) {
        try {
            // Assign a unique token to this device
            String deviceToken = Pushy.register(activity);
            // Save token locally / remotely
            saveDeviceToken(deviceToken);
        }
        catch (Exception exc) {
            // Return exc to onPostExecute
            return exc;
        }

        // Success
        return null;
    }

    @Override protected void onPreExecute(){
        // Create progress dialog and set it up
        mLoading = new ProgressDialog(activity);
        mLoading.setMessage(activity.getString(R.string.registeringDevice));
        mLoading.setCancelable(false);
        mLoading.show();
    }

    @Override
    protected void onPostExecute(Exception exc) {
        mLoading.dismiss();
        // Registration failed?
        if (exc != null) {
            // Write error to logcat
            Log.e("Pushy", "Registration failed: " + exc.getMessage());

            // Display error dialog
            new AlertDialog.Builder(activity).setTitle(R.string.registrationError)
                    .setMessage(exc.getMessage())
                    .setPositiveButton(R.string.ok, null)
                    .create()
                    .show();
        }
        activity.updateUI();
    }
}
