package com.app.nao.photorecon.ui.album;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AlertDialog;


import com.app.nao.photorecon.model.usecase.DeletePhotoFromLocalFile;
import com.app.nao.photorecon.model.usecase.DeletePhotoFromRealm;

import org.bson.types.ObjectId;



public class AlbumImageTapHandler implements View.OnLongClickListener{
    private Context context;
    private View view;
    private ObjectId albumId;
    AlbumImageTapHandler(Context context, ObjectId albumId){
        this.context = context;
        this.albumId = albumId;
    }
    @Override
    public boolean onLongClick(View v) {
        // v.setVisibility(View.INVISIBLE);
        Dialog d =displayDeleteDialog(v);
        // ここ挙動チェック
        return d != null;
        }
    Dialog displayDeleteDialog(View view){

        // AssetManager assetManager = getResources().getAssets();
        //
        AlertDialog.Builder builder = new AlertDialog.Builder((Activity)view.getContext());
        CharSequence[] options ={"画像をアルバムから削除"};   //TODO:"R.string.deleteで書き換える"
        builder.setItems(options,new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        checkDeleteFunction(view);
                    }
                });
        return builder.show();
    }
    // protected boolean checkDelete = false;
    private boolean checkDeleteFunction(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder((Activity)view.getContext());
        builder.setMessage(/*R.string.dialog_start_game*/"Delete this recognition result?\nこの結果をアプリケーションから削除しますか?\n保存元の画像は削除されません．")
                .setPositiveButton(/*R.string.start*/"Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // TODO: このリスナー関数内を別で記述．厳密なので，interfaceから書く．
                        //Realmから削除していることに注意．
                        DeletePhotoFromRealm deleteAlbumFromRealm =new DeletePhotoFromRealm();
                        if(deleteAlbumFromRealm.deleteAlbumFromRealm(albumId) != null){
                            DeletePhotoFromLocalFile deletePhotoFromLocalFile = new DeletePhotoFromLocalFile();
                            deletePhotoFromLocalFile.deletePhotoFromLocalFile(view.getContext(),albumId.toString());
                            //TODO:描写を更新する．もっといい方法があるかもしれない．

                            final Intent intent = new Intent(view.getContext(), AlbumViewActivity.class);
                            view.getContext().startActivity(intent);
                            ((Activity) view.getContext()).finish();

                            // TODO:インテントを再起動しない方法を採用.問題ないかチェックすること．
                            //  // mPhotoの値が変わってるので画像読み出し時にnull参照が発生する．一旦上に戻す．
                            // ((AlbumViewActivity)context).resetFilterSetting();
                            // ((AlbumViewActivity)context).updateRecyclerView(((AlbumViewActivity)context).getAllPhotoList());
                        }else{
                            Log.i("u_REALM ","削除処理に失敗しました．");
                        }
                    }
                })
                .setNegativeButton(/*R.string.cancel*/ "No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        // checkDelete =false;
                    }
                });
        // TODO:戻り値いらないかも？
        builder.show();
        return true;
    }
}

