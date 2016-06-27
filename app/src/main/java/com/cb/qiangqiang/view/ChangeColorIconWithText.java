package com.cb.qiangqiang.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.cb.qiangqiang.R;

/**
 * Created by cb on 2015/12/31.
 */
public class ChangeColorIconWithText extends View {
    private static final int COLOR = 0x4FC3F7;
    private static final int COLOR_DEFAULT = 0xBDBDBD;
    private static final String INSTANCE_STATUS = "instance_status";
    private static final String ALPHA_STATUS = "alpha_status";

    private int mColor;
    private Bitmap mIconBitmap;
    private Bitmap mIconSelectedBitmap;
    private String mText = "热帖";
    private int mTextSize;
    private int mTextDefaultColor;

    private Canvas mCanvas;
    private Bitmap mBitmap;
    private Paint mPaint;
    private Paint mBitmapPaint;
    private float mAlpha;
    private Rect mIconRect;
    private Rect mTextBound;
    private Paint mTextPaint;

    public ChangeColorIconWithText(Context context) {
        this(context, null);
    }

    public ChangeColorIconWithText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChangeColorIconWithText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ChangeColorIconWithText);
        mColor = array.getColor(R.styleable.ChangeColorIconWithText_changColor, COLOR);
        mIconBitmap = ((BitmapDrawable)array.
                getDrawable(R.styleable.ChangeColorIconWithText_iconImg)).
                getBitmap();
        mIconSelectedBitmap = ((BitmapDrawable)array.
                getDrawable(R.styleable.ChangeColorIconWithText_iconImgSelected)).
                getBitmap();
        mText = array.getString(R.styleable.ChangeColorIconWithText_textContent);
        mTextSize = (int) array.getDimension(R.styleable.ChangeColorIconWithText_textSize,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12,
                        getResources().getDisplayMetrics()));
        mTextDefaultColor = array.getColor(R.styleable.ChangeColorIconWithText_textDefaultColor, COLOR_DEFAULT);
        array.recycle();

        mTextBound = new Rect();
        mTextPaint = new Paint();
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(0xFF555555);
        mTextPaint.setDither(true);
        mTextPaint.setAntiAlias(true);
        mTextPaint.getTextBounds(mText, 0, mText.length(), mTextBound);
        mBitmapPaint = new Paint();
        mBitmapPaint.setDither(true);
        mBitmapPaint.setFilterBitmap(true);
        mBitmapPaint.setAntiAlias(true);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE_STATUS, super.onSaveInstanceState());
        bundle.putFloat(ALPHA_STATUS, mAlpha);
        return bundle;
    }
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle){
            Bundle bundle = (Bundle) state;
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE_STATUS));
            mAlpha = bundle.getFloat(ALPHA_STATUS);
            return;
        }
        super.onRestoreInstanceState(state);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int iconWidth = Math.min(getMeasuredWidth() - getPaddingLeft() - getPaddingRight(),
                getMeasuredHeight() - getPaddingTop() - getPaddingBottom() - mTextBound.height());
        int left = (getMeasuredWidth() - iconWidth) / 2;
        int top = (getMeasuredHeight() - mTextBound.height() - iconWidth) / 2;
        mIconRect = new Rect(left, top, left + iconWidth, top + iconWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        canvas.drawBitmap(mIconBitmap, null, mIconRect, null);
        int alpha = (int) Math.ceil(255 * mAlpha);
        drawSourceBitmap(canvas, 255 - alpha);
        drawTargetBitmap(alpha);
        drawSourceText(canvas, alpha);
        drawTargetText(canvas, alpha);
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
    }

    private void drawSourceBitmap(Canvas canvas, int alpha){
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setFilterBitmap(true);
        mPaint.setAlpha(alpha);
        canvas.drawBitmap(mIconBitmap, null, mIconRect, mPaint);
    }

    private void drawTargetBitmap(int alpha) {
        mBitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mPaint = new Paint();
        mPaint.setColor(mColor);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setFilterBitmap(true);
        mPaint.setAlpha(alpha);
        mCanvas.drawRect(mIconRect, mPaint);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        mPaint.setAlpha(255);
        mCanvas.drawBitmap(mIconSelectedBitmap, null, mIconRect, mPaint);
    }

    private void drawTargetText(Canvas canvas, int alpha) {
        mTextPaint.setColor(mColor);
        mTextPaint.setAlpha(alpha);
        int x = (getMeasuredWidth() - mTextBound.width()) / 2;
        int y = mIconRect.bottom + mTextBound.height();
        canvas.drawText(mText, x, y, mTextPaint);
    }

    private void drawSourceText(Canvas canvas, int alpha) {
        mTextPaint.setColor(mTextDefaultColor);
        mTextPaint.setAlpha(255 - alpha);
        int x = (getMeasuredWidth() - mTextBound.width()) / 2;
        int y = mIconRect.bottom + mTextBound.height();
        canvas.drawText(mText, x, y, mTextPaint);
    }

    public void setIconAlpha(float alpha){
        mAlpha = alpha;
        invalidateView();
    }

    private void invalidateView() {
        if(Looper.getMainLooper() == Looper.myLooper()){
            invalidate();
        }else {
            postInvalidate();
        }
    }


}
