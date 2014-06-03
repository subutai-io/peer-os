package org.safehaus.subutai.impl.packagemanager.handler;

import java.util.Collection;
import org.safehaus.subutai.api.packagemanager.PackageInfo;
import org.safehaus.subutai.impl.packagemanager.PackageManagerImpl;

public class DeleteHandler extends AbstractHandler<Boolean> {

    private final String hostname;

    public DeleteHandler(PackageManagerImpl pm, String hostname) {
        super(pm);
        this.hostname = hostname;
    }

    @Override
    public Boolean performAction() {
        FindHandler h = new FindHandler(packageManager, hostname);
        Collection<PackageInfo> col = h.performAction();
        if(col == null) return Boolean.FALSE;

        return packageManager.getStorage().delete(hostname);
    }

}
