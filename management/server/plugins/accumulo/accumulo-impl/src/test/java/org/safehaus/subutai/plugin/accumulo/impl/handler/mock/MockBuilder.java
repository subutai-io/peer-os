package org.safehaus.subutai.plugin.accumulo.impl.handler.mock;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
//import org.safehaus.subutai.core.tracker.impl.TrackerImpl;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.accumulo.api.NodeType;
import org.safehaus.subutai.plugin.accumulo.impl.AccumuloImpl;
import org.safehaus.subutai.plugin.accumulo.impl.Commands;
import org.safehaus.subutai.plugin.accumulo.impl.handler.AddNodeOperationHandler;
import org.safehaus.subutai.plugin.accumulo.impl.handler.InstallOperationHandler;
import org.safehaus.subutai.plugin.accumulo.impl.handler.UninstallOperationHandler;
//import org.safehaus.subutai.product.common.test.unit.mock.CommandMock;
//import org.safehaus.subutai.product.common.test.unit.mock.CommonMockBuilder;
//import org.safehaus.subutai.product.common.test.unit.mock.DbManagerMock;
//import org.safehaus.subutai.product.common.test.unit.mock.LxcManagerMock;

import java.util.Date;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class MockBuilder {

//    public static AbstractOperationHandler getInstallOperationWithResult( boolean success ) {
//        AccumuloImpl accumuloImplMock = new AccumuloImplMock().setCommands( getCommands( success ) );
//        AccumuloClusterConfig accumuloClusterConfig = new AccumuloClusterConfig();
//        accumuloClusterConfig.setClusterName( "test-cluster" );
//
//        return new InstallOperationHandler( accumuloImplMock, accumuloClusterConfig );
//    }


//    public static AbstractOperationHandler getUninstallOperationWithResult( boolean success ) {
//
//        DbManager dbManager = new DbManagerMock().setDeleteInfoResult( success );
//
//        AccumuloImpl solrImpl = new AccumuloImplMock().setClusterAccumuloClusterConfig( new AccumuloClusterConfig() );
//
//        return new UninstallOperationHandler( solrImpl, "test-cluster" );
//    }
//
//
//    public static AbstractOperationHandler getAddNodeOperationWithResult( boolean success ) {
//        LxcManagerMock lxcManagerMock = new LxcManagerMock().setMockLxcMap( CommonMockBuilder.getLxcMap() );
//
//        AccumuloClusterConfig config = new AccumuloClusterConfig();
//        AccumuloImpl accumuloImplMock = new AccumuloImplMock().setCommands( getCommands( success ) )
//                                              .setClusterAccumuloClusterConfig( config );
//
//        return new AddNodeOperationHandler( accumuloImplMock, "test-cluster", "test-node", NodeType.TRACER );
//    }
//
//
//    private static Commands getCommands( boolean installSuccess ) {
//        CommandMock installCommand = new CommandMock().setSucceeded( installSuccess );
//
//        return new CommandsMock().setInstallCommand( installCommand );
//    }

    public static Tracker getTrackerMock(){
        Tracker tr = mock( Tracker.class );
        tr.createProductOperation( "source", "source" );
        return tr;
    }


//    public ProductOperation createProductOperation( String source, String description ) {
//        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), "Source is null or empty" );
//        Preconditions.checkNotNull( !Strings.isNullOrEmpty( description ), "Description is null or empty" );
//
//        ProductOperationImpl po = new ProductOperationImpl( source.toLowerCase(), description, this );
//        if ( saveProductOperation( source, po ) ) {
//            return po;
//        }
//        return null;
//    }
}
