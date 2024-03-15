package com.app.nao.photorecon.ui.backup;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.app.nao.photorecon.R;
import com.app.nao.photorecon.model.repository.LocalFileUtil;
import com.app.nao.photorecon.model.usecase.CreateRealmBackup;
import com.app.nao.photorecon.model.usecase.DeletePhotoFromLocalFile;
import com.app.nao.photorecon.model.usecase.DeletePhotoFromRealm;
import com.app.nao.photorecon.ui.album.AlbumViewActivity;
import com.app.nao.photorecon.ui.util.DateManager;

import org.w3c.dom.Text;

import java.io.File;


// TODO: 非常に不安定なので，なにかしら見てあげること
public class BackupViewActivity extends AppCompatActivity{
    // 初回ログイン，もしくはセッション切れのときにこの画面に飛ぶ．
    private Button loginButton;
    private Button signUpButton;
    private EditText emailEditText;
    private EditText passEditText;

    // display progress
    private FrameLayout progressLayout;
    private ProgressBar progressBar;
    private TextView processText;

    // from viewmdel
    private BackupViewModel model;
    private CharSequence[] ArrayBackupNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_login);
        emailEditText = findViewById(R.id.emailEditText);
        passEditText = findViewById(R.id.passwordEditText);
        progressLayout = findViewById(R.id.progressDisplayFrameLayout);
        progressBar = findViewById(R.id.progressBar);
        processText = findViewById(R.id.progressTextView);

        // Create a ViewModel the first time the system calls an activity's onCreate() method.
        // Re-created activities receive the same MyViewModel instance created by the first activity.
        model = new ViewModelProvider(this).get(BackupViewModel.class);
        model.getBackupStateLv().observe(this, state -> {
            // TODO: UI側の更新はこちらに記載する．
            // 値が変わったときに呼び出される
            // TODO: if文で分岐していくこと．
            inActiveLoadingProgressLayout();
            switch (state.getProcessState()){

                case INITIATION:
                    Log.i("app","AWS lambdaリストを表示します．．");
                    activeLoadingProgressLayout("Activating...");
                    break;
                case LOGGING_IN:
                    Log.i("app","ログインしています．．");
                    activeLoadingProgressLayout("Login...");
                    break;
                case LOG_IN_SUCCESS_AND_GETTING_BACKUP_LIST:
                    Log.i("app","AWS lambdaをフックする準備ができました．");
                    activeLoadingProgressLayout("Getting List...");
                    // 次のlambdaListgetへ直接移行する．
                    model.getAWSBackupList();
                    break;
                case GET_BACKUP_NAME_LIST:
                    inActiveLoadingProgressLayout();
                    showBackupListNameDialog(state.getLambdaResponseBackupList());
                    break;
            }
        });

        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // loginが押された時
                // ログイン情報を送信: 適当なviewmodelからusecase起動
                //activeLoadingProgressbar(true);
                // AWSClient.saveEncryptedToken(v.getContext());
                model.pushSignInButton("","");
                activeLoadingProgressLayout("try Login ...");

            }
        });
        signUpButton = findViewById(R.id.createUserButton);
        signUpButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(v.getContext(), SignupViewActivity.class);
                // デバック用に1次利用する．
                // 権限チェック
                //JNIとの兼ね合いで，Realmが恐らく見えなくなっている．この潜在的な問題が解決するまで一旦放置するかもしれん
                // https://github.com/realm/realm-java/issues/7301
                if (ContextCompat.checkSelfPermission(
                        v.getContext(), Manifest.permission.MANAGE_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) v.getContext(), new String[]{Manifest.permission.MANAGE_EXTERNAL_STORAGE},1);
                }else{
                }
                //

                CreateRealmBackup createRealmBackup = new CreateRealmBackup();
                File f = v.getContext().getFilesDir();
                createRealmBackup.createRealmBackup(new File(f,LocalFileUtil.LOCAL_REALM_BACKUP_DIRECTORY)+"/backup");
                // v.getContext().startActivity(intent);
            }
        });
        // アクティビティのイニシエーションをする．
        //AWS側にチェックを実施
        // これはライフサイクルで頻繁に起きないようにしたほうがいいかもしれん
        // ログイン情報をチェックしています．
        activeLoadingProgressLayout("Ready to Access...");
        model.activityInitiation(this);

    }
    public void inActiveLoadingProgressLayout(){
        progressLayout.setVisibility(View.GONE);
    }
    public void activeLoadingProgressLayout(String message){
        progressLayout.setVisibility(View.VISIBLE);
        processText.setText(message);
    }

    public void showBackupListNameDialog(CharSequence[] arrayBackupName){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        CharSequence[] dialogCharSequence = new CharSequence[arrayBackupName.length +1];
        dialogCharSequence[0] = "現在の状態をバックアップする";
        for(int x=0;x< arrayBackupName.length;x++){
            dialogCharSequence[x+1] = arrayBackupName[x];
        }
        builder.setTitle("選択してください").setItems(dialogCharSequence, new DialogInterface.OnClickListener() {
        @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which ==0){

                    confirmUploadCloudStorageDialog();
                }else{
                    confirmDownloadCloudStorageDialog(dialogCharSequence[which]);
                }
            }
        });
        builder.create().show();
    }
    private boolean confirmUploadCloudStorageDialog(){
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setMessage(/*R.string.dialog_start_game*/
                "現在のStorageをアップロードします．\n" +
                "この操作には多量のバケット通信が発生します．")
            .setPositiveButton(/*R.string.start*/"Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    // URLをアップロード用の要求する
                    //model.exeS3BackupUpload();
                    // TODO:ファイルネーム．暫定的にここには，現在の日付時間を入れておく．
                    model.requestS3UploadURL(DateManager.getLocalDateFormatH().toString());
                }
            })
            .setNegativeButton(/*R.string.cancel*/ "No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                }
            });
        // TODO:戻り値いらないかも？
        builder.show();
        return true;
    }
    private void confirmDownloadCloudStorageDialog(CharSequence cs){
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setMessage(/*R.string.dialog_start_game*/
                cs+"をダウンロードします．\n" +
                    "現在のアルバムの状態はリセットされます．\n"+
                    "この操作には多量のバケット通信が発生します．")
            .setPositiveButton(/*R.string.start*/"Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    // model.exeS3BackupDownload(cs.toString());
                    // ダウンロード用のURLを要求する．
                    model.requestS3DownloadURL(cs.toString());
                }
            })
            .setNegativeButton(/*R.string.cancel*/ "No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                }
            });
        builder.show();
    }

}
// TODO: バックアップのアップロード
// TODO: バックアップのダウンロード
// TODO: サインアップ・メール認証