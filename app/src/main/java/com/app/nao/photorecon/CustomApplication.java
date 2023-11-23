package com.app.nao.photorecon;

import android.app.Application;

import com.app.nao.photorecon.ui.global.GlobalState;

import io.realm.Realm;

//最初期起動に宣言される
public class CustomApplication extends Application {
    // デバイス構成が変更されたとき
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        // アプリケーションの初期化処理をここに記述する
        // 例: ライブラリの初期化、共通の設定など
    }
    public GlobalState getMyRepository(){
        return new GlobalState();
    }
    // 他の親クラスのメソッドをオーバーライドすることもできる
    // 例: onTerminate(), onLowMemory(), onTrimMemory() など
}
