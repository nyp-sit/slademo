package fyp.nyp.hdbproject.testpurpose;

import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

import fyp.nyp.hdbproject.LoadedInfo;
import fyp.nyp.hdbproject.aws.Beacons;
import fyp.nyp.hdbproject.aws.ContentExhibit;
import fyp.nyp.hdbproject.aws.DynamoDBHelper;

/**
 * Created by L30911 on 1/26/2016.
 */
public class QuestionsTask extends AsyncTask<Beacons, Void, ArrayList<Beacons>> {
    private final String TAG = this.getClass().getSimpleName();

    @Override
    protected ArrayList<Beacons> doInBackground(Beacons... params) {
        Log.d(TAG, "Beacon : " + params[0].getBeaconId1());

        //for(Beacons beacon : params){
        ContentExhibit exhibit = new ContentExhibit();
        exhibit.setBeaconId(params[0].getId());

        exhibit.setContent("The HDB estimated that from 1960 to 1969, an average of 147,000 housing units—80,000 from the current deficit, 20,000 due to the redevelopment of the Central Area, and 47,000 due to population increase—would need to be constructed, or an average of about 14,000 a year. However, the private sector only had the ability to provide 2,500 per year, and at price levels out of reach of the low-income population.[3] As many as 54,430 housing units were built between 1960 and 1965 by the HDB. Due to land constraints, high-rise and high-density flats were chosen.");
        exhibit.setCorrectAnswerIndex(2);
        exhibit.setOptions(Arrays.asList(new String[]{"Legal Constraints", "Policy Constraints", "Land Constraints", "Budget Constraints"}));
        exhibit.setQuestions("Why was high density flats chosen?");
        exhibit.setFileName("test_audio.mp3");
        exhibit.setFileType(LoadedInfo.FILE_TYPE.AUDIO.ordinal());

        DynamoDBHelper.save(exhibit);
        exhibit.setBeaconId(params[1].getId());
        DynamoDBHelper.save(exhibit);

        ContentExhibit exhibit3 = new ContentExhibit();
        exhibit3.setBeaconId(params[2].getId());

        exhibit3.setContent("Shortly after achieving self-governance in 1959, Singapore faced a serious problem of housing shortages; low construction rates and massive damage from World War II further exacerbated the prewar housing shortage. In 1947, the British Housing Committee Report noted Singapore had \\\"one of the world’s worst slums -- 'a disgrace to a civilised community'\\\", and the average person-per-building density was 18.2 by 1947. High-rise buildings were also rare. In 1959, the shortage problem remained. An HDB paper estimated that in 1966, 300,000 people lived in squatter settlements in the suburbs and 250,000 lived in squalid shophouses in the Central Area.[3] In its election campaign in 1959, the People's Action Party (PAP) recognized that housing required urgent attention and pledged to provide low-cost housing for the poor if it was elected. When it won the elections and formed the newly elected government, it took immediate action to solve the housing shortage. The government passed the Housing & Development Act of 1960, which replaced the existing Singapore Improvement Trust with the Housing & Development Board.");
        exhibit3.setCorrectAnswerIndex(2);
        exhibit3.setOptions(Arrays.asList(new String[]{"1950", "1951", "1960", "1957"}));
        exhibit3.setQuestions("In which year was the Housing & Development Act established?");
        exhibit3.setFileName("test_video.mp4");
        exhibit3.setFileType(LoadedInfo.FILE_TYPE.VIDEO.ordinal());


        DynamoDBHelper.save(exhibit3);
        exhibit3.setBeaconId(params[3].getId());
        exhibit3.setBeaconId(params[4].getId());
        exhibit3.setBeaconId(params[5].getId());
        DynamoDBHelper.save(exhibit3);
        //}

        return null;
    }


}
