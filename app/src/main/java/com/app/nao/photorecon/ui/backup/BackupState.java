package com.app.nao.photorecon.ui.backup;

import java.util.Date;

import ApiGatewayManager.model.LambdaResponceBackupList;

public class BackupState {
    public BackupState(BackupState b){
        this.processState = getProcessState();
        this.lambdaResponseBackupList = getLambdaResponseBackupList();
    }
    public BackupState(){
        this.processState = ProcessState.LOGOUT_WITH_NO_TOKEN;
        this.lambdaResponseBackupList = new CharSequence[0];
    }
    public enum ProcessState{
        LOGOUT_WITH_NO_TOKEN,
        LOGOUT_WITH_TOKEN,
        LOGGING_IN,
        HAVE_ACTIVE_TOKEN,
        LAMBDA_CHALLENGING,
        PROCESSED
    }
    // ここに変更を検知したい変数を置く．
    // トークンの有効期限はAWSSDK側で管理
    // TODO: 外部定義クラスだけど変更検知できるか確認
    private CharSequence[] lambdaResponseBackupList = {};
    public CharSequence[] getLambdaResponseBackupList() {
        return lambdaResponseBackupList;
    }
    public void setLambdaResponseBackupList(CharSequence[] l){
        this.lambdaResponseBackupList =l;
    }
    // lambdaResponceが更新されなかったときのためのモニタリング
    private ProcessState processState= ProcessState.LOGOUT_WITH_NO_TOKEN;
    public ProcessState getProcessState() {
        return processState;
    }
    public void setProcessState(ProcessState processState) {
        this.processState = processState;
    }
}
