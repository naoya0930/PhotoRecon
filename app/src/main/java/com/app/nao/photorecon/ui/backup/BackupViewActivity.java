package com.app.nao.photorecon.ui.backup;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.app.nao.photorecon.R;
import com.app.nao.photorecon.model.net.AWSClient;

public class BackupViewActivity extends AppCompatActivity{
    // 初回ログイン，もしくはセッション切れのときにこの画面に飛ぶ．
    private Button loginButton;
    private Button signUpButton;
    private EditText emailText;
    private ProgressBar progressBar;
    private CharSequence[] ArrayBackupNames;
    private EditText passText;

    // TODO: ここで宣言したらダメなんだっけ？結局Activityのライフサイクルに依存するようになる？

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_login);

        progressBar = findViewById(R.id.progressBar);

        // Create a ViewModel the first time the system calls an activity's onCreate() method.
        // Re-created activities receive the same MyViewModel instance created by the first activity.
        BackupViewModel model = new ViewModelProvider(this).get(BackupViewModel.class);
        model.getBackupStateLv().observe(this, state -> {
            // TODO: UI側の更新はこちらに記載する．
            // 値が変わったときに呼び出される
            // TODO: if文で分岐していくこと．
            activeLoadingProgressbar(false);
            switch (state.getProcessState()){

                case PROCESSED:
                    Log.i("app","AWS lambdaリストを表示します．．");
                    showBackupListNameDialog(state.getLambdaResponseBackupList());
                    break;
                case LOGGING_IN:
                    Log.i("app","AWS configrationの設定とpreferenceの確認ができました．");
                    break;
                case HAVE_ACTIVE_TOKEN:
                    Log.i("app","AWS lambdaをフックする準備ができました．");
                    // model.getAWSBackupList();
                    break;
                case LOGOUT_WITH_TOKEN:
                    break;
                case LAMBDA_CHALLENGING:
                    break;
                case LOGOUT_WITH_NO_TOKEN:
                    // model.activityInitiation(this);
                    break;


            }
        });

        emailText = findViewById(R.id.emailEditText);
        passText = findViewById(R.id.passwordEditText);

        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // loginが押された時
                // ログイン情報を送信: 適当なviewmodelからusecase起動
                //activeLoadingProgressbar(true);
                // AWSClient.saveEncryptedToken(v.getContext());
                model.pushSignInButton("","");
                activeLoadingProgressbar(true);

            }
        });
        signUpButton = findViewById(R.id.createUserButton);
        signUpButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                model.getAWSBackupList();
            }
        });
        // アクティビティのイニシエーションをする．
        //AWS側にチェックを実施
        // これはライフサイクルで頻繁に起きないようにしたほうがいいかもしれん
        // ログイン情報をチェックしています．
        activeLoadingProgressbar(true);
        model.activityInitiation(this);

    }

    public void activeLoadingProgressbar(boolean b){
            progressBar.setActivated(b);
    }
    public void showBackupListNameDialog(CharSequence[] arrayBackupName){
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("選択してください").setItems(arrayBackupName, new DialogInterface.OnClickListener() {
        @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create().show();
    }

}
