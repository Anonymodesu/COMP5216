package sydney.edu.au.teammeet;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class LockableRecyclerView extends RecyclerView {

    private boolean scrollable;

    public LockableRecyclerView(Context context) {
        super(context);
        scrollable = true;
    }

    public LockableRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        scrollable = true;
    }

    public LockableRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        scrollable = true;
    }

    public void setScrollable(boolean scrollable) {
        this.scrollable = scrollable;
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return scrollable && super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent ev) {
        return scrollable && super.onTouchEvent(ev);
    }



}