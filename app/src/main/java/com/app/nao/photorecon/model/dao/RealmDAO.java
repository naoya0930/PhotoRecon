package com.app.nao.photorecon.model.dao;


import android.util.Log;

import org.bson.types.ObjectId;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.annotations.PrimaryKey;

// データベース（主にSQLite）にクエリを投げてレコードを取得し、エンティティに変換するクラス。
// もちろんその逆（データベースへのWrite）も行います。

public abstract class RealmDAO<T extends RealmObject>{
    protected RealmConfiguration inmemoryRealmConf =
        new RealmConfiguration.Builder()
            .name("PhotoReconApp.realm")
            .allowQueriesOnUiThread(true)
            .allowWritesOnUiThread(true)
            .compactOnLaunch()
            .inMemory()   //インメモリ実行すると，closeで破棄する．
            .build();
    protected RealmConfiguration realmConf =
        new RealmConfiguration.Builder()
            .name("PhotoReconApp.realm")
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
    protected List<T> read_all_entity(RealmConfiguration conf,Class<T> type){
        Realm.setDefaultConfiguration(conf);
        Realm realm_instance = Realm.getDefaultInstance();
        // RealmQuery<T> searchTaskQuery = realm_instance.where(T.class);
        List<T> result = new ArrayList<T>();
        realm_instance.executeTransaction(r -> {
            RealmResults<T> res = r.where(type).findAll();
            result.addAll(r.copyFromRealm(res));
        });
        realm_instance.close();
        return  result;
        // Listに戻すが，後段の処理が遅い場合は，realmListの使用を検討
//        List<T> rtv = new ArrayList<T>();
//        rtv.addAll(result.subList(0, result.size()));
//        return rtv;
    }
    protected void update_entity(RealmConfiguration conf, T obj){
        Realm.setDefaultConfiguration(conf);
        Realm realm_instance = Realm.getDefaultInstance();
        realm_instance.executeTransaction(r -> {
            RealmObject res = r.copyToRealmOrUpdate(obj);
        });
       realm_instance.close();
    }
    protected T deleteEntityByStringPrimaryKey(RealmConfiguration conf,ObjectId key,Class<T> type){
        Realm.setDefaultConfiguration(conf);
        Realm realm_instance = Realm.getDefaultInstance();
        List<T> rl= new ArrayList<T>();
        T t = findByPrimaryStringKey(conf,key,type);
        realm_instance.executeTransaction(r->{
            rl.add(t);
            t.deleteFromRealm();
        });
        realm_instance.close();
        return rl.get(0);
    }
    //ObjectId型はobjectで受けられない
    protected T findByPrimaryStringKey(RealmConfiguration conf,ObjectId key,Class<T> type) {
        Realm.setDefaultConfiguration(conf);
        Realm realm_instance = Realm.getDefaultInstance();
        List<T> res = new ArrayList<>();
        Field primaryKeyField = getPrimaryKeyField(type);
        Type primaryKeyType = primaryKeyField.getType();

        realm_instance.executeTransaction(r->{
            // TODO:ここキャストしてたらプライマリキーを動的に見つけた意味がないのでなんとか考えてみる．
            T t =r.where(type).equalTo(primaryKeyField.getName(),(ObjectId) key ).findFirst();
            res.add(t);
        });
        realm_instance.close();
        return res.get(0);
    }
    private Field getPrimaryKeyField(Class<T> type) {
        Field primaryKeyField = null;
        // Tクラスのフィールドを取得
        Field[] fields = type.getDeclaredFields();
        // 主キーを探す
        for (Field field : fields) {
            if (field.isAnnotationPresent(PrimaryKey.class)) {
                primaryKeyField = field;
                break;
            }
        }
        if (primaryKeyField == null) {
            throw new IllegalArgumentException("Primary key field not found in class " + type.getName());
        }
        return primaryKeyField;
    }
}
