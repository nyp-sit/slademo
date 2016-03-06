package sg.edu.nyp.slademo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import java.util.concurrent.ExecutionException;

import fypj.com.weicong.sharedservices.db.RegisterDeviceTask;

/**
 * Created by L30912 on 21/10/2015.
 */
public class SplashScreen extends Activity {

    private static int SPLASH_TIME_OUT = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        TelephonyManager telephonyManager =
                (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        StaticData.uid = telephonyManager.getDeviceId();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String imei = sp.getString("imei", "");

        try {
            new RegisterDeviceTask().execute(StaticData.uid).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if (imei.equals(StaticData.uid)) {
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

