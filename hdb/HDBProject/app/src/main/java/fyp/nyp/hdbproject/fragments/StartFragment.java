package fyp.nyp.hdbproject.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import fyp.nyp.hdbproject.LoadedInfo;
import fyp.nyp.hdbproject.R;
import fyp.nyp.hdbproject.aws.ContentExhibit;
import fyp.nyp.hdbproject.aws.GetMediaFiles;
import fyp.nyp.hdbproject.aws.PostDeviceStatus;

/**
 * Created by L30911 on 2/1/2016.
 */
public class StartFragment extends Fragment {

    boolean download;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            download = getArguments().getBoolean("download");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_start, null);

        final FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.flBottom);
        final FrameLayout llTopFrame = (FrameLayout) view.findViewById(R.id.flTopFrame);
        final ProgressBar pbProgress = (ProgressBar) view.findViewById(R.id.pbProgress);

        pbProgress.setIndeterminate(true);

        if(download){
            ArrayList<String> listOfVideoNames = new ArrayList<>();
            for(ContentExhibit exhibit : LoadedInfo.contentExhibitsList){
                listOfVideoNames.add(exhibit.getFileName());
            }
            try {
                new GetMediaFiles(getActivity(), pbProgress).execute(listOfVideoNames.toArray(new String[]{})).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }else{
            pbProgress.setIndeterminate(false);
            pbProgress.setVisibility(View.GONE);
        }
        Button btnStart = (Button)view.findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // getFragmentManager().beginTransaction().replace(R.id.flBottom, new MainFragment()).commit();
                Animation slide_up = AnimationUtils.loadAnimation(getActivity(),
                        R.anim.slide_in_out);

                llTopFrame.startAnimation(slide_up);
                llTopFrame.setVisibility(View.INVISIBLE);


                try {
                    TelephonyManager telephonyManager = (TelephonyManager)getActivity().getSystemService(Context.TELEPHONY_SERVICE);
                    String id = telephonyManager.getDeviceId();
                    new PostDeviceStatus().execute(id, "active").get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
        getFragmentManager().beginTransaction().replace(R.id.flBottom, new MainFragment()).commit();
        return view;
    }
}
