package com.app.nao.photorecon.model.usecase;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.app.nao.photorecon.model.dao.RealmDAO;
import com.app.nao.photorecon.model.entity.Photo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;


public class SavePhoto extends RealmDAO<Photo> {
    private Photo mPhoto;
    // private String mSegmentFileDirectory;
    public void setPhoto(Photo photo){
        this.mPhoto = photo;
    }
    public SavePhoto(Photo photo){
        this.mPhoto = photo;
    }
    //ここで正式な宛先に保存する
    public void saveSegmentBitmapToDirectory(Context context, List<Bitmap> bitmaps) {
        String objectId = mPhoto.getId().toString();
        // こっちで行けるはず
        String mSegmentFileDirectory = mPhoto.getRecon_list_uri();
        // mFileDirectory = LocalFileUtil.LOCAL_FILE_DIRECTORY +  objectId + "/";
        File directory = new File(context.getFilesDir(), mSegmentFileDirectory);

        // ディレクトリが存在しない場合は作成
        if (!directory.exists()) {
            directory.mkdirs();
        }
        for(int i=0;i<bitmaps.size();i++) {
            Bitmap bitmap =bitmaps.get(i);
            Bitmap cropBitmap = resizeBitmap(cropToSquare(bitmap),200);
            // ファイルの保存パスを生成
            String fileName =Integer.toString(i)+".JPEG";
            File file = new File(directory, fileName);

            // セグメントした画像をBitmapをファイルに保存
            try {
                FileOutputStream fos = new FileOutputStream(file);
                cropBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
                Log.i("TEST","Data is saved:"+directory+"/"+fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    Bitmap cropToSquare(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int size = Math.min(width, height); // 短い辺のサイズを取得

        int x = (width - size) / 2;
        int y = (height - size) / 2;

        return Bitmap.createBitmap(bitmap, x, y, size, size);
    }
    Bitmap resizeBitmap(Bitmap bitmap, int squireSize){
        return Bitmap.createScaledBitmap(bitmap,squireSize,squireSize,true);

    }

    public void saveOriginalBitmapToDirectory(Context context, Bitmap bitmap){
        String objectId = mPhoto.getId().toString();
        String mOriginalFileDirectory = mPhoto.getSourceOriginalUri();
        // String mOriginalFileDirectory = LocalFileUtil.LOCAL_FILE_DIRECTORY +  objectId + "/original/";
        File directory = new File(context.getFilesDir(), mOriginalFileDirectory);

        // ディレクトリが存在しない場合は作成
        if (!directory.exists()) {
            directory.mkdirs();
        }
            // ファイルの保存パスを生成
        String fileName =objectId+".JPEG";
        File file = new File(directory, fileName);

            // セグメントした画像をBitmapをファイルに保存
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
        public void registerToRealm(){
            // if(photo.getRecon_list_uri()!=null) {
                // this.photo.setRecon_list_uri(mFileDirectory);
                super.update_entity(super.realmConf, this.mPhoto);
            // }else{
                //エラーハンドリング
            // }
        }

}
