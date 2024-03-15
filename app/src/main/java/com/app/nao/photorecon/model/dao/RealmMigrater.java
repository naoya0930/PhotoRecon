package com.app.nao.photorecon.model.dao;

import java.io.File;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class RealmMigrater extends RealmDAO{
    protected String makeBackupRealm(File destination){
        Realm.setDefaultConfiguration(super.realmConf);
        Realm realm_instance = Realm.getDefaultInstance();
        // String realmpath = realm_instance.getPath();
        // > /data/user/0/com.app.naoPhotoRecon/files/PhotoReconApp.realm
        // トランザクション中で実行しないとだめかも



        //io.realm.exceptions.RealmFileException:
        // Unable to open a realm at path
        // '/data/user/0/com.app.naoPhotoRecon/files/app_data/realms':
        // open() failed: Is a directory Path:
        // /data/user/0/com.app.naoPhotoRecon/files/app_data/realms
        //
        // Exception backtrace:
        //<backtrace not supported on this platform>.
        // (open("/data/user/0/com.app.naoPhotoRecon/files/app_data/realms") failed:
        // Is a directory Path: /data/user/0/com.app.naoPhotoRecon/files/app_data/realms
        //Exception backtrace:
        //<backtrace not supported on this platform>) (/data/user/0/com.app.naoPhotoRecon/files/app_data/realms)
        // in /tmp/realm-java/realm/realm-library/src/main/cpp/io_realm_internal_OsSharedRealm.cpp line 404 Kind: ACCESS_ERROR.
        if (!destination.exists()) {
            destination.mkdirs();
        }
        realm_instance.executeTransaction(r-> {
            r.writeCopyTo(destination);
        });
        return "SUCCESS";
    }
    protected String setBackupRealm(String fileName){
        // TODO: 方針として，realmの名前をリプレイスする
        return "";
    }
}
