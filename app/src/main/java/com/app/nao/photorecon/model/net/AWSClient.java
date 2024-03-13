package com.app.nao.photorecon.model.net;

import android.content.Context;
import android.util.Log;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.http.HttpClient;
import com.amazonaws.http.HttpMethodName;
import com.amazonaws.http.HttpResponse;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.results.Tokens;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.apigateway.ApiClientFactory;

import com.amazonaws.mobileconnectors.apigateway.ApiRequest;
import com.amazonaws.mobileconnectors.apigateway.ApiResponse;

import com.amazonaws.util.IOUtils;
import com.app.nao.photorecon.customapigateway.model.StorageControlURL;
import com.app.nao.photorecon.model.repository.AppSecretForDev;
import com.app.nao.photorecon.model.repository.LocalFileUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import ApiGatewayManager.PhotoreconprodClient;
import ApiGatewayManager.model.LambdaResponceBackupList;


// NOTE:各種トークンは，sharedPreferenceに持っていくのでこれはソースコード側で管理しない．
// NOTE:lambdaの戻り値はSDKの範囲外なので，こちらはきっちり管理していく．ModelはSDKに含まれているのでusecaseでモニタリングする．
// このクラスでやることはスレッド管理を正常にすること．後段のCallbackに関しては，interfaceを書くこと
public class AWSClient {
    // AWSClientから出さない
    private String userName = "";

    private String getUserName() {
        return userName;
    }

    private void setUserName(String str) {
        this.userName = str;
    }

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

    public void initiateAwsTokens(Context context) {
        awsMobileClient = AWSMobileClient.getInstance();
        _initiateAwsTokens(context);
    }

    // Cognito callback funcs
    private AWSClientInterfaceCallbacks callbackFunc;
    // S3 callback funcs

    public AWSClient(AWSClientInterfaceCallbacks callbacks) {
        this.callbackFunc = callbacks;
    }

    private void _initiateAwsTokens(Context app) {
        // TODO:シークレットから獲得

        int resourceId = app.getResources().getIdentifier("awsconfiguration", "raw", app.getPackageName());
        awsMobileClient.initialize(app, new AWSConfiguration(app, resourceId), callbackFunc.awsInitiationCallback());
    }

    public void tryLogin(String inputUserName, String inputPassword) {
        _tryLogin(inputUserName, inputPassword);
    }

    private void _tryLogin(String username, String password) {
        // 問答無用でデバック用のアカウントでログイン
        if (AppSecretForDev.isDev) {
            Log.i("app", "devモードでログインしています．");
            username = AppSecretForDev.CognitoUserIdTest;
            password = AppSecretForDev.CognitoPassTest;
        }
        setUserName(username);
        // TODO ログイン情報を読み込む．
        awsMobileClient.signIn(username, password, null, callbackFunc.tryLoginCallback());
    }

    public void hookApiGateway() {
        // TODO: 安全性的にイマイチなので，自分がサブスレッドかチェックする．
        new Thread(new Runnable() {
            @Override
            public void run() {
                LambdaResponceBackupList lm = _hookApi();
                if (lm != null) {
                    callbackFunc.reflectLambdaResponseToUI(lm);
                } else {
                    Log.e("app", "トークンが無効です．");
                }
            }
        }).start();
    }

    private LambdaResponceBackupList _hookApi() {
        Tokens tokens;
        try {
            tokens = awsMobileClient.getTokens();
            if (awsMobileClient.getTokens().getIdToken() == null) {
                // TODO: ログイン状態キャッシュがカラになっている．
                return null;
            } else {
                // TODO: このトークンでログインを試みる．
            }
        } catch (java.lang.Exception e) {
            // TODO:lambdaでunauthorizedが戻ってくる場合，ここから先落ちるので注意．ここでハンドリングすること.
            Log.e("app", e.toString());
            return null;
        }
        //
        Map<String, String> parameters = new HashMap<>();
        parameters.put("username", getUserName());

        final PhotoreconprodClient client = new ApiClientFactory().build(PhotoreconprodClient.class);
        ApiRequest localRequest =
                new ApiRequest(client.getClass().getSimpleName())
                        .withPath("/user/backup/list")
                        .withHttpMethod(HttpMethodName.valueOf("GET"))
                        // .withHeaders(headers)
                        // .addHeader("Content-Type", "application/json")
                        .addHeader("Authorization", "Bearer " + tokens.getIdToken().getTokenString());  //Use JWT token
        ApiResponse response = client.execute(localRequest);
        LambdaResponceBackupList lmResList;
        try {
            String jsonString = convertInputStreamToString(response.getContent());
            Gson gson = new GsonBuilder().create();
            lmResList = gson.fromJson(jsonString, LambdaResponceBackupList.class);
            Log.i("", "");
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

    /* Use the JDK HttpClient (since v11) class to do the download. */
    /* Use the JDK HttpURLConnection (since v1.1) class to do the download. */
    public String getBackupFromS3Url(String presignedUrlString) {

        try {
            // set URL
            URL presignedUrl = new URL(presignedUrlString);
            HttpURLConnection connection = (HttpURLConnection) presignedUrl.openConnection();
            connection.setRequestMethod("GET");

            try(InputStream content = connection.getInputStream();
                FileOutputStream fileOutputStream = new FileOutputStream(LocalFileUtil.LOCAL_REALM_BACKUP_DIRECTORY+"backup.realm");
                ) {
                IOUtils.copy(content, fileOutputStream);
            }

            return "SUCCESS";
        } catch (IOException e) {
            return "FAIL";
        }
    }
    public String pushBackupToS3Url(String presignedUrlString,String backupFileName){
        try {
            // file setting
            File file = new File(LocalFileUtil.LOCAL_REALM_BACKUP_DIRECTORY+backupFileName);
            FileInputStream inputStream = new FileInputStream(file);
            byte[] seg = IOUtils.toByteArray(inputStream);

            // URL setting
            URL presignedUrl = new URL(presignedUrlString);
            HttpURLConnection connection = (HttpURLConnection) presignedUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/octet-stream");

            // try push
            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(seg);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Backup successfully pushed to S3
            } else {
                // Handle unsuccessful response
                return null;
                // return "FAIL WITH RESPONSE:"+ responseCode;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "SUCCESS";
    }
    // 同じことをしてるので，抽象クラス化しても良いかも？
    private final static String REQUEST_API_BACKUP_GET_PATH = "/user/backup/download/url";
    private final static String REQUEST_API_BACKUP_PUSH_PATH = "user/backup/upload/url";
    //アップロードリンクをリクエスト
    public String requestPushS3Url(String fileName){
        // トークン拾い
        Tokens tokens;
        try {
            tokens = awsMobileClient.getTokens();
            if (awsMobileClient.getTokens().getIdToken() == null) {
                // TODO: ログイン状態キャッシュがカラになっている．
                return null;
            } else {
                // TODO: このトークンでログインを試みる．
            }
        } catch (java.lang.Exception e) {
            // TODO:lambdaでunauthorizedが戻ってくる場合，ここから先落ちるので注意．ここでハンドリングすること.
            Log.e("app", e.toString());
            return null;
        }
        //
        Map<String, String> parameters = new HashMap<>();
        // 'cognito:[parameter]':という名前で入る
        parameters.put("filename", fileName);
        // コピペ
        final  PhotoreconprodClient client= new ApiClientFactory().build(PhotoreconprodClient.class);
        ApiRequest localRequest =
                new ApiRequest(client.getClass().getSimpleName())
                        .withPath(REQUEST_API_BACKUP_PUSH_PATH)
                        .withHttpMethod(HttpMethodName.valueOf("GET"))
                        // .withHeaders(headers)
                        // .addHeader("Content-Type", "application/json")
                        .addHeader("Authorization", "Bearer " + tokens.getIdToken().getTokenString());  //Use JWT token
        ApiResponse response = client.execute(localRequest);

        StorageControlURL upload_url;
        try {
            String jsonString = convertInputStreamToString(response.getContent());
            Gson gson = new GsonBuilder().create();
            upload_url = gson.fromJson(jsonString, StorageControlURL.class);
            Log.i("", "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return upload_url.getPresignedUrl();
    }
    // ダウンロードリンクをリクエスト
    public String requestGetS3Url(String fileName){
        Tokens tokens;
        try {
            tokens = awsMobileClient.getTokens();
            if (awsMobileClient.getTokens().getIdToken() == null) {
                // TODO: ログイン状態キャッシュがカラになっている．
                return null;
            } else {
                // TODO: このトークンでログインを試みる．
            }
        } catch (java.lang.Exception e) {
            // TODO:lambdaでunauthorizedが戻ってくる場合，ここから先落ちるので注意．ここでハンドリングすること.
            Log.e("app", e.toString());
            return null;
        }
        // hookApi
        Map<String, String> parameters = new HashMap<>();
        // 'cognito:[parameter]':という名前で入る
        parameters.put("filename", fileName);
        // コピペ
        final PhotoreconprodClient client = new ApiClientFactory().build(PhotoreconprodClient.class);
        ApiRequest localRequest =
                new ApiRequest(client.getClass().getSimpleName())
                        .withPath(REQUEST_API_BACKUP_GET_PATH)
                        .withHttpMethod(HttpMethodName.valueOf("POST"))
                        // .withHeaders(headers)
                        // .addHeader("Content-Type", "application/json")
                        .addHeader("Authorization", "Bearer " + tokens.getIdToken().getTokenString());  //Use JWT token
        ApiResponse response = client.execute(localRequest);

        StorageControlURL download_url;
        try {
            String jsonString = convertInputStreamToString(response.getContent());
            Gson gson = new GsonBuilder().create();
            download_url = gson.fromJson(jsonString, StorageControlURL.class);
            Log.i("", "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return download_url.getPresignedUrl();
    }
}