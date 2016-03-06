package fyp.nyp.hdbproject;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by L30911 on 1/21/2016.
 */
public class CustomFontTextView extends TextView {
    public CustomFontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/myriad_pro_bold.ttf"));
        //this.setTextColor(Color.parseColor("#9aa3ac"));
        //this.setGravity(TEXT_ALIGNMENT_CENTER);
    }
}
