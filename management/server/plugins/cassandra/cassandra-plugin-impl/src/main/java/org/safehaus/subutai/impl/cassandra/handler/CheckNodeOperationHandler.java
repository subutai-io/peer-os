package org.safehaus.subutai.impl.cassandra.handler;


import org.safehaus.subutai.api.cassandra.CassandraConfig;
import org.safehaus.subutai.impl.cassandra.CassandraImpl;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;


/**
 * Created by bahadyr on 8/25/14.
 */
public class CheckNodeOperationHandler extends AbstractOperationHandler<CassandraImpl> {

    private ProductOperation po;
    private CassandraConfig config;


    public CheckNodeOperationHandler( final CassandraImpl manager, final CassandraConfig config ) {
        super( manager, config.getClusterName() );
        this.config = config;
        po = manager.getTracker().createProductOperation( CassandraConfig.PRODUCT_KEY,
                String.format( "Setting up %s cluster...", config.getClusterName() ) );
    }


    @Override
    public void run() {
        // TODO
    }
}
