package org.safehaus.subutai.impl.packagemanager.storage;

import java.util.Collection;
import org.safehaus.subutai.api.dbmanager.DbManager;
import org.safehaus.subutai.api.packagemanager.PackageInfo;
import org.safehaus.subutai.api.packagemanager.storage.PackageInfoStorage;

public class DbPackageInfoStorage implements PackageInfoStorage {

    private final DbManager dbManager;

    public DbPackageInfoStorage(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public Collection<PackageInfo> retrieve(String key) {
        PackageInfoCollection col = dbManager.getInfo(PackageInfo.SOURCE_NAME,
                key, PackageInfoCollection.class);
        return col != null ? col.getItems() : null;
    }

    @Override
    public boolean persist(String key, Collection<PackageInfo> packages) {
        PackageInfoCollection col = new PackageInfoCollection(packages);
        return dbManager.saveInfo(PackageInfo.SOURCE_NAME, key, col);
    }

    @Override
    public boolean delete(String key) {
        return dbManager.deleteInfo(PackageInfo.SOURCE_NAME, key);
    }

}
