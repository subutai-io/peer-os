package org.safehaus.subutai.core.repository.api;


import java.util.Set;


/**
 * Allows to manage apt repository packages
 */
public interface RepositoryManager
{
    /**
     * Adds package to repository so that it becomes available to all resource hosts and container hosts for
     * installation
     *
     * @param pathToPackage - absolute path to package
     *
     * @throws RepositoryException - thrown if some error occurs during addition
     */
    public void addPackageByPath( String pathToPackage ) throws RepositoryException;

    /**
     * Removes package from repository
     *
     * @param packageName - name of package
     *
     * @throws RepositoryException -  thrown if some error occurs during removal
     */
    public void removePackageByName( String packageName ) throws RepositoryException;

    /**
     * Extracts package to /tmp directory, Extracted package contents will reside under /tmp/packageName direcotry
     *
     * @param packageName - name of package
     *
     * @throws RepositoryException - thrown if some error occurs during extraction
     */
    public void extractPackageByName( String packageName ) throws RepositoryException;


    /**
     * Searches and lists packages in repository containing "term" in the name
     *
     * @param term - term to search
     *
     * @return - set of {@code PackageInfo}
     *
     * @throws RepositoryException - thrown if some error occurs during search
     */
    public Set<PackageInfo> listPackages( String term ) throws RepositoryException;


    /**
     * Returns detailed information about the specified package
     *
     * @param packageName - name of package
     *
     * @return - string containing package details
     *
     * @throws RepositoryException - thrown if some error occurs
     */
    public String getPackageInfo( String packageName ) throws RepositoryException;
}
