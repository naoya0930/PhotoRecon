package com.app.nao.photorecon.ui.backup;

import java.util.Date;

import ApiGatewayManager.model.LambdaResponceBackupList;

public class BackupState {
    public BackupState(BackupState b){
        this.processState = getProcessState();
        this.lambdaResponseBackupList = getLambdaResponseBackupList();
    }
    public BackupState(){
        this.processState = ProcessState.INITIATION;
        this.lambdaResponseBackupList = new CharSequence[0];
    }
    // フロントだけでなく，Viewmodelで処理する状態も考えて，各実行状態のStateを置く．
    //Viewmodel と Modelの間は，Interfaceで，
    //ViewとViewmnodelの間は，関数として呼び出す．
    public enum ProcessState{
        INITIATION,
        INITIATION_SUCCESS,
        LOGGING_IN,
        LOG_IN_SUCCESS_AND_GETTING_BACKUP_LIST,
        GET_BACKUP_NAME_LIST,
        REQUESTING_DOWNLOAD_S3_URL,
        REQUESTING_UPLOAD_S3_URL,
        BACKUP_UPLOADING,
        BACKUP_DOWNLOADING,
        PROCESS_SUCCESSED
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
    private ProcessState processState= ProcessState.INITIATION;
    public ProcessState getProcessState() {
        return processState;
    }
    public void setProcessState(ProcessState processState) {
        this.processState = processState;
    }
}
