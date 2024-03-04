package com.app.nao.photorecon.ui.album;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.app.nao.photorecon.model.entity.SegmentedPhoto;

import org.bson.types.ObjectId;

public class ThumbnailTapComponentAdapter implements View.OnClickListener{
    private Context context;
    private BoxPaintView mBoxPaintView;
    private LinearLayout thumbnailListLayout;
    private SegmentedPhoto mSegmentedPhoto;
    ThumbnailTapComponentAdapter(Context context,LinearLayout thumbnailListLayout, SegmentedPhoto segmentedPhoto, BoxPaintView boxPaintView){
        this.context = context;
        this.thumbnailListLayout = thumbnailListLayout;
        this.mSegmentedPhoto = segmentedPhoto;
        this.mBoxPaintView = boxPaintView;

    }
    @Override
    public void onClick(View v) {
        if(mSegmentedPhoto==null) { return;
        } else {
            // imageviewの枠囲み
            //TODO: 一度タップすると二度と消えなくなるので修正する．
//            int borderColor = Color.YELLOW; // 任意の色に変更
//            int borderWidth = 10; // 任意の枠の太さに変更
//            ShapeDrawable shapeDrawable = new ShapeDrawable();
//            shapeDrawable.getPaint().setColor(borderColor);
//            shapeDrawable.getPaint().setStyle(Paint.Style.STROKE);
//            shapeDrawable.getPaint().setStrokeWidth(borderWidth);
//            thumbnailListLayout.setBackground(shapeDrawable);

            // オリジナルイメージ側に四角い枠を表示
            mBoxPaintView.setSegmentedPhoto(mSegmentedPhoto);
            mBoxPaintView.invalidate();
            mBoxPaintView.setVisibility(View.VISIBLE);
        };
    }
}
