package org.safehaus.subutai.impl.packagemanager.handler;

import java.util.Collection;
import org.safehaus.subutai.api.packagemanager.PackageInfo;
import org.safehaus.subutai.impl.packagemanager.PackageManagerImpl;

public class FindHandler extends AbstractHandler<Collection<PackageInfo>> {

    private final String hostname;

    public FindHandler(PackageManagerImpl pm, String hostname) {
        super(pm);
        this.hostname = hostname;
    }

    @Override
    public Collection<PackageInfo> performAction() {
        return packageManager.getStorage().retrieve(hostname);
    }

}
