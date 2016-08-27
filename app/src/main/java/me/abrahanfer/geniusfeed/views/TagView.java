package me.abrahanfer.geniusfeed.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import me.abrahanfer.geniusfeed.R;

/**
 * Created by abrahan on 27/08/16.
 */

public class TagView extends View {

    private Boolean mShowButton;
    private int mBackgroundColor;
    private int mTextColor;

    private Paint tagRectangleColor;
    private Paint tagTextColor;

    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.mBackgroundColor = backgroundColor;
        invalidate();
        requestLayout();
    }

    public int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(int textColor) {
        this.mTextColor = textColor;
        invalidate();
        requestLayout();
    }

    public boolean isShowButton() {
        return mShowButton;
    }

    public void setShowButton(boolean showButton) {
        mShowButton = showButton;
        invalidate();
        requestLayout();
    }


    public TagView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attributeSet,
                R.styleable.TagView,
                0, 0);

        try {
            mShowButton = a.getBoolean(R.styleable.TagView_showButton, false);
            mBackgroundColor = a.getColor(R.styleable.TagView_backgroudColor, Color.BLACK);
            mTextColor = a.getColor(R.styleable.TagView_textColor, Color.WHITE);
        } finally {
            a.recycle();
        }
    }

    public void init() {
        tagTextColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        tagTextColor.setColor(mTextColor);
    }

}
