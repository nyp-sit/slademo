package fypj.com.weicong.phoneapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    GoogleApiClient client;
    LocalBroadcastManager lm;

    BroadcastReceiver connect = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            tvText.setText("Connected to peer");
        }
    };

    BroadcastReceiver disconnect = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            tvText.setText("Disconnected");
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        client.connect();
        lm.registerReceiver(connect, new IntentFilter("fypj.com.weicong.phoneapp.PEER_CONNECTED"));
        lm.registerReceiver(disconnect, new IntentFilter("fypj.com.weicong.phoneapp.PEER_DISCONNECTED"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        client = new GoogleApiClient.Builder(getBaseContext())
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        lm = LocalBroadcastManager.getInstance(getApplicationContext());

        tvText =(TextView) findViewById(R.id.tvText);

    }

    TextView tvText;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        client.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        lm.unregisterReceiver(connect);
        lm.unregisterReceiver(disconnect);
    }

    @Override
    public void onConnected(Bundle bundle) {

        Wearable.NodeApi.getConnectedNodes(client).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                if (getConnectedNodesResult != null && getConnectedNodesResult.getNodes().size() > 0) {
                    startService(new Intent(MainActivity.this, WatchConnectionService.class));

                    tvText.setText("Connected to peer");
                }
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {
        stopService(new Intent(MainActivity.this, WatchConnectionService.class));
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
