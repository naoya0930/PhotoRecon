package com.app.nao.photorecon.ui.main;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import org.pytorch.IValue;
import org.pytorch.LiteModuleLoader;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;

import com.app.nao.photorecon.R;
import com.app.nao.photorecon.model.entity.Photo;
import com.app.nao.photorecon.model.entity.SegmentedClass;
import com.app.nao.photorecon.model.usecase.ResultToEntities;
import com.app.nao.photorecon.model.usecase.SaveBitmapToDataDirectory;
import com.app.nao.photorecon.model.usecase.SavePhoto;
import com.app.nao.photorecon.model.usecase.SnapRectanglePhoto;
import com.app.nao.photorecon.ui.album.AlbumViewActivity;
import com.app.nao.photorecon.ui.util.AssetFileExplorer;
import com.app.nao.photorecon.ui.util.DateManager;


public class MainActivity extends AppCompatActivity implements Runnable {
    // view
    private ImageView mImageView;
    // Viewに対して直接的すぎる変更で良くないで修正する
    public void setImageViewBitmap(Bitmap bitmap){
        mImageView.setImageBitmap(bitmap);
    }
    public ImageView getImageView(){
        return mImageView;
    }
    private ResultView mResultView;
    private Button mRegisterButton;
    private ProgressBar mProgressBar;

    public void setProgressBarInvisible(int status){
        mResultView.setVisibility(status);
    }
    private Bitmap mBitmap = null;
    public void setBitmap(Bitmap bitmap){this.mBitmap = bitmap;}
    public Bitmap getBitmap(){return mBitmap;}
    // state
    private Module mModule = null;
    private SegmentedClass mSegmentedClass;
    private Photo mPhoto;
    // Activities or services
    private ResultToEntities resultToEntities;
    private SnapRectanglePhoto snapRectanglePhoto;
    private ArrayList<Bitmap> mPreSegmentedThumbnails;
    private SavePhoto mSavePhoto;
    private Uri mSelectedImageUri;
    public void setSelectedImageUri(Uri selectedImageUri){
        this.mSelectedImageUri = selectedImageUri;
    }
    public Uri getSelectedImageUri(){
        return mSelectedImageUri;
    }
    private CharSequence mPtlFileName = "yolov5s.torchscript.ptl";
// TODO:これをActivityに書いてあるのは違和感があるので，直したい．
    private float mImgScaleX, mImgScaleY, mIvScaleX, mIvScaleY, mStartX, mStartY;

    public void setImageRatio(float imageScaleX, float imageScaleY, float ivScaleX, float ivScaleY, float startX, float startY) {
        this.mImgScaleX = imageScaleX;
        this.mImgScaleY = imageScaleY;
        this.mIvScaleX = ivScaleX;
        this.mIvScaleY = ivScaleY;
        this.mStartX = startX;
        this.mStartY = startY;
    }
    // ratio Original by yolo size
    // 呼び出しごとにメソッド追加

    private Button buttonResisterPhoto;

    // TODO:ここ以下２つのメソッドをまとめる
    protected boolean checkManageExtraStoragePermission(){
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.MANAGE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.MANAGE_EXTERNAL_STORAGE},1);
        }else{
            return true;
        }
        return false;
    }
    protected boolean checkReadMediaImagePermission(){
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_MEDIA_IMAGES) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES},1);
        }else{
            return true;
        }
        return false;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO:android 31以降は無視されるので注意
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }
        // android 33より新しい場合はメソッド側で毎回確かめるようにする．
        // OnCreateで権限チェックはできなくなっている．

        setContentView(R.layout.activity_main);

        mImageView = findViewById(R.id.imageView);
        mImageView.setImageResource(R.drawable.noimage_24);
        //mImageView.setImageBitmap(mBitmap);
        mResultView = findViewById(R.id.resultView);
        mResultView.setVisibility(View.INVISIBLE);

        mProgressBar = findViewById(R.id.progressBar);
        mRegisterButton = findViewById(R.id.registerPhotoButton);
        mRegisterButton.setEnabled(false);

        Realm.init(this); // context, usually an Activity or Application
        resultToEntities = new ResultToEntities();
        snapRectanglePhoto = new SnapRectanglePhoto();

        // referenceDialog
        final Button referenceDialogButton = findViewById(R.id.referenceDialogButton);
        referenceDialogButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

            }
        });
        //Album Intent
        final Button buttonActiveAlbum = findViewById(R.id.activeAlbumButton);
        buttonActiveAlbum.setOnClickListener(new ActiveAlbumButtonHandler(this));

        // select Photo Intent
        final Button buttonSelectPhoto = findViewById(R.id.selectPhotoButton);
        buttonSelectPhoto.setOnClickListener(new SelectPhotoButtonHandler(this,activityResultLauncher));


        buttonResisterPhoto = findViewById(R.id.registerPhotoButton);
        buttonResisterPhoto.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //ローカルファイルに保存する
                //日付をセットする．
                mPhoto.setSaved_at(DateManager.getLocalDate());
                // resultの情報をもとにrealmに情報を登録する．
                mSavePhoto = new SavePhoto(mPhoto);
                mSavePhoto.saveSegmentBitmapToDirectory(v.getContext(),mPreSegmentedThumbnails);
                mSavePhoto.saveOriginalBitmapToDirectory(v.getContext(),mBitmap);
                mSavePhoto.registerToRealm();
                mRegisterButton.setEnabled(false);

                // TODO: 成功，失敗の結果をイベント通知
                mSelectedImageUri = null;
            }
        });
        final Button buttonBackup = findViewById(R.id.backupButton);
        buttonBackup.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

            }
        });

        final Button buttonSelectModel = findViewById(R.id.selectModelButton);
        buttonSelectModel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mResultView.setVisibility(View.INVISIBLE);
                AssetManager assetManager = getResources().getAssets();
                String[] assetList =null;
                try {
                    assetList = assetManager.list("");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                List<CharSequence> modelFileList =new ArrayList<>();
                for (String modelName : assetList)
                    if (modelName.endsWith(".ptl"))
                        modelFileList.add(modelName);

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Select Model");
                CharSequence[] options = modelFileList.toArray(new CharSequence[modelFileList.size()]);
                builder.setSingleChoiceItems(options, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mPtlFileName = options[whichButton];
                        updateModel();
                    }
                });
                builder.show();
            }
        });
        //初期化
        try {
            mModule = LiteModuleLoader.load(AssetFileExplorer.assetFilePath(getApplicationContext(), mPtlFileName.toString()));
            BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open("classes.txt")));
            String line;
            List<String> classes = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                classes.add(line);
            }
            PrePostProcessor.mClasses = new String[classes.size()];
            classes.toArray(PrePostProcessor.mClasses);
            mSegmentedClass = new SegmentedClass(mPtlFileName.toString(),classes);
        } catch (IOException e) {
            Log.e("Object Detection", "Error reading assets", e);
            finish();
        }
    }
    @Override
    protected void onRestart(){
        super.onRestart();
        //各種初期化
        mImageView.setImageResource(R.drawable.noimage_24);
        mResultView.setVisibility(View.INVISIBLE);
        buttonResisterPhoto.setEnabled(false);

    }
    // Notice: Do not execute this function every frame because it takes time to execute.
    private void updateModel(){
        try {
            mModule = LiteModuleLoader.load(AssetFileExplorer.assetFilePath(getApplicationContext(), mPtlFileName.toString()));
            BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open("classes.txt")));
            String line;
            List<String> classes = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                classes.add(line);
            }
            PrePostProcessor.mClasses = new String[classes.size()];
            classes.toArray(PrePostProcessor.mClasses);
            mSegmentedClass = new SegmentedClass(mPtlFileName.toString(),classes);
        } catch (IOException e) {
            Log.e("Object Detection", "Error reading assets", e);
            finish();
        }
    }
    final private ActivityResultLauncher<Intent> activityResultLauncher =
            registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new PredictionActivityResultHandler(this));

    @Override
    public void run() {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(mBitmap, PrePostProcessor.mInputWidth, PrePostProcessor.mInputHeight, true);
        final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(resizedBitmap, PrePostProcessor.NO_MEAN_RGB, PrePostProcessor.NO_STD_RGB);
        IValue[] outputTuple = mModule.runMethod("forward",IValue.from(inputTensor)).toTuple();

        final Tensor outputTensor = outputTuple[0].toTensor();
        final float[] outputs = outputTensor.getDataAsFloatArray();
        // TODO:imageVIewへの適応をここでやってしまっている．要修正
        final ArrayList<Result> results =  PrePostProcessor.outputsToNMSPredictions(outputs, mImgScaleX, mImgScaleY, mIvScaleX, mIvScaleY, mStartX, mStartY);
        // サムネイルを作成する．//メモリにおいておくには，最大サイズがでかいので対処する
        // 元の画像 mBitmap 短い方に圧縮
        // Image View 1080*1080
        // resized bitmap 640*640
        // 元の画像との比率
        // mImgScaleY...3000/640
        // mImgScaleX...4000/640
        // UI上のイメージviewと画像の比率
        // mIvScaleY=0.27
        // mIvScaleX=0.27
        // mStartX=0.0
        // mStartY=134.99997
        // ImageView用に整形されていたものをもとに戻す作業
        mPreSegmentedThumbnails =  new ArrayList<Bitmap>(
                snapRectanglePhoto.makeSegmentedImages(
                        mIvScaleX,mIvScaleY,
                        mStartX,mStartY,
                        mBitmap, results));
        List<Result> savedResult = new ArrayList<Result>(
                snapRectanglePhoto.makeSegmentedRectangle(
                        mIvScaleX,mIvScaleY,
                        mStartX,mStartY,
                        results));
        // まだ保存しない
        // mPhotoに突っ込む
        // NOTE:ここで値がバグってる
        mPhoto = resultToEntities.resultToPhoto(
                new ArrayList<>(savedResult),
                mSelectedImageUri.toString(),
                //URIでパースすると勝手に絶対パスに変換しよる（多分）
                mSegmentedClass,
                mPtlFileName.toString()
        );

        runOnUiThread(() -> {
            mProgressBar.setVisibility(ProgressBar.INVISIBLE);
            mResultView.setResults(results);
            mResultView.invalidate();
            mResultView.setVisibility(View.VISIBLE);
            mRegisterButton.setEnabled(true);
        });
    }
}
