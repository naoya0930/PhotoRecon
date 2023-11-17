package com.app.nao.photorecon.model.usecase;

import android.graphics.Bitmap;
import android.graphics.Rect;

import com.app.nao.photorecon.ui.main.Result;

import java.util.ArrayList;
import java.util.List;

public class SnapRectanglePhoto  {
    public ArrayList<Bitmap> makeSegmentedImages(
            float mIvScaleX,float mIvScaleY,
            float mStartX,float mStartY,
            Bitmap sourceImg,ArrayList<Result> results) {
        ArrayList<Bitmap> bl = new ArrayList<Bitmap>();
        for (Result result : results) {
            Rect rect = result.getRect();
            Bitmap b = Bitmap.createBitmap(
                    sourceImg,
                    (int)((rect.left - mStartX) / mIvScaleX),
                    (int)((rect.top - mStartY) / mIvScaleY),
                    (int)(rect.width() / mIvScaleX),
                    (int)(rect.height() / mIvScaleY)
            );
            bl.add(b);
        }
        return bl;
    }
    public List<Result> makeSegmentedRectangle(
            float mIvScaleX,float mIvScaleY,
            float mStartX,float mStartY,
            ArrayList<Result> results
    ){
        List<Result> rs = new ArrayList<>();

        for (Result result : results) {
            Rect rect = result.getRect();
            // 縮小後にrectを計算しているので，スケール調整はいらない
            rs.add(new Result(result.getClassIndex(),result.getScore(), new Rect(
                    (int)((rect.left - mStartX)),
                    (int)((rect.top - mStartY)),
                    (int)((rect.right -mStartX)),
                    (int)((rect.bottom -mStartY))
            )));
        }

        return rs;
    }
}
