package io.subutai.core.repository.api;


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
     * @param packageName - full name of package
     *
     * @throws RepositoryException -  thrown if some error occurs during removal
     */
    public void removePackageByName( String packageName ) throws RepositoryException;

    /**
     * Extracts package to /tmp directory, Extracted package contents will reside under /tmp/packageName directory
     *
     * @param packageName - full name of package
     *
     * @throws RepositoryException - thrown if some error occurs during extraction
     */
    public void extractPackageByName( String packageName ) throws RepositoryException;

    /**
     * Extracts specified files from the package to /tmp/packageName. They will reside in the same subdirectories as in
     * the package
     *
     * @param packageName - full name of package
     * @param files - relative paths to files inside the package
     *
     * @throws RepositoryException - thrown if some error occurs during extraction
     */
    public void extractPackageFiles( String packageName, Set<String> files ) throws RepositoryException;


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
     * @param packageName - short name of package, taken from listPackages output
     *
     * @return - string containing package details
     *
     * @throws RepositoryException - thrown if some error occurs
     */
    public String getPackageInfo( String packageName ) throws RepositoryException;

    /**
     * Returns full package name by short package name
     */
    public String getFullPackageName( String shortPackageName ) throws RepositoryException;


    public void addAptSource( String hostname, String ip ) throws RepositoryException;

    public void removeAptSource( String ip ) throws RepositoryException;
}
