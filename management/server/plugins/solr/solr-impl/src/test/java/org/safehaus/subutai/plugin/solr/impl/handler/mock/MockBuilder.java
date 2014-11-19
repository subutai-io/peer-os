package org.safehaus.subutai.plugin.solr.impl.handler.mock;


;


/*public class MockBuilder
{

    public static AbstractOperationHandler getInstallOperationWithResult( boolean success )
    {
        SolrImpl solrImpl = new SolrImplMock().setCommands( getCommands( success ) );
        SolrClusterConfig solrClusterConfig = new SolrClusterConfig().setClusterName( "test-cluster" );

        return new InstallOperationHandler( solrImpl, solrClusterConfig );
    }


    private static Commands getCommands( boolean installSuccess )
    {
        CommandMock installCommand = new CommandMock().setSucceeded( installSuccess );

        return new CommandsMock().setInstallCommand( installCommand );
    }


    public static AbstractOperationHandler getUninstallOperationWithResult( boolean success )
    {

        SolrImpl solrImpl = new SolrImplMock()
                .setClusterSolrClusterConfig( new SolrClusterConfig().setClusterName( "test-cluster" ) );

        return new UninstallOperationHandler( solrImpl, "test-cluster" );
    }


    public static AbstractOperationHandler getAddNodeOperationWithResult( boolean success )
    {
//        LxcManagerMock lxcManagerMock = new LxcManagerMock().setMockLxcMap( CommonMockBuilder.getLxcMap() );

        SolrImpl solrImpl = new SolrImplMock().setCommands( getCommands( success ) )
                                              .setClusterSolrClusterConfig( new SolrClusterConfig() );

        //return new NodeOperationHandler( solrImpl, "test-cluster" );
        return null;
    }
}*/
