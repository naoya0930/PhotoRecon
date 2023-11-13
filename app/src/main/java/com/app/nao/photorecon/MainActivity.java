package com.app.nao.photorecon;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;

import com.app.nao.photorecon.model.entity.Photo;
import com.app.nao.photorecon.model.entity.SegmentedClass;
import com.app.nao.photorecon.model.usecase.ResultToEntities;
import com.app.nao.photorecon.model.usecase.SaveBitmapToDataDirectory;
import com.app.nao.photorecon.model.usecase.SavePhoto;
import com.app.nao.photorecon.model.usecase.SnapRectanglePhoto;
import com.app.nao.photorecon.ui.album.AlbumViewActivity;
import com.app.nao.photorecon.ui.util.DateManager;

public class MainActivity extends AppCompatActivity implements Runnable {
    private int mImageIndex = 0;
    private String[] mTestImages = {"test1.png", "test2.jpg", "test3.png"};
    private ImageView mImageView;
    private ResultView mResultView;
    private Button mRegisterButton;
    private ProgressBar mProgressBar;
    private Bitmap mBitmap = null;
    private Module mModule = null;
    private SegmentedClass mSegmentedClass;
    private Photo mPhoto;
    // Activities or services
    private ResultToEntities resultToEntities;
    private SnapRectanglePhoto snapRectanglePhoto;
    private SaveBitmapToDataDirectory saveBitmapToDataDirectory;
    private ArrayList<Bitmap> mPreSegmentedThumbnails;
    private SavePhoto mSavePhoto;
    //
    private Uri mSelectedImageUri;
    private CharSequence mPtlFileName = "yolov5s.torchscript.ptl";
    private float mImgScaleX, mImgScaleY, mIvScaleX, mIvScaleY, mStartX, mStartY;


    public static String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }
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

        try {
            mBitmap = BitmapFactory.decodeStream(getAssets().open(mTestImages[mImageIndex]));
        } catch (IOException e) {
            Log.e("Object Detection", "Error reading assets", e);
            finish();
        }

        mImageView = findViewById(R.id.imageView);
        mImageView.setImageBitmap(mBitmap);
        mResultView = findViewById(R.id.resultView);
        mResultView.setVisibility(View.INVISIBLE);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mRegisterButton = findViewById(R.id.registerPhotoButton);
        mRegisterButton.setEnabled(false);

        // Realm test setting
        Realm.init(this); // context, usually an Activity or Application
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("default-realm")
                .allowQueriesOnUiThread(true)
                .allowWritesOnUiThread(true)
                .compactOnLaunch()
                .inMemory()   //インメモリ実行すると，closeで破棄する．
                .build();
        Realm.setDefaultConfiguration(config);
        Realm realm_instance = Realm.getDefaultInstance();
        Log.v("EXAMPLE","Successfully opened the default realm at: " + realm_instance.getPath());
        realm_instance.close();

        // set Activites/service
        resultToEntities = new ResultToEntities();
        snapRectanglePhoto = new SnapRectanglePhoto();
        saveBitmapToDataDirectory = new SaveBitmapToDataDirectory();

        // @string: 写真を取る
        final Button buttonLive = findViewById(R.id.activeCameraButton);
        buttonLive.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //オリジナルの写真アプリに各種追加．
            }
        });
        final Button buttonActiveAlbum = findViewById(R.id.activeAlbumButton);
        buttonActiveAlbum.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final Intent intent = new Intent(MainActivity.this, AlbumViewActivity.class);
                startActivity(intent);
            }
        });
        final Button buttonSelectPhoto = findViewById(R.id.selectPhotoButton);
        buttonSelectPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //写真を選択させる．intent起動
                if(checkReadMediaImagePermission()) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(pickPhoto);
                    //結果をresultviewに表示
                }else{

                }

            }
        });

        final Button buttonResisterPhoto = findViewById(R.id.registerPhotoButton);
        buttonResisterPhoto.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //ローカルファイルに保存する
                //日付をセットする．
                mPhoto.setSaved_at(DateManager.getLocalDate());
                // saveBitmapToDataDirectory.saveSegmentBitmapToDirectory(v.getContext(), mPreSegmentedThumbnails);
                // saveBitmapToDataDirectory.saveOriginalBitmapToDiarectory(v.getContext(),mBitmap);
                // resultの情報をもとにrealmに情報を登録する．
                mSavePhoto = new SavePhoto(mPhoto);
                mSavePhoto.saveSegmentBitmapToDirectory(v.getContext(),mPreSegmentedThumbnails);
                mSavePhoto.saveOriginalBitmapToDirectory(v.getContext(),mBitmap);
                mSavePhoto.registerToRealm();
                mRegisterButton.setEnabled(false);

                // TODO: 成功，失敗の結果をイベント通知
                // Log.i("TEST","mPreSegmentedThumbnails:  "+ mPreSegmentedThumbnails.size());
                // Log.i("TEST","mPhoto_modelname:  "+mPhoto.getModel_name());
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
            mModule = LiteModuleLoader.load(MainActivity.assetFilePath(getApplicationContext(), mPtlFileName.toString()));
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
    // Notice: Do not execute this function every frame because it takes time to execute.
    private void updateModel(){
        try {
            mModule = LiteModuleLoader.load(MainActivity.assetFilePath(getApplicationContext(), mPtlFileName.toString()));
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
    private final ActivityResultLauncher<Intent> activityResultLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            activityResult -> {
                    // requestCode, resultCode, data
                if(activityResult.getResultCode() !=RESULT_CANCELED) {
                    if (activityResult.getResultCode() == Activity.RESULT_OK) {
                        if (activityResult.getData() != null) {
                            //結果を受け取った後の処理
                            mSelectedImageUri = activityResult.getData().getData();
                            String[] filePathColumn = {MediaStore.Images.Media.DATA};
                            if (mSelectedImageUri != null) {
                                Cursor cursor = getContentResolver().query(mSelectedImageUri,
                                        filePathColumn, null, null, null);
                                if (cursor != null) {
                                    //画像の配置

                                    cursor.moveToFirst();
                                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                    String picturePath = cursor.getString(columnIndex);
                                    mBitmap = BitmapFactory.decodeFile(picturePath);
                                    Matrix matrix = new Matrix();
                                    matrix.postRotate(0.0f);
                                    mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
                                    mImageView.setImageBitmap(mBitmap);
                                    cursor.close();
                                    // 推論開始
                                    // Mresultviewarraylistが結果を持ってる．
                                    // mButtonDetect.setEnabled(false);
                                    mProgressBar.setVisibility(ProgressBar.VISIBLE);
                                    //mButtonDetect.setText(getString(R.string.run_model));
                                    mImgScaleX = (float)mBitmap.getWidth() / PrePostProcessor.mInputWidth;
                                    mImgScaleY = (float)mBitmap.getHeight() / PrePostProcessor.mInputHeight;

                                    mIvScaleX = (mBitmap.getWidth() > mBitmap.getHeight() ? (float)mImageView.getWidth() / mBitmap.getWidth() : (float)mImageView.getHeight() / mBitmap.getHeight());
                                    mIvScaleY  = (mBitmap.getHeight() > mBitmap.getWidth() ? (float)mImageView.getHeight() / mBitmap.getHeight() : (float)mImageView.getWidth() / mBitmap.getWidth());

                                    mStartX = (mImageView.getWidth() - mIvScaleX * mBitmap.getWidth())/2;
                                    mStartY = (mImageView.getHeight() -  mIvScaleY * mBitmap.getHeight())/2;

                                    Thread thread = new Thread(MainActivity.this);
                                    thread.start();
                                }
                            }
                        } else {

                        }
                    }
                }
            });

    @Override
    public void run() {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(mBitmap, PrePostProcessor.mInputWidth, PrePostProcessor.mInputHeight, true);
        final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(resizedBitmap, PrePostProcessor.NO_MEAN_RGB, PrePostProcessor.NO_STD_RGB);
        // IValue[] outputTuple = mModule.forward(IValue.from(inputTensor)).toTuple(); torch.nn.Module
        IValue[] outputTuple = mModule.runMethod("forward",IValue.from(inputTensor)).toTuple();
        
        // This logcast is invalid, it was used to test a method included in torchscript.
        // Log.i("torch_log",""+mModule.runMethod("test_sample",IValue.from(2)).toString());
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
