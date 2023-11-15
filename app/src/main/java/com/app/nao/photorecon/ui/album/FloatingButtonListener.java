package com.app.nao.photorecon.ui.album;

import static com.app.nao.photorecon.model.usecase.FilterPhotoBySegmentedName.filterPhotoBySegmentedName;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import com.app.nao.photorecon.R;
import com.app.nao.photorecon.model.entity.Photo;
import com.app.nao.photorecon.model.entity.SegmentedPhoto;
import com.app.nao.photorecon.model.usecase.FilterPhotoBySegmentedName;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class FloatingButtonListener implements View.OnClickListener {
    private Context context;
    private Pair<Long,Long> mDatePair;
    private MaterialDatePicker<Long> mDatePicker;
    private List<Photo> mPhotoList;

    FloatingButtonListener(Context context, List<Photo> photoList){
        this.context = context;
        this.mPhotoList = photoList;
    }
    @Override
    public void onClick(View v){
        // 検索windowの表示
        Dialog d = displayDeleteDialog(v);

    }
    Dialog displayDeleteDialog(View view) {

        // AssetManager assetManager = getResources().getAssets();
        AlertDialog.Builder builder = new AlertDialog.Builder((Activity) view.getContext());
        //TODO: ここstringファイルに記述
        builder.setTitle("検索");
        CharSequence[] options = {"日付", "物体"};
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        showDateSelectDialog();

                        break;
                    case 1:
                        searchByReconCategoryDialog();

                        break;
                }
            }
        });
        return builder.show();
    }
    private void showDateSelectDialog(){

        // MaterialDatePicker.Builder builder =
        //         MaterialDatePicker.Builder.dateRangePicker();
        // builder.setTheme(R.style.SubAppTheme);
        //MaterialDatePicker mDateRangePicker = builder.build();
        MaterialDatePicker mDateRangePicker = MaterialDatePicker.Builder
                .dateRangePicker()
                .setTheme(R.style.ThemeOverlay_MaterialComponents_MaterialCalendar_Fullscreen)
                .build();
        mDateRangePicker.addOnPositiveButtonClickListener(
                new MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>() {
            @Override
            public void onPositiveButtonClick(Pair<Long, Long> selection) {
                // updateDateRangeTextView(selection.first, selection.second);
                mDatePair = selection;
            }
        });

        mDateRangePicker.show(
                ((AppCompatActivity)context).getSupportFragmentManager(),
                mDateRangePicker.toString());
    }
    private void searchByReconCategoryDialog(){
        // TODO:assetのclasses.txtを呼ぶ方法を検討．たぶん今のままでいい...
        // アルバムを走査して検索リストを作成
        // Albumのintent呼び出し時にやる．ここでやることじゃない．
        Set<CharSequence> categorizedNameSet = new HashSet<>();
        for(Photo photo: mPhotoList){
            for(SegmentedPhoto sp:photo.getRecon_list()){
                categorizedNameSet.add(sp.getCategorization_name());
            }
        }
        //ここまで
        Set<CharSequence> selectedCategoryNameSet = new HashSet<>();
        final boolean[] checkedItems = new boolean[categorizedNameSet.size()];

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select Model");
        CharSequence[] options = categorizedNameSet.toArray(new CharSequence[0]);

        builder.setMultiChoiceItems(options, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichItem, boolean isChecked) {
                checkedItems[whichItem] = isChecked;
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // ユーザーがOKボタンをクリックしたときの処理
                for(int x=0;x<checkedItems.length;x++) {
                    if (checkedItems[x]){
                        selectedCategoryNameSet.add(options[x]);
                    }
                }
                List<Photo> resPhoto =
                        FilterPhotoBySegmentedName.filterPhotoBySegmentedName (mPhotoList, selectedCategoryNameSet);
                // TODO: resPhotoで更新したいがこれで大丈夫？．．．
                ((AlbumViewActivity)context).updateRecyclerView(resPhoto);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // ユーザーがキャンセルボタンをクリックしたときの処理

            }
        });
        builder.show();
        //
    }
}