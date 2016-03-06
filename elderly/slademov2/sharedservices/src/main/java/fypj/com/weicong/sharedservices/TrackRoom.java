package fypj.com.weicong.sharedservices;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

/**
 * Created by L30912 on 12/10/2015.
 */


@DynamoDBTable(tableName = "TrackRoom")
public class TrackRoom {

    private String id;
    private String enterRoom;
    private String exitRoom;
    private long timeStamp;
    private String dateTime;

    @DynamoDBHashKey(attributeName = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @DynamoDBAttribute(attributeName = "enterRoom")
    public String getEnterRoom() {
        return enterRoom;
    }

    public void setEnterRoom(String enterRoom) {
        this.enterRoom = enterRoom;
    }

    @DynamoDBAttribute(attributeName = "exitRoom")
    public String getExitRoom() {
        return exitRoom;
    }

    public void setExitRoom(String exitRoom) {
        this.exitRoom = exitRoom;
    }

    @DynamoDBAttribute(attributeName = "dateTime")
    public String getDateTime(){
        return dateTime;
    }

    public void setDateTime(String dateTime){
        this.dateTime = dateTime;
    }

    @DynamoDBRangeKey(attributeName = "timeStamp")
    public long getTimeStamp() {
        return timeStamp;
    }
    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
