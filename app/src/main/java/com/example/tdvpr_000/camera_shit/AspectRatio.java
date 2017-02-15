package com.example.tdvpr_000.camera_shit; /**
 * Created by tdvpr_000 on 2/1/2017.
 */
import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/*
 * Layout that adjusts to maintain a specific aspect ratio.
 */
public class AspectRatio extends FrameLayout {

    private double targetAspectRatio = -1.0;        // initially use default window size

    public AspectRatio(Context context) {
        super(context);
    }

    public AspectRatio(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setAspectRatio(double aspectRatio) {
        if (aspectRatio < 0) {
            throw new IllegalArgumentException();
        }

        if (targetAspectRatio != aspectRatio) {
            targetAspectRatio = aspectRatio;
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if (targetAspectRatio > 0) {
            int initialWidth = MeasureSpec.getSize(widthMeasureSpec);
            int initialHeight = MeasureSpec.getSize(heightMeasureSpec);

            // padding
            int horizontalPadding = getPaddingLeft() + getPaddingRight();
            int verticalPadding = getPaddingTop() + getPaddingBottom();
            initialWidth -= horizontalPadding;
            initialHeight -= verticalPadding;

            double viewAspectRatio = (double) initialWidth / initialHeight;
            double aspectDifference = targetAspectRatio / viewAspectRatio - 1;

            if (Math.abs(aspectDifference) < 0.01) {
                //no changes
            } else {
                if (aspectDifference > 0) {
                    initialHeight = (int) (initialWidth / targetAspectRatio);
                } else {
                    initialWidth = (int) (initialHeight * targetAspectRatio);
                }
                initialWidth += horizontalPadding;
                initialHeight += verticalPadding;
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(initialWidth, MeasureSpec.EXACTLY);
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(initialHeight, MeasureSpec.EXACTLY);
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}