package fyp.nyp.hdbproject.aws;

import android.os.AsyncTask;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;

import java.util.ArrayList;

/**
 * Created by L30911 on 1/15/2016.
 */
public class GetBeaconsTask extends AsyncTask<Void, Void, ArrayList<Beacons>> {
    @Override
    protected ArrayList<Beacons> doInBackground(Void... params) {
        /*
        * Create beacon
        * */
/*
        Beacons beaconInsert = new Beacons();

        beaconInsert.setName("Beacon at Exhibit 3");
        beaconInsert.setBeaconId1("e2c56db5dffb48d2b060d0f5a71096e0");
        beaconInsert.setBeaconId2("0");
        beaconInsert.setBeaconId3("1");

        beaconInsert.setX(123);
        beaconInsert.setY(240);

        beaconInsert.setAngleStart(90);
        beaconInsert.setAngleEnd(180);

        DynamoDBHelper.save(beaconInsert);
*/
        DynamoDBScanExpression scanExp = new DynamoDBScanExpression();
        PaginatedScanList<Beacons> list = DynamoDBHelper.get().scan(Beacons.class, scanExp);
        ArrayList<Beacons> beaconsArrayList = new ArrayList<>();
        for(Beacons beacon : list){
            beaconsArrayList.add(beacon);
        }
        return beaconsArrayList;
    }
}
