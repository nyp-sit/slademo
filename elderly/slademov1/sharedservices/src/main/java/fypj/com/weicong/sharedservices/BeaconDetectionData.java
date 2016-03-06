package fypj.com.weicong.sharedservices;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

/**
 * Created by L30911 on 12/10/2015.
 */
@DynamoDBTable(tableName = "BeaconDetection")
public class BeaconDetectionData {
    private String id;//device id
    private double lat;
    private double lng;
    private double distance;
    private String Id1;
    private String Id2;
    private String Id3;
    private long timestamp;

    @DynamoDBHashKey(attributeName = "timestamp")
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @DynamoDBHashKey(attributeName = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    @DynamoDBAttribute(attributeName = "lat")
    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }
    @DynamoDBAttribute(attributeName = "lng")
    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
    @DynamoDBAttribute(attributeName = "distance")
    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
    @DynamoDBAttribute(attributeName = "Id1")
    public String getId1() {
        return Id1;
    }

    public void setId1(String id1) {
        Id1 = id1;
    }
    @DynamoDBAttribute(attributeName = "Id2")
    public String getId2() {
        return Id2;
    }

    public void setId2(String id2) {
        Id2 = id2;
    }
    @DynamoDBAttribute(attributeName = "Id3")
    public String getId3() {
        return Id3;
    }

    public void setId3(String id3) {
        Id3 = id3;
    }
}
