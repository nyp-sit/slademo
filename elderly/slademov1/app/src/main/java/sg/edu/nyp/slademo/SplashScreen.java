package sg.edu.nyp.slademo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

/**
 * Created by L30912 on 21/10/2015.
 */
public class SplashScreen extends Activity {

    private static int SPLASH_TIME_OUT = 1000;
    public static String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        TelephonyManager telephonyManager =
                (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        uid = telephonyManager.getDeviceId();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String imei = sp.getString("imei", "");

        if (imei.equals(uid)) {
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent i = new Intent(SplashScreen.this, ViewPagerActivity.class);
                    startActivity(i);

                    finish();
                }
            }, SPLASH_TIME_OUT);
        } else {
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent i = new Intent(SplashScreen.this, ViewPagerActivity.class);
                    startActivity(i);

                    finish();
                }
            }, SPLASH_TIME_OUT);
        }
    }
}

