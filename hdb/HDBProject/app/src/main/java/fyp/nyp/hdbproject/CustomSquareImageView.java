package fyp.nyp.hdbproject;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by L30911 on 2/2/2016.
 */
public class CustomSquareImageView extends ImageView {
    public CustomSquareImageView(Context context) {
        super(context);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        setMeasuredDimension(width, width);
    }

    public CustomSquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSquareImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

}
