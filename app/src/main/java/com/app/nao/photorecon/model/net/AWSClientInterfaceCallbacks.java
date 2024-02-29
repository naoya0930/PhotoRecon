package com.app.nao.photorecon.model.net;

import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.client.results.SignInResult;

import ApiGatewayManager.model.LambdaResponceBackupList;

public interface AWSClientInterfaceCallbacks {
    Callback<UserStateDetails> awsInitiationCallback();
    Callback<SignInResult> tryLoginCallback();

    void reflectLambdaResponseToUI(LambdaResponceBackupList lambdaList);
}
//    private Callback<UserStateDetails>() awsInitiationCallback= new Callback<SignInResult>(){
//    {
//            @Override
//            public void onResult(UserStateDetails details) {
//            }
//
//            @Override
//            public void onError(Exception e) {
//            }
//    };
//    Callback<SignInResult> tryLogninCallback = new Callback<SignInResult>(){
//            @Override
//            public void onResult(final SignInResult signInResult) {}
//            }
//            @Override
//            public void onError(Exception e) {
//            }
//    };
    //   public void hookApiGateway() {
        // TODO: 安全性的にイマイチなので，自分がサブスレッドかチェックする．
        //ここは自分で書く必要あり．
// //}}