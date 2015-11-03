package net.dian1.player.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * Created by Dmall on 2015/11/3.
 */
public class GridViewNoneScroll extends GridView {

    public GridViewNoneScroll(Context context) {
        super(context);
    }

    public GridViewNoneScroll(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GridViewNoneScroll(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(
                Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
