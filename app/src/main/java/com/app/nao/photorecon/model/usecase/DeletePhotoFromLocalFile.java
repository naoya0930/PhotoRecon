package com.app.nao.photorecon.model.usecase;

import android.content.Context;

import com.app.nao.photorecon.model.repository.LocalFileUtil;

import java.io.File;

public class DeletePhotoFromLocalFile {

    public void deletePhotoFromLocalFile(Context context, String objectId) {
        // コンテキストを使用する処理はここに入れない，
        // ファイルはappdata/files/app_data/original/{objectid} or thumbnails/{ObjectId}で保存
        File originalImageDir = new File(context.getFilesDir(), LocalFileUtil.LOCAL_ORIGINAL_FILE_DIRECTORY + objectId);
        File thumbnailImageDir = new File(context.getFilesDir(),LocalFileUtil.LOCAL_THUMBNAILS_FILE_DIRECTORY + objectId);
        if(originalImageDir.exists()&&thumbnailImageDir.exists()){
            deleteRecursive(originalImageDir);
            deleteRecursive(thumbnailImageDir);

        }else{
            // TODO: エラーハンドリング
        }
    }
// フォルダを再帰的に削除するヘルパーメソッド
    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        fileOrDirectory.delete(); // ファイルまたはディレクトリを削除
    }
}
