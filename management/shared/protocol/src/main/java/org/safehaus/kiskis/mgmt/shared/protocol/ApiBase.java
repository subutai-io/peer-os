package org.safehaus.kiskis.mgmt.shared.protocol;

import java.util.List;
import java.util.UUID;

/**
 * Created by dilshat on 5/1/14.
 */
public interface ApiBase<T> {

    public UUID installCluster(T config);

    public UUID uninstallCluster(String clusterName);

    public List<T> getClusters();

    public T getCluster(String clusterName);
}
