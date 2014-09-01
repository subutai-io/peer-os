package org.safehaus.subutai.plugin.presto.impl.handler;


import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.plugin.presto.api.Presto;
import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.ClusterSetupException;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.shared.protocol.ConfigBase;

import com.google.common.base.Preconditions;


/**
 * Created by daralbaev on 01.09.14.
 */
public class PrestoDbSetupStrategy implements ClusterSetupStrategy {

    private Environment environment;
    private ProductOperation po;
    private Presto prestoManager;
    private PrestoClusterConfig prestoClusterConfig;


    public PrestoDbSetupStrategy( final ProductOperation po, final Presto prestoManager,
                                  final PrestoClusterConfig prestoClusterConfig ) {

        Preconditions.checkNotNull( prestoClusterConfig, "Presto cluster config is null" );
        Preconditions.checkNotNull( po, "Product operation tracker is null" );
        Preconditions.checkNotNull( prestoManager, "Presto manager is null" );

        this.po = po;
        this.prestoManager = prestoManager;
        this.prestoClusterConfig = prestoClusterConfig;
    }


    public PrestoDbSetupStrategy( final Environment environment, final ProductOperation po, final Presto prestoManager,
                                  final PrestoClusterConfig prestoClusterConfig ) {
        Preconditions.checkNotNull( prestoClusterConfig, "Presto cluster config is null" );
        Preconditions.checkNotNull( environment, "Environment is null" );
        Preconditions.checkNotNull( po, "Product operation tracker is null" );
        Preconditions.checkNotNull( prestoManager, "Presto manager is null" );

        this.environment = environment;
        this.po = po;
        this.prestoManager = prestoManager;
        this.prestoClusterConfig = prestoClusterConfig;
    }


    @Override
    public ConfigBase setup() throws ClusterSetupException {
        return null;
    }
}
