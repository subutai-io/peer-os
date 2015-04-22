package org.safehaus.subutai.core.peer.impl.container;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.command.CommandUtil;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.core.peer.api.ResourceHost;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class DestroyContainerTaskTest
{
    private static final String HOSTNAME = "hostname";
    @Mock
    ResourceHost resourceHost;
    @Mock
    CommandUtil commandUtil;

    DestroyContainerTask task;


    @Before
    public void setUp() throws Exception
    {
        task = new DestroyContainerTask( resourceHost, HOSTNAME );
        task.commandUtil = commandUtil;
    }


    @Test
    public void testCall() throws Exception
    {
        task.call();

        verify( commandUtil ).execute( any( RequestBuilder.class ), eq( resourceHost ) );
    }
}
