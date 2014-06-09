package org.safehaus.subutai.api.packagemanager.storage;

import java.util.Collection;
import org.safehaus.subutai.api.packagemanager.PackageInfo;

public interface PackageInfoStorage {

    public Collection<PackageInfo> retrieve(String key);

    public boolean persist(String key, Collection<PackageInfo> packages);

    public boolean delete(String key);
}
