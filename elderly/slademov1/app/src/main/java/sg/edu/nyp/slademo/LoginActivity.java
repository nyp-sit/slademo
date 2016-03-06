package sg.edu.nyp.slademo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by L30912 on 21/10/2015.
 */
public class LoginActivity extends Activity {

    EditText etName;
    EditText etPhone;
    TextView tvIMEI;
    Button btnSave;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        etName = (EditText) findViewById(R.id.etName);
        etPhone = (EditText) findViewById(R.id.etPhone);
        tvIMEI = (TextView) findViewById(R.id.tvIMEI);
        btnSave = (Button) findViewById(R.id.btnSave);
        TelephonyManager telephonyManager =
                (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        uid = telephonyManager.getDeviceId();
        tvIMEI.setText(uid);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveName("name", etName.getText().toString());
                savePhone("phone", etPhone.getText().toString());
                saveIMEI("imei", uid);

                Intent i = new Intent(LoginActivity.this, MapsActivity.class);
                startActivity(i);

                finish();

            }
        });
    }

    private void saveName(String key, String value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString(key, value);
        edit.commit();
    }

    private void savePhone(String key, String value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString(key, value);
        edit.commit();
    }

    private void saveIMEI(String key, String value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString(key, value);
        edit.commit();
    }


}
