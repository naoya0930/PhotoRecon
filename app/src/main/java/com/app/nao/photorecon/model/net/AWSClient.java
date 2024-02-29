package com.app.nao.photorecon.model.net;

import android.content.Context;
import android.util.Log;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.results.Tokens;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.apigateway.ApiClientFactory;

import com.app.nao.photorecon.model.repository.AppSecretForDev;

import ApiGatewayManager.PhotoreconprodClient;
import ApiGatewayManager.model.LambdaResponceBackupList;


// NOTE:各種トークンは，sharedPreferenceに持っていくのでこれはソースコード側で管理しない．
// NOTE:lambdaの戻り値はSDKの範囲外なので，こちらはきっちり管理していく．ModelはSDKに含まれているのでusecaseでモニタリングする．
// このクラスでやることはスレッド管理を正常にすること．interfaceを書くこと
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
//            new Callback<UserStateDetails>() {
//            @Override
//            public void onResult(UserStateDetails details) {
//                Log.d("app", "AWS ClientInitiation Success"+ details.getUserState().toString());
//                // ここケースわけが必要？SIGND_INという状態がよーわからん
//
//                tryLogin();
//            }
//
//            @Override
//            public void onError(Exception e) {
//                Log.d("AWS ClientInitiation", e.toString());
//            }
//        });
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
        awsMobileClient.signIn(username, password, null, callbackFunc.tryLoginCallback()
//
//                new Callback<SignInResult>() {
//            @Override
//            public void onResult(final SignInResult signInResult) {
//                Log.d("TAG", "Sign-in callback state: " + signInResult.getSignInState());
//                        switch (signInResult.getSignInState()) {
//                            case DONE:
//
//                                hookApiGateway();
//                                break;
//                            case NEW_PASSWORD_REQUIRED:
//                                // パスワードの再設定画面に案内
//                                Log.e("app", "このアカウントは現在サーバ側で使用できないようになっています");
//                                break;
//                            default:
//                                Log.e("app", "このアプリケーションは現在サーバ側で使用できないようになっています");
//                                break;
//                        }
//            }
//            @Override
//            public void onError(Exception e) {
//                Log.e("token", "  ");
//            }
//        }
    );
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
        ApiClientFactory factory = new ApiClientFactory().credentialsProvider(new AWSCredentialsProvider() {
            @Override
            public AWSCredentials getCredentials() {
                return new AWSCredentials() {
                    @Override
                    public String getAWSAccessKeyId() {
                        return tokens.getIdToken().toString();
                    }

                    @Override
                    public String getAWSSecretKey() {
                        return "";
                    }
                };
            }

            @Override
            public void refresh() {
            }
        });
        final PhotoreconprodClient client = factory.build(PhotoreconprodClient.class);
        // NOTE:ここ認証できていないとエラーで落ちる．
        //com.amazonaws.mobileconnectors.apigateway.ApiClientException: Cognito Identity not configured
        final LambdaResponceBackupList lambdaBodyResponce = client.userBackupListGet();
        return  lambdaBodyResponce;
    }
}