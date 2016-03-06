package fyp.nyp.hdbproject.fragments;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

import fyp.nyp.hdbproject.LoadedInfo;
import fyp.nyp.hdbproject.PersistentSession;
import fyp.nyp.hdbproject.R;
import fyp.nyp.hdbproject.aws.Beacons;
import fyp.nyp.hdbproject.aws.ContentExhibit;
import fyp.nyp.hdbproject.aws.GetBeaconsTask;

/**
 * Created by L30911 on 1/15/2016.
 */
public class MainFragment extends Fragment {
    private final String TAG = this.getClass().getSimpleName();
    ArrayList<Beacons> beaconsArrayList;
    ArrayList<Button> buttonsList = new ArrayList<>();


    String [] colors = {
            "#ff97ba",
            "#ea4d5e",
            "#46cece",
            "#f49915",
            "#2d70b6",
            "#bac6dc"
    };

    int[] tickedDrawables = {
            R.drawable.pink_tile,
            R.drawable.ticked_red_tile,
            R.drawable.green_tile,
            R.drawable.orange_tile,
            R.drawable.blue_tile,
            R.drawable.purple_tile
    };
    Button btnExhibit1, btnExhibit2, btnExhibit3, btnExhibit4, btnExhibit5, btnExhibit6;
    Button btnRewards;
    LinearLayout llParentLayout;

    String previousId;
    SharedPreferences.Editor prefEditor;
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = getActivity().getSharedPreferences(TAG, Context.MODE_PRIVATE);
        prefEditor = preferences.edit();

        lastTime = preferences.getLong("last_time",0);
        if(lastTime - System.currentTimeMillis() < 30 * 60 * 1000){//30mins
            correctAnswer = preferences.getInt("last_score",0);

            HashSet<String> hashSet = (HashSet)preferences.getStringSet("completed", new HashSet<String>());
            for(String id : hashSet){
                LoadedInfo.setContentExhibitAnswered(LoadedInfo.getContentExihibtByBeaconId(id));
            }
        }
        try {
            beaconsArrayList = new GetBeaconsTask().execute().get();
            Collections.sort(beaconsArrayList, new Comparator<Beacons>() {
                @Override
                public int compare(Beacons lhs, Beacons rhs) {
                    return new Integer(Integer.parseInt(lhs.getName().replace("Exhibit ", ""))).compareTo(new Integer(Integer.parseInt(rhs.getName().replace("Exhibit ", ""))));
                }
            });
            PersistentSession.beaconsArrayList = beaconsArrayList;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
    SlidingUpPanelLayout slidingUpPanelLayout;
    ImageView ivProceedToQuiz;
    int correctAnswer = 0;
    TextView tvCorrectAnswer, tvStationName;
    LinearLayout llTabLayout;
    long lastTime = 0;
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_main, null);

        llTabLayout = (LinearLayout) view.findViewById(R.id.llTabLayout);

        btnExhibit1 = (Button)view.findViewById(R.id.btnExhibit1);
        btnExhibit2 = (Button)view.findViewById(R.id.btnExhibit2);
        btnExhibit3 = (Button)view.findViewById(R.id.btnExhibit3);
        btnExhibit4 = (Button)view.findViewById(R.id.btnExhibit4);
        btnExhibit5 = (Button)view.findViewById(R.id.btnExhibit5);
        btnExhibit6 = (Button)view.findViewById(R.id.btnExhibit6);


        btnRewards = (Button) view.findViewById(R.id.btnRewards);
        btnRewards.setEnabled(false);

        ivProceedToQuiz = (ImageView) view.findViewById(R.id.ivProceedToQuiz);
        llParentLayout = (LinearLayout) view.findViewById(R.id.llParentLayout);
        tvCorrectAnswer = (TextView) view.findViewById(R.id.tvCorrectAnswer);
        tvStationName = (TextView) view.findViewById(R.id.tvStationName);
        buttonsList.add(btnExhibit1);
        buttonsList.add(btnExhibit2);
        buttonsList.add(btnExhibit3);
        buttonsList.add(btnExhibit4);
        buttonsList.add(btnExhibit5);
        buttonsList.add(btnExhibit6);


        tvCorrectAnswer.setText(String.valueOf(correctAnswer));

        final LinearLayout flFrameLayout = (LinearLayout) view.findViewById(R.id.flFrameLayout);
        slidingUpPanelLayout = (SlidingUpPanelLayout) view.findViewById(R.id.sliding_layout);
        slidingUpPanelLayout.setCoveredFadeColor(Color.TRANSPARENT);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        final ContentFragment fragment = new ContentFragment();
        fragment.setTargetFragment(MainFragment.this, 1);

        transaction.replace(R.id.flFrameLayout, fragment).commit();
        llParentLayout.setVisibility(View.GONE);
        btnRewards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(LoadedInfo.answeredExhibits.size() == 1){

                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                Fragment fragment = new RewardsFragment();

                fragmentTransaction.add(R.id.flBottom, fragment).addToBackStack(null).commit();
                }
            }
        });
        btnRewards.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new Handler().post(new Runnable() {

                    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                    @Override
                    public void run()
                    {
                        SharedPreferences preferences = getActivity().getSharedPreferences(TAG, Context.MODE_PRIVATE);
                        SharedPreferences.Editor prefEditor = preferences.edit();

                        prefEditor.putLong("last_time", 0);
                        prefEditor.putInt("last_score", 0);
                        prefEditor.putStringSet("completed", new HashSet<String>());

                        prefEditor.commit();

                        Intent intent = getActivity().getIntent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        getActivity().overridePendingTransition(0, 0);
                        getActivity().finish();
                        intent.putExtra("download", false);

                        getActivity().overridePendingTransition(0, 0);
                        startActivity(intent);
                    }
                });
                return false;
            }
        });
        ivProceedToQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if(!LoadedInfo.isContentExhibitAnswered(LoadedInfo.getContentExihibtByBeaconId(previousId))){

                ivProceedToQuiz.setVisibility(View.GONE);
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                QuizFragment fragment = new QuizFragment();
                Bundle bundle = new Bundle();
                bundle.putString("id", previousId);
                fragment.setArguments(bundle);
                fragment.setTargetFragment(MainFragment.this, 1);

                transaction.replace(R.id.flFrameLayout, fragment).commit();
            }else{
                Toast.makeText(getActivity(), "Already answered this quiz", Toast.LENGTH_LONG).show();

            }
            }
        });

        for(int i = 0; i < buttonsList.size(); i++){
            Button button = buttonsList.get(i);
            final int index = i;
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (index == MainFragment.this.currentIndex) {
                        ivProceedToQuiz.setVisibility(View.VISIBLE);
                        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);

                        llTabLayout.setBackgroundColor(Color.parseColor(colors[index]));
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        ContentFragment fragment = new ContentFragment();
                        fragment.setTargetFragment(MainFragment.this, 1);

                        Bundle bundle = new Bundle();
                        previousId = beaconsArrayList.get(index).getId();

                        tvStationName.setText(beaconsArrayList.get(index).getName());
                        bundle.putString("id", beaconsArrayList.get(index).getId());
                        //bundle.putString("beaconId2",beaconsArrayList.get(index).getBeaconId2());
                        fragment.setArguments(bundle);
                        transaction.replace(R.id.flFrameLayout, fragment).commit();

                        //LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) llParent.getLayoutParams();
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                        DisplayMetrics displaymetrics = new DisplayMetrics();
                        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                        int height = displaymetrics.heightPixels;
                        int width = displaymetrics.widthPixels;

                        params.height = (int) (height * 0.6);
                        params.width = width;

                        flFrameLayout.setLayoutParams(params);
                    }
                }
            });
        }
        IntentFilter intentFilter = new IntentFilter("fyp.nyp.hdbproject.SHORTEST_CHANGED");
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Something new here
                Bundle bundle = intent.getExtras();
                String beaconId = bundle.getString("beaconId");
                int indexOfBeacon = -1;
                for(int i = 0; i < beaconsArrayList.size();i++){
                    Log.d(TAG, "Beacon Id : "+beaconId);
                    if(beaconsArrayList.get(i).getId().equals(beaconId)){
                        indexOfBeacon = i;
                    }
                }

                if(indexOfBeacon != -1) {
                    for(Button button : buttonsList){
                        button.setSelected(false);
                    }
                    //Index, so I probably want to add something here?
                    buttonsList.get(indexOfBeacon).setSelected(true);
                    MainFragment.this.currentIndex = indexOfBeacon;
                }
            }
        };

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, intentFilter);
        return view;
    }
    int currentIndex = -1;
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

        ivProceedToQuiz.setVisibility(View.VISIBLE);
        for(int i = 0; i < beaconsArrayList.size();i++){
            Beacons beacon = beaconsArrayList.get(i);
            if(beacon.getId().equals(previousId)){
                buttonsList.get(i).setBackgroundDrawable(getResources().getDrawable(tickedDrawables[i]));
                break;
            }
        }
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        ContentFragment fragment = new ContentFragment();
        fragment.setTargetFragment(MainFragment.this, 1);

        Bundle bundle = new Bundle();

        bundle.putString("id", previousId);

        fragment.setArguments(bundle);
        transaction.replace(R.id.flFrameLayout, fragment).commit();

        correctAnswer++;
        tvCorrectAnswer.setText(String.valueOf(correctAnswer));
        LoadedInfo.setContentExhibitAnswered(LoadedInfo.getContentExihibtByBeaconId(previousId));

        prefEditor.putLong("last_time", System.currentTimeMillis());
        prefEditor.putInt("last_score", correctAnswer);

        HashSet<String> hashSet = new HashSet<>();
        for(ContentExhibit answered : LoadedInfo.answeredExhibits){
            hashSet.add(answered.getBeaconId());
        }
        if(hashSet.size() == 1){
            btnRewards.setEnabled(true);
        }
        prefEditor.putStringSet("completed", hashSet);
        prefEditor.commit();
    }
}
