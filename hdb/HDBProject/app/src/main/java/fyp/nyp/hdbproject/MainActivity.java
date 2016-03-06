package fyp.nyp.hdbproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ExecutionException;

import fyp.nyp.hdbproject.aws.Beacons;
import fyp.nyp.hdbproject.aws.ContentExhibit;
import fyp.nyp.hdbproject.aws.GetAllContentTask;
import fyp.nyp.hdbproject.aws.GetBeaconsTask;
import fyp.nyp.hdbproject.fragments.StartFragment;
import fyp.nyp.hdbproject.services.BeaconService;
import fyp.nyp.hdbproject.testpurpose.QuestionsTask;

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.this.getClass().getSimpleName();
    public static LinearLayout topFrame;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(this, BeaconService.class));
        StartFragment fragment = new StartFragment();

        Bundle extras = getIntent().getExtras();
        if(extras == null){
            Bundle bundle = new Bundle();
            bundle.putBoolean("download", true);
            fragment.setArguments(bundle);
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();

        try {
            ArrayList<Beacons> beaconsArrayList = new GetBeaconsTask().execute().get();
            ArrayList<ContentExhibit> contentExhibits = new GetAllContentTask().execute().get();

            //for some reason, dynamodb does not sort items by primary key properly? May work with sorting if the entries are recreated.
            Collections.sort(beaconsArrayList, new Comparator<Beacons>() {
                @Override
                public int compare(Beacons lhs, Beacons rhs) {
                    return new Integer(Integer.parseInt(lhs.getName().replace("Exhibit ", ""))).compareTo(new Integer(Integer.parseInt(rhs.getName().replace("Exhibit ", ""))));
                }
            });

            new QuestionsTask().execute(beaconsArrayList.get(0), beaconsArrayList.get(1), beaconsArrayList.get(2), beaconsArrayList.get(3), beaconsArrayList.get(4), beaconsArrayList.get(5)).get();
            LoadedInfo.beaconsArrayList = beaconsArrayList;
            LoadedInfo.contentExhibitsList = contentExhibits;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        /*
        //Stimulate download video from s3, copy from assets into private app storage for now
        FileOutputStream outputStream = null;
        FileOutputStream outputStream1 = null;
        InputStream inputStream = null;
        InputStream inputStream1 = null;
        try {
            outputStream = this.openFileOutput("test_file.mp4", Context.MODE_PRIVATE);
            outputStream1 = this.openFileOutput("test_file.mp3", Context.MODE_PRIVATE);

            AssetManager assetManager = this.getAssets();
            inputStream = assetManager.open("video.mp4");
            inputStream1 = assetManager.open("audio.mp3");

            int read = 0;
            byte[] bytes = new byte[1024];

            while((read = inputStream.read(bytes)) != -1){
                outputStream.write(bytes, 0, read);
            }
            read = 0;
            bytes = new byte[1024];

            while((read = inputStream1.read(bytes)) != -1){
                outputStream1.write(bytes, 0, read);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                if (outputStream != null) {
                    outputStream.close();
                    outputStream1.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                    inputStream1.close();
                }
                Toast.makeText(getApplicationContext(), "SAVED!", Toast.LENGTH_LONG).show();
            }catch (IOException ex){
                ex.printStackTrace();
            }
        }
        */
    }


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
}
