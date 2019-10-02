package sydney.edu.au.teammeet;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;

import androidx.recyclerview.widget.RecyclerView;

//assumes first child view is a RecyclerView
public class LockableScrollView extends HorizontalScrollView {

    private boolean scrollable;

    public LockableScrollView(Context context) {
        super(context);
        scrollable = true;
    }


    public LockableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        scrollable = true;
    }

    public LockableScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        scrollable = true;
    }

    /*
    public LockableHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        recyclerView = (RecyclerView) getChildAt(0);
    }
    */
    public void setScrollable(boolean scrollable) {
        this.scrollable = scrollable;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return scrollable && super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        return scrollable && super.onTouchEvent(event);
    }
}
