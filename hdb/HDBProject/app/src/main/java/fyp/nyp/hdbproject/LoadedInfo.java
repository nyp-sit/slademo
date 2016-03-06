package fyp.nyp.hdbproject;

import java.util.ArrayList;

import fyp.nyp.hdbproject.aws.Beacons;
import fyp.nyp.hdbproject.aws.ContentExhibit;

/**
 * Created by L30911 on 1/26/2016.
 */
public class LoadedInfo {
    public static ArrayList<ContentExhibit> contentExhibitsList;
    public static ArrayList<Beacons> beaconsArrayList;
    public static ArrayList<ContentExhibit> answeredExhibits = new ArrayList<>();
    public static boolean isContentExhibitAnswered(ContentExhibit contentExhibit){
        return answeredExhibits.contains(contentExhibit);
    }
    public static void setContentExhibitAnswered(ContentExhibit contentExhibitAnswered){
        answeredExhibits.add(contentExhibitAnswered);
    }
    public static Beacons getBeaconById(ArrayList<Beacons> list, String id, String id2, String id3 ){

            id = id.replace("-", "");
            id2 = id2.replace("-", "");
            if(id3 != null) {
                id3 = id3.replace("-", "");
            }
            Beacons beacon = null;
            for (Beacons beacons : list) {
                if (beacons.getBeaconId1().equals(id) && beacons.getBeaconId2().equals(id2) && (id3 == null || beacons.getBeaconId3() == null || beacons.getBeaconId3().equals(id3))) {
                    beacon = beacons;
                    break;
                }
            }
            return beacon;
    }
    public static ContentExhibit getContentExihibtByBeaconId(String id){
        ContentExhibit contentExhibit = null;
        for(ContentExhibit contentExhibits : contentExhibitsList){
            if(contentExhibits.getBeaconId().equals(id)){
                contentExhibit = contentExhibits;
                break;
            }
        }
        return contentExhibit;
    }
    public enum FILE_TYPE{
        AUDIO, VIDEO;
    };


}
