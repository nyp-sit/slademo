package fyp.nyp.hdbproject.aws;

/**
 * Created by L30911 on 1/14/2016.
 */

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAutoGeneratedKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

/**
 * Created by L30912 on 12/10/2015.
 */


@DynamoDBTable(tableName = "hdb_beacons")
public class Beacons {
    private String id;
    private String name;
    private String beaconId1;//Major  for iBeacon
    private String beaconId2;//Minor for iBeacon
    private String beaconId3;

    private double x;
    private double y;

    private String contentId;

    @DynamoDBAttribute(attributeName = "beacon_name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @DynamoDBAttribute(attributeName = "contentQuestionId")
    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    @DynamoDBAttribute(attributeName = "x")
    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    @DynamoDBAttribute(attributeName = "y")
    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @DynamoDBAttribute(attributeName = "angleStart")
    public int getAngleStart() {
        return angleStart;
    }

    public void setAngleStart(int angleStart) {
        this.angleStart = angleStart;
    }

    @DynamoDBAttribute(attributeName = "angleEnd")
    public int getAngleEnd() {
        return angleEnd;
    }

    public void setAngleEnd(int angleEnd) {
        this.angleEnd = angleEnd;
    }

    private int angleStart;
    private int angleEnd;

    @DynamoDBAttribute(attributeName = "beaconId3")
    public String getBeaconId3() {
        return beaconId3;
    }

    public void setBeaconId3(String beaconId3) {
        this.beaconId3 = beaconId3;
    }

    @DynamoDBHashKey(attributeName = "id")
    @DynamoDBAutoGeneratedKey
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    @DynamoDBAttribute(attributeName = "beaconId1")
    public String getBeaconId1() {
        return beaconId1;
    }
    public void setBeaconId1(String beaconId1) {
        this.beaconId1 = beaconId1;
    }

    @DynamoDBAttribute(attributeName = "beaconId2")
    public String getBeaconId2() {
        return beaconId2;
    }

    public void setBeaconId2(String beaconId2) {
        this.beaconId2 = beaconId2;
    }


}
