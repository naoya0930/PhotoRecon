package com.app.nao.photorecon.ui.main;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SelectModelButtonHandler implements View.OnClickListener{
    private Context context;
    //private ResultView mResultView;
    private MainActivity mMainActivity;
    SelectModelButtonHandler(Context context){
        this.context = context;
        this.mMainActivity = (MainActivity) context;
    }
    public void onClick(View v) {
        mMainActivity.setResultViewState(View.INVISIBLE);
        CharSequence ptlFileName = mMainActivity.getPtlFileName();
        // mResultView.setVisibility(View.INVISIBLE);
        AssetManager assetManager = context.getResources().getAssets();
        String[] assetList =null;
        try {
            assetList = assetManager.list("");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Set<CharSequence> modelFileList =new HashSet<>();
        for (String modelName : assetList)
            if (modelName.endsWith(".ptl"))
                modelFileList.add(modelName);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select Model");
        // CharSequence[] options = modelFileList.toArray(new CharSequence[modelFileList.size()]);
        CharSequence[] options = modelFileList.toArray(new CharSequence[0]);
        int indexNum =0;
        for(int x=0 ;x< options.length;x++){
            if(options[x].equals(ptlFileName)){
                indexNum=x;
                break;
            }
        }
        builder.setSingleChoiceItems(options, indexNum, new DialogInterface.OnClickListener() {
            //TODO: 重いのでProgressBarを表示する．
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                mMainActivity.setPtlFileName(options[whichButton]);
                mMainActivity.loadModel();
                dialog.dismiss();
            }
        });
        builder.show();
    }
}
