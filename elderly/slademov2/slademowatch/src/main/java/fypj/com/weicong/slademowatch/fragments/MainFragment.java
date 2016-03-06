package fypj.com.weicong.slademowatch.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.wearable.view.WatchViewStub;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import fypj.com.weicong.slademowatch.R;
import fypj.com.weicong.slademowatch.Session;

/**
 * Created by L30911 on 12/18/2015.
 */
public class MainFragment extends Fragment {
    private final int NUM_PAGES = 2;
    ViewPager pager;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        Bundle bundle = getArguments();
        if(bundle != null && bundle.getBoolean("fromNotification")){
            Session.fromNotification = bundle.getBoolean("fromNotification");
        }
        View view = inflater.inflate(R.layout.fragment_activity_main, null);

        final WatchViewStub stub = (WatchViewStub) view.findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                pager = (ViewPager) stub.findViewById(R.id.gvp);
                pager.setAdapter(new ViewPagerAdapter(getFragmentManager()));
                pager.setOffscreenPageLimit(4);

                pager.getAdapter().notifyDataSetChanged();
              //  pager.setCurrentItem(3);
//
               // final DrawablePagerIndicator  titleIndicator = (DrawablePagerIndicator)stub.findViewById(R.id.page_indicator);

              //  titleIndicator.setViewPager(pager);

            }
        });

        return view;
    }



    public class ViewPagerAdapter extends FragmentStatePagerAdapter {

        public ViewPagerAdapter(FragmentManager fm){
            super(fm);
        }



        @Override
        public Fragment getItem(int position) {
            Fragment fragment = new Fragment();

            switch(position){
                case 0 :
                    fragment = new TrafficLightWatchFragment();
                    break;
                //case 1:
                  //  fragment = new BeaconWatchFragment();
                    //break;
                case 1:
                    fragment = new StepsTrackerWatchFragment();
                    break;
                //case 3:
                  //  fragment = new TimingDelayWatchFragment();
                    //break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

}
