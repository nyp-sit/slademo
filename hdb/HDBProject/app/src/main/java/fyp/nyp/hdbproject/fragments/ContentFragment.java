package fyp.nyp.hdbproject.fragments;

import android.annotation.TargetApi;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import fyp.nyp.hdbproject.LoadedInfo;
import fyp.nyp.hdbproject.R;
import fyp.nyp.hdbproject.aws.ContentExhibit;

/**
 * Created by L30911 on 1/21/2016.
 */
public class ContentFragment extends Fragment {

    private final String TAG = this.getClass().getSimpleName();
    boolean isPlayingVideo = false;
    String id;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    boolean isPlayingAudio = false;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_content_text, null);

        TextView tvContent = (TextView)view.findViewById(R.id.tvContent);
        final VideoView vvVideo = (VideoView) view.findViewById(R.id.vvVideo);
        LinearLayout llAudioLayout = (LinearLayout) view.findViewById(R.id.llAudioLayout);
        final ImageView btnPlayButtonButton = (ImageView) view.findViewById(R.id.btnPlayButton);
        final SeekBar sbProgress = (SeekBar) view.findViewById(R.id.sbProgress);


        vvVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isPlayingVideo){
                    vvVideo.resume();

                    isPlayingVideo = true;
                }else {
                    vvVideo.pause();

                    isPlayingVideo = false;
                }
            }
        });
        Bundle bundle = getArguments();
        if(bundle != null){
            id = bundle.getString("id");
            ContentExhibit exhibit = LoadedInfo.getContentExihibtByBeaconId(id);
            if(exhibit.getFileType() == LoadedInfo.FILE_TYPE.VIDEO.ordinal()){
                Log.d(TAG, "This is a video!");
                vvVideo.setVideoPath(getActivity().getFilesDir().getAbsolutePath() + "/"+exhibit.getFileName());
                vvVideo.start();
                vvVideo.setVisibility(View.VISIBLE);
            }else if(exhibit.getFileType() == LoadedInfo.FILE_TYPE.AUDIO.ordinal()){
                //Media Player
                final MediaPlayer mediaPlayer = new MediaPlayer();
                try {
                    FileInputStream inputStream = new FileInputStream(getActivity().getFilesDir().getAbsolutePath()+"/"+exhibit.getFileName());
                    mediaPlayer.setDataSource(inputStream.getFD());
                    inputStream.close();

                    mediaPlayer.prepare();

                    sbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            mediaPlayer.seekTo(progress);
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }
                    });
                    sbProgress.setMax(mediaPlayer.getDuration());
                    btnPlayButtonButton.setOnClickListener(new View.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        @Override
                        public void onClick(View v) {
                            final Handler handler = new Handler();

                            if(!isPlayingAudio) {
                                btnPlayButtonButton.setBackground(getResources().getDrawable(R.drawable.img_btn_pause));
                                mediaPlayer.start();

                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        sbProgress.setProgress(mediaPlayer.getCurrentPosition());
                                        handler.postDelayed(this, 100);
                                    }
                                }, 100);
                                isPlayingAudio = true;
                            }else{
                                btnPlayButtonButton.setBackground(getResources().getDrawable(R.drawable.img_btn_play));
                                mediaPlayer.pause();

                                isPlayingAudio = false;
                            }
                        }
                    });
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                llAudioLayout.setVisibility(View.VISIBLE);
            }
            tvContent.setText(exhibit.getContent());
        }
        LinearLayout llParent = (LinearLayout) view.findViewById(R.id.llParent);
        //LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) llParent.getLayoutParams();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        int width = displaymetrics.widthPixels;

        params.height = (int)(height * 0.6);
        params.width = width;

        llParent.setLayoutParams(params);



        return view;
    }
}
