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

/**
 @author yogesh
 adapted from https://stackoverflow.com/questions/37772918/android-change-recycler-view-column-no-on-pinch-zoom
  * */
public class PinchRecyclerView extends RecyclerView {
    private static final int INVALID_POINTER_ID = -1;
    private static final int MIN_CHILD_SIZE = 150;

    private int mActivePointerId = INVALID_POINTER_ID;
    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;
    private float maxWidth = 0.0f;
    private float maxHeight = 0.0f;
    private float mLastTouchX;
    private float mLastTouchY;
    private float mPosX;
    private float mPosY;
    private float width;
    private float height;


    public PinchRecyclerView(Context context) {
        super(context);
        if (!isInEditMode())
            mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
    }

    public PinchRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode())
            mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
    }

    public PinchRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode())
            mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent ev) {
        super.onTouchEvent(ev);
        mScaleDetector.onTouchEvent(ev);

        return true;
    }

    public float getScaleFactor() {
        return mScaleFactor;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        private static final float maxScaleFactor = 3;
        private static final float minScaleFactor = 1;

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();
            mScaleFactor = Math.max(minScaleFactor, Math.min(mScaleFactor, maxScaleFactor)); //clamps values between max, min scale factor

            for(int i = 0 ; i < getChildCount(); i++) {
                View cell = getChildAt(i);
                int newSize = (int) (MIN_CHILD_SIZE * mScaleFactor);
                ViewGroup.LayoutParams params = cell.getLayoutParams();
                params.height = newSize;
                params.width = newSize;
                cell.setLayoutParams(params);
                Log.i("rge", "" + newSize);
            }

            invalidate();
            return true;
        }
    }
}