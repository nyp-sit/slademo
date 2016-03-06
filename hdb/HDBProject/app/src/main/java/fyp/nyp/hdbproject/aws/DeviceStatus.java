package fyp.nyp.hdbproject.aws;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

/**
 * Created by L30911 on 2/1/2016.
 */
@DynamoDBTable(tableName = "hdb_device_status")
public class DeviceStatus {
    private String id;
    private String status;
    private long time;

    @DynamoDBHashKey(attributeName = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @DynamoDBAttribute(attributeName = "status")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @DynamoDBAttribute(attributeName = "time")
    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
