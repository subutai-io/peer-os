package org.safehaus.subutai.plugin.pig.impl.handler;


import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.pig.api.PigConfig;
import org.safehaus.subutai.plugin.pig.api.SetupType;
import org.safehaus.subutai.plugin.pig.impl.PigImpl;


public class AddNodeOperationHandler extends AbstractOperationHandler<PigImpl>
{

    private final String hostname;

    public AddNodeOperationHandler( PigImpl manager, String clusterName, String hostname )
    {
        super( manager, clusterName );
        this.hostname = hostname;
        productOperation = manager.getTracker().createProductOperation( PigConfig.PRODUCT_KEY,
                String.format( "Adding node %s to %s", ( hostname != null ? hostname : "" ), clusterName ) );
    }


    @Override
    public void run()
    {
        ProductOperation po = productOperation;
        PigConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            po.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        try
        {
            Agent agent;
            if ( config.getSetupType() == SetupType.OVER_HADOOP )
            {
                agent = setupHost( config );
            }
            else if ( config.getSetupType() == SetupType.WITH_HADOOP )
            {
                agent = addHost( config );
            }
            else
            {
                throw new ClusterSetupException( "No setup type" );
            }

            config.getNodes().add( agent );

            po.addLog( "Saving cluster info..." );
            manager.getPluginDao().saveInfo( PigConfig.PRODUCT_KEY, clusterName, config );
            po.addLog( "Saved cluster info" );

            po.addLogDone( null );
        }
        catch ( ClusterSetupException ex )
        {
            po.addLog( ex.getMessage() );
            po.addLogFailed( "Add worker node failed" );
        }
    }

    public Agent setupHost( PigConfig config )
    {

        return null;
    }

    public Agent addHost( PigConfig config )
    {

        return null;
    }
}
