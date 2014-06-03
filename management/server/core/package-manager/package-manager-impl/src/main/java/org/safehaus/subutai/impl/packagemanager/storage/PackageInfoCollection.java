package org.safehaus.subutai.impl.packagemanager.storage;

import java.util.ArrayList;
import java.util.Collection;
import org.safehaus.subutai.api.packagemanager.PackageInfo;

/**
 * A wrapper class for <code>PackageInfo</code> collection. Use this class if
 * persisting <code>PackageInfo</code> collections using <code>DbManager</code>.
 */
class PackageInfoCollection {

    private final Collection<PackageInfo> items;

    public PackageInfoCollection() {
        this.items = new ArrayList<>();
    }

    public PackageInfoCollection(Collection<PackageInfo> items) {
        this.items = new ArrayList<>(items);
    }

    public Collection<PackageInfo> getItems() {
        return items;
    }

}
