package org.safehaus.subutai.core.packge.api;


import java.util.Collection;


public interface PackageManager {

    /**
     * Gets a list of packages on a given host.
     *
     * @return A collection of package information objects. If there are no packages on a host for some reason, an empty
     * collection is returned. Returns <code>null</code> if host name is invalid or is not reachable or any other error
     * occurs
     */
    Collection<PackageInfo> listPackages( String hostname );

    /**
     * Gets a list of packages matching a given pattern on a given host.
     *
     * @param namePattern package name pattern; normal shell wildcards are allowed
     *
     * @return A collection of package information objects matching the given pattern. If there are no matching packages
     * on a host, an empty collection is returned. Returns <code>null</code> if host name is invalid or is not reachable
     * or any other error occurs
     */
    Collection<PackageInfo> listPackages( String hostname, String namePattern );

    /**
     * Retrieves previously saved packages information on a given host.
     *
     * @return Previously saved collection of package information objects. <code>null</code> if no information was saved
     * before
     */
    Collection<PackageInfo> findPackagesInfo( String hostname );

    /**
     * Saves packages information on host.
     *
     * @return A collection of package information objects that is saved by this method. <code>null</code> if host name
     * is invalid or is not connected or any other error occurs
     */
    Collection<PackageInfo> savePackagesInfo( String hostname );

    /**
     * Deletes previously saved packages information on a given host.
     *
     * @return <code>true</code> if package information is successfully deleted, <code>false</code> if no information
     * was saved before
     */
    boolean deletePackagesInfo( String hostname );
}
