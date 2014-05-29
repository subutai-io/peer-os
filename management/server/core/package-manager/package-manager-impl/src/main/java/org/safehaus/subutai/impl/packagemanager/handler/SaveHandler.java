package org.safehaus.subutai.impl.packagemanager.handler;

import java.util.Collection;
import org.safehaus.subutai.api.packagemanager.PackageInfo;
import org.safehaus.subutai.impl.packagemanager.PackageManagerImpl;

public class SaveHandler extends AbstractHandler<Collection<PackageInfo>> {

    private final String hostname;

    public SaveHandler(PackageManagerImpl pm, String hostname) {
        super(pm);
        this.hostname = hostname;
    }

    @Override
    public Collection<PackageInfo> performAction() {
        ListHandler lh = new ListHandler(packageManager, hostname);
        Collection<PackageInfo> ls = lh.performAction();
        if(ls == null) return null;

        boolean saved = packageManager.getDbManager().saveInfo(
                PackageInfo.SOURCE_NAME, hostname, ls);
        return saved ? ls : null;
    }

}
