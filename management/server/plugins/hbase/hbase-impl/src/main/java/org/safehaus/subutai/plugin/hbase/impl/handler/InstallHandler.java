package org.safehaus.subutai.plugin.hbase.impl.handler;


import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.plugin.hbase.api.HBaseClusterConfig;
import org.safehaus.subutai.plugin.hbase.api.SetupType;
import org.safehaus.subutai.plugin.hbase.impl.HBaseImpl;


public class InstallHandler extends AbstractOperationHandler<HBaseImpl, HBaseClusterConfig>
{

    private HBaseClusterConfig config;


    public InstallHandler( final HBaseImpl manager, final HBaseClusterConfig config )
    {
        super( manager, config.getClusterName() );
        this.config = config;
        trackerOperation = manager.getTracker().createTrackerOperation( HBaseClusterConfig.PRODUCT_KEY,
                String.format( "Setting up %s cluster...", config.getClusterName() ) );
    }


    @Override
    public void run()
    {
        if ( config.getSetupType() == SetupType.OVER_HADOOP )
        {
            setupOverHadoop();
        }
        else
        {
            setupWithHadoop();
        }
    }


    private void setupOverHadoop()
    {
        /*try
        {
            //setup up HBase cluster
            ClusterSetupStrategy setupStrategy = manager.getClusterSetupStrategy( null, config, trackerOperation );
            //            HadoopClusterConfig hadoopClusterConfig = manager.getC
            setupStrategy.setup();

            trackerOperation.addLogDone( String.format( "Cluster %s set up successfully", clusterName ) );
        }
        catch ( ClusterSetupException e )
        {
            trackerOperation
                    .addLogFailed( String.format( "Failed to setup cluster %s : %s", clusterName, e.getMessage() ) );
        }*/
    }


    private void setupWithHadoop()
    {

    }
}
