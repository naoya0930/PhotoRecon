package com.app.nao.photorecon.ui.backup;

import static androidx.lifecycle.SavedStateHandleSupport.createSavedStateHandle;
import static androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY;


import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.viewmodel.ViewModelInitializer;

import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserState;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.client.results.SignInResult;
import com.amazonaws.mobile.client.results.SignInState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.app.nao.photorecon.CustomApplication;
import com.app.nao.photorecon.model.net.AWSClient;
import com.app.nao.photorecon.model.net.AWSClientInterfaceCallbacks;

import java.util.ArrayList;

import ApiGatewayManager.model.LambdaResponceBackupList;
import ApiGatewayManager.model.LambdaResponceBackupListItem;

// AWS Cognitoの認証が入ってくるとcallbackが多発するので，流石に非同期処理を管理しきれなくなった．
// そこで，認証関連のstateの管理として部分的に使用することにする．
// usecaseへはのすべてここから呼ぶこと
public class BackupViewModel extends ViewModel implements AWSClientInterfaceCallbacks{

    // TODO: アクティビティが死んでも変数の状態は維持したいので，もう少し上位レイヤーに書く

    // MutableLiveDataを使用して状態の変更を通知
    // 新規新スタンスの作成と併せて初期値を作成．
    private final MutableLiveData<BackupState> backupStateLv = new MutableLiveData<>(new BackupState());
    public LiveData<BackupState> getBackupStateLv() {
        return backupStateLv;
    }

    // これはLiveデータではない
    // interfaceの引き渡し
    private AWSClient awsClient = new AWSClient(this);
    //
    //context渡したくないがやむなし
    public void activityInitiation(Context context) {
        // ここでAWSにアクセスしたり，プロセスの進行を管理する．
        awsClient.initiateAwsTokens(context);
        // backupState.setValue();
    }
    //ちょっとここに書くか微妙だが，プロセス管理上にいるのでここで扱う．
    public void pushSignInButton(String username,String password) {
        awsClient.tryLogin("", "");
    }
    public void getAWSBackupList(){
        awsClient.hookApiGateway();
    }
    public void exeS3BackupUpload(){

    }
    public void exeS3BackupDownload(){

    }
    @Override
    public Callback<UserStateDetails> awsInitiationCallback() {
        return new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails result) {
                if (result.getUserState().equals(UserState.SIGNED_IN)||result.getUserState().equals(UserState.SIGNED_OUT)) {
                    //自動でフックしてくれるのでここでは相手にしない，ただし，トークンが生きている場合は，アクセス可能なので，一度投げておく．
                    // backupstatusがnullで戻って来る．初期化されていない？
                    BackupState bs = backupStateLv.getValue();
                    bs.setProcessState(BackupState.ProcessState.BACKUP_UPLOADING);
                    backupStateLv.postValue(bs);
                    //TODO: トークンがnullの場合にログイン画面に案内
                    // これをやると自動でログインできるようになる．
                    // awsClient.hookApiGateway();
                } else {
                    // ログインでコケてる．
                }
            }

            @Override
            public void onError(Exception e) {

            }
        };
    }

    @Override
    public Callback<SignInResult> tryLoginCallback() {

        return new Callback<SignInResult>() {
            @Override
            public void onResult(SignInResult result) {
                if (result.getSignInState().equals(SignInState.DONE)) {
                    BackupState bs = backupStateLv.getValue();
                    bs.setProcessState(BackupState.ProcessState.GETTING_BACKUP_LIST);
                    backupStateLv.postValue(bs);

                } else {
                    // サインインでコケてる．
                }
            }
            @Override
            public void onError(Exception e) {

            }
        };
    }
    @Override
    public void reflectLambdaResponseToUI(LambdaResponceBackupList response) {
        ArrayList<LambdaResponceBackupListItem> lambdaResponseBackupItems =new ArrayList<>(response);
        BackupState bs = backupStateLv.getValue();
        ArrayList<CharSequence> st =new ArrayList<>();
        for (LambdaResponceBackupListItem item: lambdaResponseBackupItems) {
            st.add(item.getKey());
        }
        bs.setLambdaResponseBackupList(st.toArray(new CharSequence[0]));
        bs.setProcessState(BackupState.ProcessState.PROCESSED);
        backupStateLv.postValue(bs);
    }
}




