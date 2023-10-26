package com.app.nao.photorecon.model.usecase;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;

import com.app.nao.photorecon.Result;
import com.app.nao.photorecon.model.dao.RealmDAO;
import com.app.nao.photorecon.model.entity.Photo;

import java.net.URI;
import java.util.ArrayList;

public class SnapRectanglePhoto  {
    public ArrayList<Bitmap> makeSegmentedImages(float mIvScaleX,float mIvScaleY,
                                                 float mStartX,float mStartY,
                                                 Bitmap sourceImg,ArrayList<Result> results) {
        ArrayList<Bitmap> bl = new ArrayList<Bitmap>();
        for (Result result : results) {
            Rect rect = result.getRect();
            Bitmap b = Bitmap.createBitmap(
                    sourceImg,
                    (int)((rect.left - mStartX) / mIvScaleX) ,
                    (int)((rect.top - mStartY) / mIvScaleY),
                    (int)(rect.width() / mIvScaleX) ,
                    (int)(rect.height() / mIvScaleY)
            );
            bl.add(b);
        }
        return bl;
    }
}
