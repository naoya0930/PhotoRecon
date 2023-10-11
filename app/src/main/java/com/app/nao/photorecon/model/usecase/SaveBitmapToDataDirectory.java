package com.app.nao.photorecon.model.usecase;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SaveBitmapToDataDirectory {
    private  void saveBitmapToDataDirectory(Context context, Bitmap bitmap, String fileName) {
        // ファイルを保存するディレクトリを指定
        File directory = new File(context.getFilesDir(), "app_data/thumbnails");

        // ディレクトリが存在しない場合は作成
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // ファイルの保存パスを生成
        File file = new File(directory, fileName);

        // Bitmapをファイルに保存
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
