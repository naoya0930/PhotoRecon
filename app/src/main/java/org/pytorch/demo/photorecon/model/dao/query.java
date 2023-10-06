package org.pytorch.demo.photorecon.model.dao;


import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;

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
        // トランザクション中に書く!
        realm_instance.copyToRealm(obj);
        realm_instance.close();

    }

    protected List<T> read_all_entity(RealmConfiguration conf){
        Realm.setDefaultConfiguration(conf);
        Realm realm_instance = Realm.getDefaultInstance();
//
//        realm_instance.copyToRealm(obj);
//        realm_instance.close();
    }
    private void Update_entity(){
//        Realm realm_instance = Realm.getDefaultInstance();
//
//        realm_instance.copytoRealmOrUpdate(obj);
//        realm_instance.close();
    }
    private void Delete_entity(){}


}
