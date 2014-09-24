package org.safehaus.subutai.common.protocol;


import java.util.List;
import java.util.UUID;


/**
 * Created by dilshat on 5/1/14.
 */
public interface ApiBase<T extends ConfigBase>
{

    /**
     * Installs cluster according to the specified configuration object
     *
     * @param config - cluster configuration object
     *
     * @return - UUID of operation to track
     */
    public UUID installCluster( T config );

    /**
     * Uninstalls the specified cluster
     *
     * config - cluster configuration object
     *
     * @return - UUID of operation to track
     */
    public UUID uninstallCluster( T config );

    /**
     * Returns list of configuration objects of installed clusters
     *
     * @return - list of configuration objects of installed clusters
     */
    public List<T> getClusters();

    /**
     * Returns configuration object of installed cluster by name
     *
     * @param clusterName - - name of cluster
     *
     * @return - configuration object of installed cluster
     */
    public T getCluster( String clusterName );
}
