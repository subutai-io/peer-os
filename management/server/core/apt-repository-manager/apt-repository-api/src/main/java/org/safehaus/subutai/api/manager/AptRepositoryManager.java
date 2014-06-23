/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.api.manager;


import java.util.List;


/**
 */
public interface AptRepositoryManager {


    public List<String> listPackages( String pattern ) throws AptRepoException;

    public void addPackageToRepo( String pathToPackageFile ) throws AptRepoException;

    public void removePackageFromRepo( String packageFileName ) throws AptRepoException;

    public String readFileContents( String pathToFileInsideDebPackage ) throws AptRepoException;
}
