/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.api.manager;


/**
 */
public interface AptRepositoryManager {


    public void getPackagesInRepository();

    public void putPackageIntoRepository();

    public void removePackageFromRepository();

    public void searchPackageInRepository();

    public void exportRepository();


}
