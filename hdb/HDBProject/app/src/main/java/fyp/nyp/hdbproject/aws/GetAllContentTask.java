package fyp.nyp.hdbproject.aws;

import android.os.AsyncTask;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;

import java.util.ArrayList;

/**
 * Created by L30911 on 1/26/2016.
 */
public class GetAllContentTask extends AsyncTask<Void, Void, ArrayList<ContentExhibit>> {
    @Override
    protected ArrayList<ContentExhibit> doInBackground(Void... params) {



        DynamoDBScanExpression scanExp = new DynamoDBScanExpression();
        PaginatedScanList<ContentExhibit> list = DynamoDBHelper.get().scan(ContentExhibit.class, scanExp);
        return new ArrayList<>(list);
    }
}
