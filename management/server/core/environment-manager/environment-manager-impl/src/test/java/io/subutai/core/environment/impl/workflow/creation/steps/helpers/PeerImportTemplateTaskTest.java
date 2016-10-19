package io.subutai.core.environment.impl.workflow.creation.steps.helpers;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Sets;

import io.subutai.common.environment.PrepareTemplatesRequest;
import io.subutai.common.environment.PrepareTemplatesResponse;
import io.subutai.common.peer.Peer;
import io.subutai.core.environment.impl.TestHelper;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;


@RunWith( MockitoJUnitRunner.class )
public class PeerImportTemplateTaskTest
{

    PeerImportTemplateTask task;

    @Mock
    PrepareTemplatesResponse response;

    Peer PEER;


    @Before
    public void setUp() throws Exception
    {
        PEER = TestHelper.PEER();

        task = new PeerImportTemplateTask( TestHelper.ENV_ID, PEER, Sets.newHashSet( TestHelper.NODE() ),
                TestHelper.TRACKER_OPERATION() );

        doReturn( response ).when( PEER ).prepareTemplates( any( PrepareTemplatesRequest.class ) );
        doReturn( Sets.newHashSet( TestHelper.MESSAGE ) ).when( response ).getMessages();
    }


    @Test
    public void testCall() throws Exception
    {
        try
        {

            task.call();

            fail( "IllegalStateException expected" );
        }
        catch ( IllegalStateException e )
        {
        }

        doReturn( true ).when( response ).hasSucceeded();

        assertNotNull( task.call() );
    }
}
