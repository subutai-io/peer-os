/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.api.aptrepositorymanager;


import java.util.List;

import org.safehaus.subutai.shared.protocol.Agent;


public interface AptRepositoryManager {


    public List<PackageInfo> listPackages( Agent agent, String pattern ) throws AptRepoException;

    public void addPackageToRepo( Agent agent, String pathToPackageFile ) throws AptRepoException;

    public void removePackageByFilePath( Agent agent, String packageFileName ) throws AptRepoException;

    public void removePackageByName( Agent agent, String packageName ) throws AptRepoException;

    public String readFileContents( Agent agent, final String pathToPackageFile, final String pathToFileInsidePackage )
            throws AptRepoException;
}
