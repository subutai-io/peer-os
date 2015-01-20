package org.safehaus.subutai.core.lxc.quota.impl;


import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.quota.QuotaType;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.ResourceHost;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class QuotaManagerImplTest
{
    QuotaType parameter = QuotaType.QUOTA_TYPE_RAM;
    String expectedValue = "200000000";
    @Mock
    PeerManager peerManager;
    @Mock
    LocalPeer localPeer;
    @Mock
    ContainerHost containerHost;
    @Mock
    ResourceHost resourceHost;
    @Mock
    CommandResult result;

    QuotaManagerImpl quotaManager;


    @Before
    public void setupClasses() throws PeerException, CommandException
    {
        quotaManager = new QuotaManagerImpl( peerManager );
        when( peerManager.getLocalPeer() ).thenReturn( localPeer );
        when( localPeer.getContainerHostByName( anyString() ) ).thenReturn( containerHost );
        when( localPeer.getResourceHostByName( anyString() ) ).thenReturn( resourceHost );
        when( resourceHost.execute( any( RequestBuilder.class) ) ).thenReturn( result );
        when( result.hasSucceeded() ).thenReturn( true );
    }


    @Ignore
    @Test
    public void testSetQuota() throws Exception
    {
        when( result.getStdOut() ).thenReturn( expectedValue );
        //        quotaManager.setQuota( "containerName", parameter, expectedValue );
        //        String value = quotaManager.getQuota( "containerName", parameter );
        //        assertEquals( expectedValue, value );
    }


    @Ignore
    @Test
    public void testGetQuota() throws Exception
    {
        //        String value = quotaManager.getQuota( "containerName", parameter );
        //        quotaManager.setQuota( "containerName", parameter, "23423412342" );
        //        assertNotEquals( value, "23423412342" );
    }
}