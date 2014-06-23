/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.api.aptrepositorymanager;


import java.util.List;


public interface AptRepositoryManager {


    public List<PackageInfo> listPackages( String pattern ) throws AptRepoException;

    public void addPackageToRepo( String pathToPackageFile ) throws AptRepoException;

    public void removePackageByFilePath( String packageFileName ) throws AptRepoException;

    public void removePackageByName( String packageName ) throws AptRepoException;

    public String readFileContents( String pathToFileInsideDebPackage ) throws AptRepoException;
}
