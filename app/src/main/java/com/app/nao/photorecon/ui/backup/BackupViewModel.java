package com.app.nao.photorecon.ui.backup;

import static androidx.lifecycle.SavedStateHandleSupport.createSavedStateHandle;
import static androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY;


import android.content.Context;
import android.util.Log;

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
        // ActivityのOncreateによって呼び出される．サブプロセスでやりたいが，contextが必要
        awsClient.initiateAwsTokens(context);
        // backupState.setValue();
    }
    // View から Viewmodel へ
    public void pushSignInButton(String username,String password) {
        BackupState bs = backupStateLv.getValue();
        // ここから直接lambdaにgetlistする．
        bs.setProcessState(BackupState.ProcessState.LOGGING_IN);
        backupStateLv.postValue(bs);
        awsClient.tryLogin("", "");
    }
    // これは，ログイン後，シーケンスで実行
    public void getAWSBackupList(){
        awsClient.hookApiGateway();
    }
    // アップロードS3URLリクエスト
    public void requestS3UploadURL(String fileName){
        // TODO: Stateを変更する
        BackupState bs = backupStateLv.getValue();
        // ここから直接lambdaにgetlistする．
        bs.setProcessState(BackupState.ProcessState.REQUESTING_UPLOAD_S3_URL);
        backupStateLv.postValue(bs);
        // リクエスト実行
        // TODO: ファイルネームどうする？
        awsClient.requestPushS3Url(fileName);
    }
    // アップロード実行．UIが無いので追加する．
    public void exeS3BackupUpload(){

    }
    // ダウンロードS3URLリクエスト
    public void requestS3DownloadURL(String fileName){
        // TODO: Stateを変更する
        BackupState bs = backupStateLv.getValue();
        // ここから直接lambdaにgetlistする．
        bs.setProcessState(BackupState.ProcessState.REQUESTING_DOWNLOAD_S3_URL);
        backupStateLv.postValue(bs);
        // リクエスト実行
        // TODO: ファイルネームどうする？
        awsClient.requestGetS3Url(fileName);
    }
    // ダウンロード実行時，ファイルの中身をダウンロード
    public void exeS3BackupDownload(){

    }
    // AWS初期化Oncreateで呼ぶ
    @Override
    public Callback<UserStateDetails> awsInitiationCallback() {
        return new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails result) {
                if (result.getUserState().equals(UserState.SIGNED_IN)||result.getUserState().equals(UserState.SIGNED_OUT)) {
                    //自動でフックしてくれるのでここでは相手にしない，ただし，トークンが生きている場合は，アクセス可能なので，一度投げておく．
                    // backupstatusがnullで戻って来る．初期化されていない？
                    BackupState bs = backupStateLv.getValue();
                    bs.setProcessState(BackupState.ProcessState.INITIATION_SUCCESS);
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

    // ログイン成功時の戻り関数．
    @Override
    public Callback<SignInResult> tryLoginCallback() {

        return new Callback<SignInResult>() {
            @Override
            public void onResult(SignInResult result) {
                if (result.getSignInState().equals(SignInState.DONE)) {
                    BackupState bs = backupStateLv.getValue();
                    // ここから直接lambdaにgetlistする．ログインだけ完了している状態というのは存在しない．
                    bs.setProcessState(BackupState.ProcessState.LOG_IN_SUCCESS_AND_GETTING_BACKUP_LIST);
                    backupStateLv.postValue(bs);
                    // NOTE: この次のgetListLambdaは，Viewで実行する
                } else {
                    // サインインでコケてる．
                    // TODO: LOGIN_FAILED
                }
            }
            @Override
            public void onError(Exception e) {
            }
        };
    }
    // lambda関数から戻り値があった
    @Override
    public void reflectLambdaResponseToUI(LambdaResponceBackupList response) {
        ArrayList<LambdaResponceBackupListItem> lambdaResponseBackupItems =new ArrayList<>(response);
        BackupState bs = backupStateLv.getValue();
        ArrayList<CharSequence> st =new ArrayList<>();
        for (LambdaResponceBackupListItem item: lambdaResponseBackupItems) {
            st.add(item.getKey());
        }
        bs.setLambdaResponseBackupList(st.toArray(new CharSequence[0]));
        bs.setProcessState(BackupState.ProcessState.GET_BACKUP_NAME_LIST);
        backupStateLv.postValue(bs);
    }
    // ダウンロードリンクが戻ったときにフックされる
    // url ... uploadLink
    @Override
    public void acceptDownloadURL(String url) {
        if(! url.isEmpty()){
            //Viewの表示を切り替える．
            BackupState bs = backupStateLv.getValue();
            bs.setProcessState(BackupState.ProcessState.BACKUP_DOWNLOADING);
            backupStateLv.postValue(bs);
            // TODO: AWSClientを使用してダウンロードしていく．進行状態は入れない．
            String res = awsClient.getBackupFromS3Url(url);
            // Viewの表示を切り替える．
        }else{
            Log.e("app","URLのロード時にエラーが発生しました．");
        }

    }
    // アップロードリンクが戻ったときにフックされる．
    @Override
    public void acceptUploadURL(String url) {
        // TODO: バックアップ用のファイルを作成する．
        // TODO: Viewmodelの表示を切り替える．
        // TODO: AWSClientを使用してダウンロードしていく．
    }

}




