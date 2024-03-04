package com.app.nao.photorecon.ui.main;

import android.content.Context;
import android.view.View;

import com.app.nao.photorecon.model.entity.Photo;
import com.app.nao.photorecon.model.usecase.SavePhoto;
import com.app.nao.photorecon.ui.util.DateManager;


public class ResisterPhotoButtonHandler implements View.OnClickListener{
    private Photo mPhoto;
    private Context context;
    private MainActivity mMainActivity;
    // NOTE: コンストラクタはリスナ設定時に呼ばれるので注意．
    // クリックした直後の変数がほしい場合は，リスナ側に記載する．
    ResisterPhotoButtonHandler(Context context){
        this.mMainActivity =(MainActivity)context;
        this.context = context;
    };
    public void onClick(View v) {
        //ローカルファイルに保存する
        //日付をセットする．
        this.mPhoto = mMainActivity.getPhoto();
        mPhoto.setSaved_at(DateManager.getLocalDate());
        // resultの情報をもとにrealmに情報を登録する．
        SavePhoto mSavePhoto = new SavePhoto(mPhoto);
        mSavePhoto.saveSegmentBitmapToDirectory(context,mMainActivity.getPreSegmentedThumbnails());
        mSavePhoto.saveOriginalBitmapToDirectory(context,mMainActivity.getBitmap());
        mSavePhoto.registerToRealm();
        mMainActivity.setResisterButtonState(false);
       //  mRegisterButton.setEnabled(false);

        // TODO: 成功，失敗の結果をイベント通知
        mMainActivity.setSelectedImageUri(null);
    }
}
