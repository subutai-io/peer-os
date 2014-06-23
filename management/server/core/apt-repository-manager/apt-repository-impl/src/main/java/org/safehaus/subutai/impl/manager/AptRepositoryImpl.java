/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.impl.manager;


import java.util.List;

import org.safehaus.subutai.api.manager.AptRepoException;
import org.safehaus.subutai.api.manager.AptRepositoryManager;


/**
 * This is an implementation of LxcManager
 */
public class AptRepositoryImpl implements AptRepositoryManager {


    @Override
    public List<String> listPackages( final String pattern ) throws AptRepoException {
        return null;
    }


    @Override
    public void addPackageToRepo( final String pathToPackageFile ) throws AptRepoException {

    }


    @Override
    public void removePackageFromRepo( final String packageFileName ) throws AptRepoException {

    }


    @Override
    public String readFileContents( final String pathToFileInsideDebPackage ) throws AptRepoException {
        return null;
    }
}
