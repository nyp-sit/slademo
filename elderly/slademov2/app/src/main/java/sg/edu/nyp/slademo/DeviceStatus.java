package sg.edu.nyp.slademo;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

/**
 * Created by L30911 on 2/2/2016.
 */
@DynamoDBTable(tableName = "Devices")
public class DeviceStatus {
    private String id;

    @DynamoDBHashKey(attributeName = "imei_id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
