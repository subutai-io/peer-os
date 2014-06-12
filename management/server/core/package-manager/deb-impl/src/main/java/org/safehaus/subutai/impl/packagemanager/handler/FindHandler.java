package org.safehaus.subutai.impl.packagemanager.handler;

import java.util.Collection;
import org.safehaus.subutai.api.packagemanager.PackageInfo;
import org.safehaus.subutai.impl.packagemanager.DebPackageManager;

public class FindHandler extends AbstractHandler<Collection<PackageInfo>> {

    public FindHandler(DebPackageManager pm, String hostname) {
        super(pm, hostname);
    }

    @Override
    public Collection<PackageInfo> performAction() {
        ListHandler ls = new ListHandler(packageManager, hostname);
        ls.setFromFile(true);
        return ls.performAction();
    }

}
