package fypj.com.weicong.slademowatch;

import android.content.Context;
import android.graphics.Color;
import android.support.wearable.view.WearableListView;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by L30911 on 12/18/2015.
 */
public class WearableListItemLayout extends LinearLayout implements WearableListView.OnCenterProximityListener {
    public WearableListItemLayout(Context context) {
        super(context);
    }

    public WearableListItemLayout(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public WearableListItemLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    TextView tvTime;
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        tvTime = (TextView)findViewById(R.id.tvTime);
    }

    @Override
    public void onCenterPosition(boolean b) {
        tvTime.setTextColor(getResources().getColor(android.R.color.background_dark));
        //tvTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);

    }

    @Override
    public void onNonCenterPosition(boolean b) {
        tvTime.setTextColor(Color.parseColor("#424242"));
        tvTime.setAlpha(0.6f);
        //tvTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
    }
}
