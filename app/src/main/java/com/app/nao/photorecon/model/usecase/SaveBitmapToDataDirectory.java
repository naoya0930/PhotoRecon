package com.app.nao.photorecon.model.usecase;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SaveBitmapToDataDirectory {
    public void saveBitmapToDataDirectory(Context context, List<Bitmap> bitmaps) {
        // ファイルを保存するディレクトリを指定，現在の画面に出ているキャッシュとして扱いたい
        File directory = new File(context.getFilesDir(), "tmp/app_data/thumbnails/");

        // ディレクトリが存在しない場合は作成
        if (!directory.exists()) {
            directory.mkdirs();
        }
        for(int i=0;i<bitmaps.size();i++) {
            Bitmap bitmap =bitmaps.get(i);
            // ファイルの保存パスを生成
            String fileName =Integer.toString(i)+".JPEG";
            File file = new File(directory, fileName);

            // Bitmapをファイルに保存
            try {

                FileOutputStream fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
                Log.i("TEST","Data is saved:"+directory+"/"+fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
