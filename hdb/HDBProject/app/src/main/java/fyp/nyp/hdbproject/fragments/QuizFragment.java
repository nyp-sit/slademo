package fyp.nyp.hdbproject.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import fyp.nyp.hdbproject.LoadedInfo;
import fyp.nyp.hdbproject.R;
import fyp.nyp.hdbproject.aws.ContentExhibit;

/**
 * Created by L30911 on 1/27/2016.
 */
public class QuizFragment extends Fragment {
    String id;

    Button btnAnswerOne, btnAnswerTwo, btnAnswerThree, btnAnswerFour;
    TextView tvQuestion;
    ArrayList<Button> buttons = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_quiz, null);

        btnAnswerOne = (Button) view.findViewById(R.id.btnAnswerOne);
        btnAnswerTwo = (Button) view.findViewById(R.id.btnAnswerTwo);
        btnAnswerThree = (Button) view.findViewById(R.id.btnAnswerThree);
        btnAnswerFour = (Button) view.findViewById(R.id.btnAnswerFour);
        tvQuestion = (TextView) view.findViewById(R.id.tvQuestion);

        changeScreenDimens(view);
        buttons.add(btnAnswerOne);
        buttons.add(btnAnswerTwo);
        buttons.add(btnAnswerThree);
        buttons.add(btnAnswerFour);

        if(getArguments() != null){
            final Bundle bundle = getArguments();
            id = bundle.getString("id");

            final ContentExhibit exhibit = LoadedInfo.getContentExihibtByBeaconId(id);

            tvQuestion.setText(exhibit.getQuestions());
            for(int i = 0 ; i < exhibit.getOptions().size();i++){
                final int index = i;
                buttons.get(i).setText(exhibit.getOptions().get(i));
                buttons.get(i).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //This isnt doing anything here?
                        if(exhibit.getCorrectAnswerIndex() == index){
                            buttons.get(index).setText(Html.fromHtml("&#x2713;"));

                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent i = new Intent();
                                    i.putExtra("id", exhibit.getBeaconId());
                                    getTargetFragment().onActivityResult(getTargetRequestCode(), 1, i);
                                }
                            }, 1000);

                        }else{
                            buttons.get(index).setBackgroundDrawable(getResources().getDrawable(R.drawable.wrong_button));
                            buttons.get(index).setText("");
                        }
                    }
                });
            }
        }

        return view;
    }

    public void changeScreenDimens(View view){
        LinearLayout llParent = (LinearLayout) view.findViewById(R.id.llParentQuiz);
        //LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) llParent.getLayoutParams();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        int width = displaymetrics.widthPixels;

        params.height = (int)(height * 0.55);
        params.width = width;

        llParent.setLayoutParams(params);
    }
}
