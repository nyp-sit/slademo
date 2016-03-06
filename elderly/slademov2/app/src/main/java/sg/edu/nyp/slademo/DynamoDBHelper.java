package sg.edu.nyp.slademo;

import android.content.Context;
import android.util.Log;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by chitboon on 10/6/15.
 */
public class DynamoDBHelper {

    private static final String LOG_TAG = DynamoDBHelper.class.getSimpleName();
    private static DynamoDBMapper mapper;

    public static DynamoDBMapper get() {
        if (mapper == null) {
            AWSCredentials credentialsProvider = new BasicAWSCredentials("AKIAJDRIFQM4NR63YW7Q","26ryCK+bwLeasC/LnvjF+DZKIVni5t/0K9i7TIR4");
            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
            ddbClient.setRegion(com.amazonaws.regions.Region.getRegion(Regions.AP_SOUTHEAST_1));
            mapper = new DynamoDBMapper(ddbClient);
        }
        return mapper;
    }

    public static void save(Object data) {
        if(hasActiveInternetConnection() && mapper != null){
            get();
            mapper.save(data);
        }
    }
    public static boolean hasActiveInternetConnection() {
        //if (isNetworkAvailable(context)) {
            try {
                HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
                urlc.setRequestProperty("User-Agent", "Test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500);
                urlc.connect();
                return (urlc.getResponseCode() == 200);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error checking internet connection", e);
            }
        //} else {
          //  Log.d(LOG_TAG, "No network available!");
        //}
        return false;
    }

}
