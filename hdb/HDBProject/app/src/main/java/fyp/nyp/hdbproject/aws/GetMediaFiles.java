package fyp.nyp.hdbproject.aws;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by L30911 on 2/2/2016.
 */
public class GetMediaFiles extends AsyncTask<String[] ,Void, Void>{
    Context context;
    ProgressBar progressBar;
    public GetMediaFiles(Context context, ProgressBar progressBar){
        this.context = context;
        this.progressBar = progressBar;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        progressBar.setProgress(100);
        progressBar.setVisibility(View.GONE);
        Toast.makeText(context, "Downloading Complete", Toast.LENGTH_LONG).show();
    }

    @Override
    protected Void doInBackground(String[]... params) {

        for(int i = 0;i < params[0].length; i++){
            String fileName = params[0][i];
            S3Object object = S3Helper.get().getObject(new GetObjectRequest("virtualyuhua", fileName));
            InputStream inputStream = object.getObjectContent();
            FileOutputStream outputStream = null;
            try {
                outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
                byte[] buffer = new byte[1024];

                int read;

                while((read = inputStream.read(buffer)) != -1){
                    outputStream.write(buffer, 0, read);
                }
                outputStream.flush();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally{
                if(outputStream != null){
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(inputStream != null){
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }
}
