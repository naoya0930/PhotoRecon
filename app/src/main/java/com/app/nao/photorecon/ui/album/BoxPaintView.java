package com.app.nao.photorecon.ui.album;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.app.nao.photorecon.model.entity.SegmentedPhoto;

public class BoxPaintView extends View{
    private final static int TEXT_X = 40;
    private final static int TEXT_Y = 35;
    private final static int TEXT_WIDTH = 260;
    private final static int TEXT_HEIGHT = 50;

    private Paint mPaintRectangle;
    private Paint mPaintText;
    private SegmentedPhoto mSegmentedPhoto;
    public BoxPaintView(Context context) {
        super(context);
    }

    public BoxPaintView(Context context, AttributeSet attrs){
        super(context, attrs);
        mPaintRectangle = new Paint();
        mPaintRectangle.setColor(Color.YELLOW);
        mPaintText = new Paint();
    }
    public void setSegmentedPhoto(SegmentedPhoto photo){
        this.mSegmentedPhoto = photo;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        if(mSegmentedPhoto==null){return ;}
        super.onDraw(canvas);
        Log.i("ACTION", "描写しています．");
        mPaintRectangle.setStrokeWidth(5);
        mPaintRectangle.setStyle(Paint.Style.STROKE);
        //canvas.drawRect(result.rect, mPaintRectangle);
        Rect segmentedRect = mSegmentedPhoto.getRect();
        canvas.drawRect(segmentedRect, mPaintRectangle);
        Path mPath = new Path();
        RectF mRectF = new RectF(
                segmentedRect.left,
                segmentedRect.top,
                segmentedRect.left + TEXT_WIDTH,
                segmentedRect.top + TEXT_HEIGHT);
        mPath.addRect(mRectF, Path.Direction.CW);
        mPaintText.setColor(Color.MAGENTA);
        canvas.drawPath(mPath, mPaintText);

        mPaintText.setColor(Color.WHITE);
        mPaintText.setStrokeWidth(0);
        mPaintText.setStyle(Paint.Style.FILL);
        mPaintText.setTextSize(32);
        canvas.drawText(String.format(
                // "%s %.2f", PrePostProcessor.mClasses[result.classIndex]result.score),
                "%s %.2f",mSegmentedPhoto.getCategorization_name(),mSegmentedPhoto.getScore()),
                segmentedRect.left + TEXT_X,
                segmentedRect.top + TEXT_Y, mPaintText);
    }
}

