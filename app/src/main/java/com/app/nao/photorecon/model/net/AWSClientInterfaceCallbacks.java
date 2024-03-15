package com.app.nao.photorecon.model.net;

import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.client.results.SignInResult;

import ApiGatewayManager.model.LambdaResponceBackupList;

public interface AWSClientInterfaceCallbacks {
    Callback<UserStateDetails> awsInitiationCallback();
    Callback<SignInResult> tryLoginCallback();

    void reflectLambdaResponseToUI(LambdaResponceBackupList lambdaList);
    void acceptDownloadURL(String url);
    void acceptUploadURL(String url);
}