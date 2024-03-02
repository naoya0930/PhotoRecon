package com.app.nao.photorecon.model.net;

import android.content.Context;
import android.util.Log;

import com.amazonaws.http.HttpMethodName;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.results.Tokens;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.apigateway.ApiClientFactory;

import com.amazonaws.mobileconnectors.apigateway.ApiRequest;
import com.amazonaws.mobileconnectors.apigateway.ApiResponse;
import com.app.nao.photorecon.model.repository.AppSecretForDev;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import ApiGatewayManager.PhotoreconprodClient;
import ApiGatewayManager.model.LambdaResponceBackupList;


// NOTE:各種トークンは，sharedPreferenceに持っていくのでこれはソースコード側で管理しない．
// NOTE:lambdaの戻り値はSDKの範囲外なので，こちらはきっちり管理していく．ModelはSDKに含まれているのでusecaseでモニタリングする．
// このクラスでやることはスレッド管理を正常にすること．後段のCallbackに関しては，interfaceを書くこと
public class AWSClient {

    public void test_thread(Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                initiateAwsTokens(context);
            }
        }).start();
    }
    // TODO:ここstaticにしたほうが気分がいい．なんでだろうか．
    private static AWSMobileClient awsMobileClient;
    public void initiateAwsTokens(Context context){
        awsMobileClient = AWSMobileClient.getInstance();
        _initiateAwsTokens(context);
    }
    // callback funcs
    private AWSClientInterfaceCallbacks callbackFunc;
    public AWSClient(AWSClientInterfaceCallbacks callbacks){
        this.callbackFunc = callbacks;
    }

    private void _initiateAwsTokens(Context app) {
        // TODO:シークレットから獲得

        int resourceId = app.getResources().getIdentifier("awsconfiguration", "raw", app.getPackageName());
        awsMobileClient.initialize(app, new AWSConfiguration(app, resourceId),callbackFunc.awsInitiationCallback());
    }
    public void tryLogin(String inputUserName,String inputPassword){
        _tryLogin(inputUserName,inputPassword);
    }
    private void _tryLogin(String username,String password){
        // 問答無用でデバック用のアカウントでログイン
        if(AppSecretForDev.isDev){
            Log.i("app","devモードでログインしています．");
            username = AppSecretForDev.CognitoUserIdTest;
            password = AppSecretForDev.CognitoPassTest;
        }
        // TODO ログイン情報を読み込む．
        awsMobileClient.signIn(username, password, null, callbackFunc.tryLoginCallback());
    }
    public void hookApiGateway() {
        // TODO: 安全性的にイマイチなので，自分がサブスレッドかチェックする．
        new Thread(new Runnable() {
            @Override
            public void run() {
                LambdaResponceBackupList lm = _hookApi();
                if(lm!=null){
                    callbackFunc.reflectLambdaResponseToUI(lm);
                }else{
                    Log.e("app","トークンが無効です．");
                }
            }
        }).start();
    }

    private LambdaResponceBackupList _hookApi() {
        Tokens tokens;
        try{
            tokens = awsMobileClient.getTokens();
            if(awsMobileClient.getTokens().getIdToken()== null){
                // TODO: ログイン状態キャッシュがカラになっている．
                return null;
            }else{
                // TODO: このトークンでログインを試みる．
            }
        }catch (java.lang.Exception e){
            // TODO:lambdaでunauthorizedが戻ってくる場合，ここから先落ちるので注意．ここでハンドリングすること.
            Log.e("app",e.toString());
            return null;
        }
        //
        final PhotoreconprodClient client = new ApiClientFactory().build(PhotoreconprodClient.class);
        ApiRequest localRequest =
                new ApiRequest(client.getClass().getSimpleName())
                        .withPath("/user/backup/list")
                        .withHttpMethod(HttpMethodName.valueOf("GET"))
                        // .withHeaders(headers)
                        // .addHeader("Content-Type", "application/json")
                        .addHeader("Authorization", "Bearer "+tokens.getIdToken().getTokenString());  //Use JWT token
                        // .withParameters(parameters);
        // デフォルトではpostで投げるようになっている．
        ApiResponse response= client.execute(localRequest);
        LambdaResponceBackupList lmResList;
        try {
            String jsonString = convertInputStreamToString(response.getContent());
            Gson gson = new GsonBuilder().create();
            lmResList = gson.fromJson(jsonString, LambdaResponceBackupList.class);

            Log.i("","");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return lmResList;
    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }
        return stringBuilder.toString();
    }
}: