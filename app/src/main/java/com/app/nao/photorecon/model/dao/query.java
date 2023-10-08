package com.app.nao.photorecon.dao;


import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;

// データベース（主にSQLite）にクエリを投げてレコードを取得し、エンティティに変換するクラス。
// もちろんその逆（データベースへのWrite）も行います。

abstract class query<T extends RealmObject>{
    //インメモリへの転送と，DBファイルへの反映も書きたい

    protected RealmConfiguration InmemoryRealmConf =
        new RealmConfiguration.Builder()
            .name("PhotoReconApp")
            .allowQueriesOnUiThread(true)
            .allowWritesOnUiThread(true)
            .compactOnLaunch()
            .inMemory()   //インメモリ実行すると，closeで破棄する．
            .build();
    protected RealmConfiguration RealmConf =
        new RealmConfiguration.Builder()
            .name("PhotoReconApp")
            .allowQueriesOnUiThread(true)
            .allowWritesOnUiThread(true)
            .compactOnLaunch()
            .build();

    protected void create_entity(RealmConfiguration conf,T obj){
        Realm.setDefaultConfiguration(conf);
        Realm realm_instance = Realm.getDefaultInstance();
        realm_instance.executeTransaction(r -> {
            r.copyToRealm(obj);
        });
        realm_instance.close();

    }
    // RealmQueryを実行する．
    protected List<T> read_entity(RealmConfiguration conf,RealmQuery q){
        Realm.setDefaultConfiguration(conf);
        Realm realm_instance = Realm.getDefaultInstance();
        // RealmQuery<T> searchTaskQuery = realm_instance.where(T.class);
        RealmResults<T> result = q.findAll();
        realm_instance.close();
        // Listに戻すが，後段の処理が遅い場合は，realmListの使用を検討
        List<T> rtv = new ArrayList<T>();
        rtv.addAll(result.subList(0, result.size()));
        return rtv;
    }
    private void update_entity(RealmConfiguration conf, T obj){
        Realm.setDefaultConfiguration(conf);
        Realm realm_instance = Realm.getDefaultInstance();
        realm_instance.executeTransaction(r -> {
            r.copyToRealmOrUpdate(obj);
        });
       realm_instance.close();
    }
    private void delete_entity(RealmConfiguration conf,RealmQuery q){
        Realm.setDefaultConfiguration(conf);
        Realm realm_instance = Realm.getDefaultInstance();
        realm_instance.executeTransaction(r->{
            RealmResults<T> list = q.findAll();
            for(T t:list){
                // 遅い気がする．もう少しいい実装がありそう
                t.deleteFromRealm();
            }
        });
        realm_instance.close();
    }


}
