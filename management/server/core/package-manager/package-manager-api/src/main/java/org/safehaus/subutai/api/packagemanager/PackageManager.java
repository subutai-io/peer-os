package org.safehaus.subutai.api.packagemanager;

import java.util.Collection;

public interface PackageManager {

    /**
     * Gets a list of packages on a given host.
     *
     * @param hostname
     * @return A collection of package information objects. If there are no
     * packages on a host for some reason, an empty collection is returned.
     * Returns <code>Null</code> if host name is invalid or is not reachable or
     * any other error occurs
     */
    Collection<PackageInfo> listPackages(String hostname);

    /**
     * Retrieves previously saved packages information. Host name is used as a
     * key for DB querying.
     *
     * @param hostname
     * @return Previously saved collection of package information objects.
     * <code>Null</code> if no information was saved before
     */
    Collection<PackageInfo> findPackagesInfo(String hostname);

    /**
     * Saves packages information on host.
     *
     * @param hostname
     * @return A collection of package information objects that is saved by this
     * method. <code>Null</code> if host name is invalid or is not connected or
     * any other error occurs
     */
    Collection<PackageInfo> savePackagesInfo(String hostname);

    /**
     * Deletes previously saved packages information. Host name is used as key
     * for DB querying.
     *
     * @param hostname
     * @return <code>true</code> if package information is successfully deleted,
     * <code>false</code> if no information was saved before
     */
    boolean deletePackagesInfo(String hostname);
}
