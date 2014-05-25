package org.safehaus.kiskis.mgmt.impl.solr.mock;


import org.safehaus.subutai.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.solr.Config;
import org.safehaus.kiskis.mgmt.impl.solr.Commands;
import org.safehaus.kiskis.mgmt.impl.solr.SolrImpl;
import org.safehaus.kiskis.mgmt.impl.solr.handler.AddNodeOperationHandler;
import org.safehaus.kiskis.mgmt.impl.solr.handler.InstallOperationHandler;
import org.safehaus.kiskis.mgmt.impl.solr.handler.UninstallOperationHandler;
import org.safehaus.subutai.product.common.test.unit.mock.CommandMock;
import org.safehaus.subutai.product.common.test.unit.mock.CommonMockBuilder;
import org.safehaus.subutai.product.common.test.unit.mock.DbManagerMock;
import org.safehaus.subutai.product.common.test.unit.mock.LxcManagerMock;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;


public class MockBuilder {

    public static AbstractOperationHandler getInstallOperationWithResult( boolean success ) {
        SolrImpl solrImpl = new SolrImplMock().setCommands( getCommands( success ) );
        Config config = new Config().setClusterName( "test-cluster" );

        return new InstallOperationHandler( solrImpl, config );
    }

    public static AbstractOperationHandler getUninstallOperationWithResult( boolean success ) {

        DbManager dbManager = new DbManagerMock().setDeleteInfoResult( success );

        SolrImpl solrImpl = new SolrImplMock()
                .setClusterConfig( new Config() )
                .setDbManager( dbManager );

        return new UninstallOperationHandler( solrImpl, "test-cluster" );
    }

    public static AbstractOperationHandler getAddNodeOperationWithResult( boolean success ) {
        LxcManagerMock lxcManagerMock = new LxcManagerMock().setMockLxcMap( CommonMockBuilder.getLxcMap() );

        SolrImpl solrImpl = new SolrImplMock()
                .setCommands( getCommands( success ) )
                .setClusterConfig( new Config() )
                .setLxcManager( lxcManagerMock );

        return new AddNodeOperationHandler( solrImpl, "test-cluster" );
    }

    private static Commands getCommands( boolean installSuccess ) {
        CommandMock installCommand = new CommandMock().setSucceeded( installSuccess );

        return new CommandsMock().setInstallCommand( installCommand );
    }
}
