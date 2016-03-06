package fyp.nyp.hdbproject.aws;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

/**
 * Created by chitboon on 10/6/15.
 */
public class DynamoDBHelper {

    public static DynamoDBMapper mapper;

    public static DynamoDBMapper get() {
        if (mapper == null) {
            AWSCredentials credentialsProvider = new BasicAWSCredentials("AKIAJDRIFQM4NR63YW7Q","26ryCK+bwLeasC/LnvjF+DZKIVni5t/0K9i7TIR4");
            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
            ddbClient.setRegion(com.amazonaws.regions.Region.getRegion(Regions.AP_SOUTHEAST_1));
            mapper = new DynamoDBMapper(ddbClient);
        }
        return mapper;
    }

    public static void save(Object data) {
        get();
        mapper.save(data);
    }

}
