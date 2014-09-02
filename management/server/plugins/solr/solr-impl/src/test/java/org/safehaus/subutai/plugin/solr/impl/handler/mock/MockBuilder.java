package org.safehaus.subutai.plugin.solr.impl.handler.mock;


import org.safehaus.subutai.api.dbmanager.DbManager;
import org.safehaus.subutai.plugin.solr.api.SolrClusterConfig;
import org.safehaus.subutai.plugin.solr.impl.Commands;
import org.safehaus.subutai.plugin.solr.impl.SolrImpl;
import org.safehaus.subutai.plugin.solr.impl.handler.AddNodeOperationHandler;
import org.safehaus.subutai.plugin.solr.impl.handler.InstallOperationHandler;
import org.safehaus.subutai.plugin.solr.impl.handler.UninstallOperationHandler;
import org.safehaus.subutai.product.common.test.unit.mock.CommandMock;
import org.safehaus.subutai.product.common.test.unit.mock.CommonMockBuilder;
import org.safehaus.subutai.product.common.test.unit.mock.DbManagerMock;
import org.safehaus.subutai.product.common.test.unit.mock.LxcManagerMock;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;


public class MockBuilder {

    public static AbstractOperationHandler getInstallOperationWithResult( boolean success ) {
        SolrImpl solrImpl = new SolrImplMock().setCommands( getCommands( success ) );
        SolrClusterConfig solrClusterConfig = new SolrClusterConfig().setClusterName( "test-cluster" );

        return new InstallOperationHandler( solrImpl, solrClusterConfig );
    }


    public static AbstractOperationHandler getUninstallOperationWithResult( boolean success ) {

        DbManager dbManager = new DbManagerMock().setDeleteInfoResult( success );

        SolrImpl solrImpl = new SolrImplMock().setClusterSolrClusterConfig( new SolrClusterConfig() );

        return new UninstallOperationHandler( solrImpl, "test-cluster" );
    }


    public static AbstractOperationHandler getAddNodeOperationWithResult( boolean success ) {
        LxcManagerMock lxcManagerMock = new LxcManagerMock().setMockLxcMap( CommonMockBuilder.getLxcMap() );

        SolrImpl solrImpl = new SolrImplMock().setCommands( getCommands( success ) )
                                              .setClusterSolrClusterConfig( new SolrClusterConfig() );

        return new AddNodeOperationHandler( solrImpl, "test-cluster" );
    }


    private static Commands getCommands( boolean installSuccess ) {
        CommandMock installCommand = new CommandMock().setSucceeded( installSuccess );

        return new CommandsMock().setInstallCommand( installCommand );
    }
}
