package org.safehaus.subutai.impl.packagemanager;

import java.util.Collection;
import org.safehaus.subutai.api.packagemanager.PackageInfo;
import org.safehaus.subutai.impl.packagemanager.handler.DeleteHandler;
import org.safehaus.subutai.impl.packagemanager.handler.FindHandler;
import org.safehaus.subutai.impl.packagemanager.handler.ListHandler;
import org.safehaus.subutai.impl.packagemanager.handler.SaveHandler;

public class PackageManagerImpl extends PackageManagerBase {

    @Override
    public Collection<PackageInfo> listPackages(String hostname) {
        ListHandler h = new ListHandler(this, hostname);
        return h.performAction();
    }

    @Override
    public Collection<PackageInfo> findPackagesInfo(String hostname) {
        FindHandler h = new FindHandler(this, hostname);
        return h.performAction();
    }

    @Override
    public Collection<PackageInfo> savePackagesInfo(String hostname) {
        SaveHandler h = new SaveHandler(this, hostname);
        return h.performAction();
    }

    @Override
    public boolean deletePackagesInfo(String hostname) {
        DeleteHandler h = new DeleteHandler(this, hostname);
        Boolean b = h.performAction();
        return b != null ? b.booleanValue() : false;
    }

}
