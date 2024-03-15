package com.app.nao.photorecon.model.usecase;

import com.app.nao.photorecon.model.dao.RealmMigrater;

import java.io.File;

public class CreateRealmBackup extends RealmMigrater {
    public String createRealmBackup(File f ){
        return super.makeBackupRealm(f);
    }
    public String createRealmBackup(String pass ){
        return super.makeBackupRealm(new File(pass));
    }

}
