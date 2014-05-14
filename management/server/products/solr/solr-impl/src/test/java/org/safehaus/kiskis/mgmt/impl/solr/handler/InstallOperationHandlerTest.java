package org.safehaus.kiskis.mgmt.impl.solr.handler;


import org.junit.BeforeClass;
import org.junit.Test;
import org.safehaus.kiskis.mgmt.api.solr.Config;
import org.safehaus.kiskis.mgmt.shared.protocol.operation.AbstractOperationHandler;
import org.safehaus.kiskis.mgmt.impl.solr.SolrImpl;
import org.safehaus.kiskis.mgmt.impl.solr.util.AgentManagerMock;
import org.safehaus.kiskis.mgmt.impl.solr.util.CommandRunnerMock;
import org.safehaus.kiskis.mgmt.impl.solr.util.DbManagerMock;
import org.safehaus.kiskis.mgmt.impl.solr.util.LxcManagerMock;
import org.safehaus.kiskis.mgmt.impl.solr.util.TrackerMock;
import org.safehaus.kiskis.mgmt.shared.protocol.operation.ProductOperationState;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;


public class InstallOperationHandlerTest {
    private static SolrImpl solrImpl;


    @BeforeClass
    public static void setUp() {
        solrImpl = new SolrImpl( new CommandRunnerMock(), new AgentManagerMock(), new DbManagerMock(), new TrackerMock(),
                        new LxcManagerMock() );
    }


    @Test( expected = NullPointerException.class )
    public void testWithNullConfig() {
        solrImpl.installCluster( null );
    }


    @Test
    public void testWithMalformedConfiguration() {
        Config config = new Config();
        AbstractOperationHandler operationHandler = new InstallOperationHandler( solrImpl, config );

        operationHandler.run();

        assertTrue( operationHandler.getProductOperation().getLog().contains( "Malformed configuration" ) );
        assertEquals( operationHandler.getProductOperation().getState(), ProductOperationState.FAILED );
    }

}
