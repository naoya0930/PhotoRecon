package com.app.nao.photorecon.ui.main;

import static com.app.nao.photorecon.model.net.AWSClientByUserpool.test_thread;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
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
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import com.app.nao.photorecon.R;
import com.app.nao.photorecon.model.entity.Photo;
import com.app.nao.photorecon.model.entity.SegmentedClass;
import com.app.nao.photorecon.model.net.AWSClient;
import com.app.nao.photorecon.model.net.AWSClientByUserpool;
import com.app.nao.photorecon.model.usecase.ResultToEntities;
import com.app.nao.photorecon.model.usecase.SnapRectanglePhoto;
import com.app.nao.photorecon.ui.backup.BackupViewActivity;
import com.app.nao.photorecon.ui.license.LicenseViewActivity;
import com.app.nao.photorecon.ui.util.AssetFileExplorer;


public class MainActivity extends AppCompatActivity implements Runnable {
    private ImageView mImageView;
    // Viewに対して直接的すぎる変更で良くないで修正する
    public void setImageViewBitmap(Bitmap bitmap){
        mImageView.setImageBitmap(bitmap);
    }
    public ImageView getImageView(){
        return mImageView;
    }
    private ResultView mResultView;
    public void setResultViewState(int state){
        mResultView.setVisibility(state);
    }
    private Button mRegisterButton;
    public void setResisterButtonState(boolean b){
        mRegisterButton.setEnabled(b);
    }
    private ProgressBar mProgressBar;

    public void setProgressBarInvisible(int status){
        mProgressBar.setVisibility(status);
    }
    private Bitmap mBitmap = null;
    public void setBitmap(Bitmap bitmap){this.mBitmap = bitmap;}
    public Bitmap getBitmap(){return mBitmap;}
    // state
    private Module mModule = null;
    private SegmentedClass mSegmentedClass;
    private Photo mPhoto;
    public Photo getPhoto(){
        return mPhoto;
    }
    // Activities or services
    private ResultToEntities resultToEntities;
    private SnapRectanglePhoto snapRectanglePhoto;
    private ArrayList<Bitmap> mPreSegmentedThumbnails;
    public ArrayList<Bitmap> getPreSegmentedThumbnails(){
        return mPreSegmentedThumbnails;
    }
    // private SavePhoto mSavePhoto;
    private Uri mSelectedImageUri;
    public void setSelectedImageUri(Uri selectedImageUri){
        this.mSelectedImageUri = selectedImageUri;
    }
    public Uri getSelectedImageUri(){
        return mSelectedImageUri;
    }
    private CharSequence mPtlFileName = "yolov5s.torchscript.ptl";
    public void setPtlFileName(CharSequence str){
        mPtlFileName = str;
    }
    public CharSequence getPtlFileName(){return mPtlFileName;}
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

    private Button buttonResisterPhoto;
    private Button buttonSelectPhoto;
    private Button buttonActiveAlbum;
    private Button referenceDialogButton;
    private Button buttonBackup;
    private Button buttonSelectModel;

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
        // AWS test
        //test_thread(this);

        setContentView(R.layout.activity_main);

        mImageView = findViewById(R.id.imageView);
        mImageView.setImageResource(R.drawable.noimage_24);
        //mImageView.setImageBitmap(mBitmap);
        mResultView = findViewById(R.id.resultView);
        mResultView.setVisibility(View.INVISIBLE);

        mProgressBar = findViewById(R.id.progressBar);
        mRegisterButton = findViewById(R.id.registerPhotoButton);
        mRegisterButton.setEnabled(false);

        resultToEntities = new ResultToEntities();
        snapRectanglePhoto = new SnapRectanglePhoto();

        // referenceDialog
        referenceDialogButton = findViewById(R.id.referenceDialogButton);
        referenceDialogButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final Intent intent = new Intent(v.getContext(), LicenseViewActivity.class);
                startActivity(intent);
            }
        });
        //Album Intent
        buttonActiveAlbum = findViewById(R.id.activeAlbumButton);
        buttonActiveAlbum.setOnClickListener(new ActiveAlbumButtonHandler(this));

        // select Photo Intent
        buttonSelectPhoto = findViewById(R.id.selectPhotoButton);
        buttonSelectPhoto.setOnClickListener(new SelectPhotoButtonHandler(this,activityResultLauncher));

        // Resister Button
        buttonResisterPhoto = findViewById(R.id.registerPhotoButton);
        buttonResisterPhoto.setOnClickListener(new ResisterPhotoButtonHandler(this));

        buttonBackup = findViewById(R.id.backupButton);
        buttonBackup.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final Intent intent = new Intent(v.getContext(), BackupViewActivity.class);
                startActivity(intent);
            }
        });
        buttonSelectModel = findViewById(R.id.selectModelButton);
        buttonSelectModel.setOnClickListener(new SelectModelButtonHandler(this));

        //Moduleの初期化
        this.loadModel();
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
    // TODO: これの処理を決める．Viewmodelでもないような..?usecaseっぽい
    public void loadModel(){
        try {
            mModule = LiteModuleLoader.load(AssetFileExplorer.assetFilePath(getApplicationContext(), mPtlFileName.toString()));
            //TODO:  assetFileとやり取りしているので，切り出してusecaseに置く
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
    // TODO:これをActivityに置いていていいのか検討．ライフサイクルの一環だし問題ないか？
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
        // mPhoto.setSaved_at(DateManager.getLocalDate());
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
