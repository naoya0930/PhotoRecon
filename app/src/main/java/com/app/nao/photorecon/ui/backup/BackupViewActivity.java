package com.app.nao.photorecon.ui.backup;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.app.nao.photorecon.R;
import com.app.nao.photorecon.ui.album.AlbumViewActivity;

import org.w3c.dom.Text;



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
    private CharSequence[] ArrayBackupNames;

    // TODO: ここで宣言したらダメなんだっけ？結局Activityのライフサイクルに依存するようになる？

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
        BackupViewModel model = new ViewModelProvider(this).get(BackupViewModel.class);
        model.getBackupStateLv().observe(this, state -> {
            // TODO: UI側の更新はこちらに記載する．
            // 値が変わったときに呼び出される
            // TODO: if文で分岐していくこと．
            inActiveLoadingProgressLayout();
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
                    model.getAWSBackupList();
                    activeLoadingProgressLayout("Getting List...");
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
                v.getContext().startActivity(intent);
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
        builder.setTitle("選択してください").setItems(arrayBackupName, new DialogInterface.OnClickListener() {
        @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create().show();
    }

}
// TODO: バックアップのアップロード
// TODO: バックアップのダウンロード
// TODO: サインアップ・メール認証