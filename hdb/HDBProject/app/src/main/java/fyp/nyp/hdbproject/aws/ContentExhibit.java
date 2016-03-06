package fyp.nyp.hdbproject.aws;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.List;

/**
 * Created by L30911 on 1/26/2016.
 */
@DynamoDBTable(tableName = "hdb_content")
public class ContentExhibit{

    private String beaconId;


    private String content;
    private String questions;
    private List<String> options;
    private int correctAnswerIndex;

    //File information
    private int fileType;
    private String fileName;

    @DynamoDBHashKey(attributeName = "beaconId")
    public String getBeaconId() {
        return beaconId;
    }

    public void setBeaconId(String beaconId) {
        this.beaconId = beaconId;
    }

    @DynamoDBAttribute(attributeName = "content")
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @DynamoDBAttribute(attributeName = "questions")
    public String getQuestions() {
        return questions;
    }

    public void setQuestions(String questions) {
        this.questions = questions;
    }

    @DynamoDBAttribute(attributeName = "options")
    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    @DynamoDBAttribute(attributeName = "answerIndex")
    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }

    public void setCorrectAnswerIndex(int correctAnswerIndex) {
        this.correctAnswerIndex = correctAnswerIndex;
    }

    @DynamoDBAttribute(attributeName = "fileType")
    public int getFileType() {
        return fileType;
    }

    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    @DynamoDBAttribute(attributeName = "fileName")
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
