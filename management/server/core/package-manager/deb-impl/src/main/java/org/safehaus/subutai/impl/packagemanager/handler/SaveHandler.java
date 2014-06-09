package org.safehaus.subutai.impl.packagemanager.handler;

import java.util.Collection;
import org.safehaus.subutai.api.packagemanager.PackageInfo;
import org.safehaus.subutai.impl.packagemanager.DebPackageManager;

public class SaveHandler extends AbstractHandler<Collection<PackageInfo>> {

    private final String hostname;

    public SaveHandler(DebPackageManager pm, String hostname) {
        super(pm);
        this.hostname = hostname;
    }

    @Override
    public Collection<PackageInfo> performAction() {
        ListHandler lh = new ListHandler(packageManager, hostname);
        Collection<PackageInfo> ls = lh.performAction();
        if(ls == null) return null;

        boolean saved = packageManager.getStorage().persist(hostname, ls);
        return saved ? ls : null;
    }

}
