package org.safehaus.subutai.impl.packagemanager;

import java.util.Collection;
import org.safehaus.subutai.api.packagemanager.PackageInfo;
import org.safehaus.subutai.impl.packagemanager.handler.ListHandler;

public class PackageManagerImpl extends PackageManagerBase {

    @Override
    public Collection<PackageInfo> listPackages(String hostname) {
        ListHandler h = new ListHandler(this, hostname);
        return h.performAction();
    }

    @Override
    public Collection<PackageInfo> getPackagesInfo(String hostname) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<PackageInfo> savePackagesInfo(String hostname) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean deletePackagesInfo(String hostname) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
