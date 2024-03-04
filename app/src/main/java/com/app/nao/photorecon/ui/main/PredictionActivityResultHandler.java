package com.app.nao.photorecon.ui.main;

import static android.app.Activity.RESULT_CANCELED;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;


//参照渡しに気をつける．
public class PredictionActivityResultHandler implements ActivityResultCallback<ActivityResult> {
    // private Uri mSelectedImageUri;
    //TODO:メモリリークがあるため，非常に良くない実装であること
    //TODO: とりあえず動くで実装する．参照等がめちゃくちゃなので整理する

    private MainActivity mMainActivity;
    private Bitmap mBitmap;
    private Uri mSelectedImageUri;

    PredictionActivityResultHandler(Context context){
        this.mMainActivity=(MainActivity) context;
        this.mBitmap = mMainActivity.getBitmap();
        this.mSelectedImageUri = mMainActivity.getSelectedImageUri();
    }

    @Override
    public void onActivityResult(ActivityResult activityResult) {

        if(activityResult.getResultCode() !=RESULT_CANCELED) {

            if (activityResult.getResultCode() == Activity.RESULT_OK) {
                if (activityResult.getData() != null) {
                    //結果を受け取った後の処理
                    this.mSelectedImageUri = activityResult.getData().getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    if (mSelectedImageUri != null) {
                        Cursor cursor = mMainActivity.getContentResolver().query(mSelectedImageUri,
                                filePathColumn, null, null, null);
                        if (cursor != null) {
                            //画像の配置

                            cursor.moveToFirst();
                            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                            String picturePath = cursor.getString(columnIndex);
                            mBitmap = BitmapFactory.decodeFile(picturePath);
                            Matrix matrix = new Matrix();
                            matrix.postRotate(0.0f);
                            mBitmap = (Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true));
                            // mImageView.setImageBitmap(mBitmap);
                            mMainActivity.setImageViewBitmap(mBitmap);
                            ImageView mImageView = mMainActivity.getImageView();
                            cursor.close();
                            // 推論開始
                            // Mresultviewarraylistが結果を持ってる．
                            // mButtonDetect.setEnabled(false);
                            mMainActivity.setProgressBarInvisible(ProgressBar.VISIBLE);
                            //mProgressBar.setVisibility(ProgressBar.VISIBLE);
                            //mButtonDetect.setText(getString(R.string.run_model));
                            float mImgScaleX = (float)mBitmap.getWidth() / PrePostProcessor.mInputWidth;
                            float mImgScaleY = (float)mBitmap.getHeight() / PrePostProcessor.mInputHeight;

                            float mIvScaleX = (mBitmap.getWidth() > mBitmap.getHeight() ? (float)mImageView.getWidth() / mBitmap.getWidth() : (float)mImageView.getHeight() / mBitmap.getHeight());
                            float mIvScaleY  = (mBitmap.getHeight() > mBitmap.getWidth() ? (float)mImageView.getHeight() / mBitmap.getHeight() : (float)mImageView.getWidth() / mBitmap.getWidth());

                            float mStartX = (mImageView.getWidth() - mIvScaleX * mBitmap.getWidth())/2;
                            float mStartY = (mImageView.getHeight() -  mIvScaleY * mBitmap.getHeight())/2;
                            mMainActivity.setImageRatio(mImgScaleX,mImgScaleY,mIvScaleX,mIvScaleY,mStartX,mStartY);
                            mMainActivity.setBitmap(mBitmap);
                            mMainActivity.setSelectedImageUri(mSelectedImageUri);
                            Thread thread = new Thread(mMainActivity);
                            thread.start();
                        }
                    }
                } else {

                }
            }
        }

    }
}
