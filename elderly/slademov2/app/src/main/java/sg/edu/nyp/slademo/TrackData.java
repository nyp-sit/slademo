package sg.edu.nyp.slademo;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

@DynamoDBTable(tableName = "TrackData")
public class TrackData {

    private String id;
    private float accuracy;
    private double altitude;
    private float bearing;
    private float distance;
    @DynamoDBAttribute(attributeName = "accuracy")
    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }
    @DynamoDBAttribute(attributeName = "altitude")
    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }
    @DynamoDBAttribute(attributeName = "bearing")
    public float getBearing() {
        return bearing;
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
    }

    @DynamoDBAttribute(attributeName = "distance")
    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }
    @DynamoDBAttribute(attributeName = "speed")
    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }
    @DynamoDBAttribute(attributeName = "locationId")
    public int getLocationId() {
        return locationId;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    @DynamoDBAttribute(attributeName = "wardId")
    public int getWardId() {
        return wardId;
    }

    public void setWardId(int wardId) {
        this.wardId = wardId;
    }

    @DynamoDBAttribute(attributeName = "vehicleId")
    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    private float speed;
    private int locationId;
    private int wardId;
    private int vehicleId;
    private long timeStamp;
    private double lat;
    private double lng;
    private float step;
    private String dateTime;

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

    @DynamoDBAttribute(attributeName = "step")
    public float getStep(){ return step;}

    public void setStep(float step) {this.step = step; }

    @DynamoDBRangeKey(attributeName = "timeStamp")
    public long getTimeStamp() {
        return timeStamp;
    }
    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    @DynamoDBAttribute(attributeName = "datetime")
    public String getDateTime(){ return dateTime;}

    public void setDateTime(String dateTime) {this.dateTime = dateTime; }

}
