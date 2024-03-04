package com.app.nao.photorecon.ui.main;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.MediaStore;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

public class SelectPhotoButtonHandler implements View.OnClickListener{
    Context context;
    ActivityResultLauncher<Intent> activityResultLauncher;
    SelectPhotoButtonHandler(Context context, ActivityResultLauncher<Intent> activityResultLauncher){
        this.context = context;
        this.activityResultLauncher = activityResultLauncher;

    }
    @Override
    public void onClick(View v) {
        //写真を選択させる．intent起動
        if(checkReadMediaImagePermission()) {
            Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            activityResultLauncher.launch(pickPhoto);
            //結果をresultviewに表示
        }else{
        }

    }
    private boolean checkReadMediaImagePermission(){
        if (ContextCompat.checkSelfPermission(
                (Activity)context, Manifest.permission.READ_MEDIA_IMAGES) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_MEDIA_IMAGES},1);
        }else{
            return true;
        }
        return false;
    }
//    private final ActivityResultLauncher<Intent> activityResultLauncher =
//            ((FragmentActivity)context).registerForActivityResult(
//                    new ActivityResultContracts.StartActivityForResult(),
//                    new  PredictionActivityResultHandler());

}
