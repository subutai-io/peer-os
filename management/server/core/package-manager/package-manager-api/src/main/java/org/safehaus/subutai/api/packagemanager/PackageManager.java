package org.safehaus.subutai.api.packagemanager;

import java.util.Collection;

public interface PackageManager {

    Collection<PackageInfo> listPackages(String hostname);

    Collection<PackageInfo> getPackagesInfo(String hostname);

    Collection<PackageInfo> savePackagesInfo(String hostname);

    Collection<PackageInfo> deletePackagesInfo(String hostname);
}
