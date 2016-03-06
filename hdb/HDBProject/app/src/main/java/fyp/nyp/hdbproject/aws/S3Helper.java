package fyp.nyp.hdbproject.aws;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;

/**
 * Created by chitboon on 10/6/15.
 */
public class S3Helper {

    public static AmazonS3Client s3Client;

    public static AmazonS3Client get() {
        if (s3Client == null) {
            AWSCredentials credentialsProvider = new BasicAWSCredentials("AKIAJDRIFQM4NR63YW7Q","26ryCK+bwLeasC/LnvjF+DZKIVni5t/0K9i7TIR4");
            AmazonS3Client ddbClient = new AmazonS3Client(credentialsProvider);
            ddbClient.setRegion(com.amazonaws.regions.Region.getRegion(Regions.AP_SOUTHEAST_1));
            s3Client = ddbClient;
        }
        return s3Client;
    }

}
