package fypj.com.weicong.slademowatch.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;
import android.support.wearable.view.WatchViewStub;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import fypj.com.weicong.slademowatch.R;

/**
 * Created by L30911 on 12/18/2015.
 */
public class MainFragment extends Fragment {
    private final int NUM_PAGES = 4;

    boolean notification = false;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_activity_main, null);
        final WatchViewStub stub = (WatchViewStub) view.findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                GridViewPager pager = (GridViewPager) stub.findViewById(R.id.gvp);

                DotsPageIndicator dotsPageIndicator = (DotsPageIndicator) stub.findViewById(R.id.page_indicator);
                dotsPageIndicator.setPager(pager);

                pager.setAdapter(new GridViewPagerAdapter(getFragmentManager()));
                pager.setOffscreenPageCount(4);

                Bundle bundle = getArguments();
                if(bundle != null) {
                    notification = bundle.getBoolean("fromNotification");
                }
            }
        });

        return view;
    }

    private class GridViewPagerAdapter extends FragmentGridPagerAdapter {
        Fragment fragment[] = new Fragment[]{new TrafficLightWatchFragment() ,new BeaconWatchFragment(), new StepsTrackerWatchFragment(), new TimingDelayWatchFragment()};

        public GridViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.app.Fragment getFragment(int i, int i1) {
            Fragment frag = fragment[i1];


            if(i1 == 0){
                frag.setArguments(getArguments());
            }
            return frag;
        }

        @Override
        public int getRowCount() {
            return 1;
        }

        @Override
        public int getColumnCount(int i) {
            return 4;
        }
    }
}
