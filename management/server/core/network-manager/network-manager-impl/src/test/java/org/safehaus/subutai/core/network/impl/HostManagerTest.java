package org.safehaus.subutai.core.network.impl;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandUtil;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.core.network.api.NetworkManagerException;

import com.google.common.collect.Sets;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class HostManagerTest
{

    private static final String DOMAIN = "domain";
    @Mock
    ContainerHost containerHost;
    @Mock
    CommandUtil commandUtil;

    private HostManager hostManager;


    @Before
    public void setUp() throws Exception
    {
        hostManager = new HostManager( Sets.newHashSet( containerHost ), DOMAIN );
        hostManager.commandUtil = commandUtil;
    }


    @Test( expected = NetworkManagerException.class )
    public void testExecute() throws Exception
    {
        hostManager.execute();

        verify( commandUtil ).execute( any( RequestBuilder.class ), eq( containerHost ) );

        doThrow( new CommandException( "" ) ).when( commandUtil )
                                             .execute( any( RequestBuilder.class ), eq( containerHost ) );

        hostManager.execute();
    }
}
