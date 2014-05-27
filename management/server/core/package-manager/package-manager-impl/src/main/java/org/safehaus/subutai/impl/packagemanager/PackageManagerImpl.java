
package org.safehaus.subutai.impl.packagemanager;

import java.util.Collection;
import org.safehaus.subutai.api.packagemanager.PackageInfo;

public class PackageManagerImpl extends PackageManagerBase {

    @Override
    public Collection<PackageInfo> listPackages(String hostname) {
        return null;
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
    public Collection<PackageInfo> deletePackagesInfo(String hostname) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
