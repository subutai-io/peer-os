package io.subutai.core.localpeer.impl.entity;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Sets;

import io.subutai.common.command.CommandCallback;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.host.HostArchitecture;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.HostInterfaceModel;
import io.subutai.common.host.HostInterfaces;
import io.subutai.common.peer.Peer;
import io.subutai.common.security.objects.PermissionObject;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class AbstractSubutaiHostTest
{
    private static final String PEER_ID = UUID.randomUUID().toString();
    private static final String HOST_ID = UUID.randomUUID().toString();
    private static final String HOSTNAME = "hostname";
    private static final HostArchitecture ARCH = HostArchitecture.AMD64;
    private static final String INTERFACE_NAME = "eth0";
    private static final String IP = "127.0.0.1";


    @Mock
    Peer peer;
    @Mock
    HostInterfaceModel anHostInterface;
    @Mock
    HostInfo hostInfo;
    @Mock
    RequestBuilder requestBuilder;
    @Mock
    CommandCallback callback;


    AbstractSubutaiHost host;
    @Mock
    private HostInterfaces hostInterfaces;


    static class HostImpl extends AbstractSubutaiHost
    {
        public HostImpl( final String peerId, final HostInfo hostInfo )
        {
            super( peerId, hostInfo );
        }


        @Override
        public int compareTo( final HostInfo o )
        {
            return 0;
        }


        @Override
        public boolean isConnected()
        {
            return true;
        }


        @Override
        public String getLinkId()
        {
            return String.format( "%s|%s", getClassPath(), getUniqueIdentifier() );
        }


        @Override
        public String getUniqueIdentifier()
        {
            return getId();
        }


        @Override
        public String getClassPath()
        {
            return this.getClass().getSimpleName();
        }


        @Override
        public String getContext()
        {
            return PermissionObject.PeerManagement.getName();
        }


        @Override
        public String getKeyId()
        {
            return getPeerId();
        }
    }


    @Before
    public void setUp() throws Exception
    {
        when( hostInterfaces.getAll() ).thenReturn( Sets.newHashSet( anHostInterface ) );
        when( hostInfo.getId() ).thenReturn( HOST_ID );
        when( hostInfo.getHostname() ).thenReturn( HOSTNAME );
        when( hostInfo.getArch() ).thenReturn( ARCH );
        when( hostInfo.getHostInterfaces() ).thenReturn( hostInterfaces );
        when( anHostInterface.getName() ).thenReturn( INTERFACE_NAME );
        when( anHostInterface.getIp() ).thenReturn( IP );
        host = new HostImpl( PEER_ID, hostInfo );
        host.setPeer( peer );
        host.init();
    }


    @Test
    public void testGetNSetPeer() throws Exception
    {
        Peer peer = mock( Peer.class );
        host.setPeer( peer );

        assertEquals( peer, host.getPeer() );
    }


    @Test
    public void testExecute() throws Exception
    {
        host.execute( requestBuilder );

        verify( peer ).execute( requestBuilder, host, null );
    }


    @Test
    public void testExecuteWithCallback() throws Exception
    {

        host.execute( requestBuilder, callback );

        verify( peer ).execute( requestBuilder, host, callback );
    }


    @Test
    public void testExecuteAsync() throws Exception
    {
        host.executeAsync( requestBuilder );

        verify( peer ).executeAsync( requestBuilder, host, null );
    }


    @Test
    public void testExecuteAsyncWithCallback() throws Exception
    {
        host.executeAsync( requestBuilder, callback );

        verify( peer ).executeAsync( requestBuilder, host, callback );
    }


    @Test
    public void testGetId() throws Exception
    {
        assertEquals( HOST_ID, host.getId() );
    }


    @Test
    public void testGetPeerId() throws Exception
    {
        assertEquals( PEER_ID, host.getPeerId() );
    }


    @Test
    public void testGetHostname() throws Exception
    {
        assertEquals( HOSTNAME, host.getHostname() );
    }


    @Test
    public void testUpdateHostInfo() throws Exception
    {
        long lastHeartBeat = host.getLastHeartbeat();

        host.updateHostInfo( hostInfo );

        assertTrue( host.getLastHeartbeat() > lastHeartBeat );
    }


    @Test
    public void testGetLastHeartbeat() throws Exception
    {
        assertTrue( host.getLastHeartbeat() == 0 );
    }


    @Test
    public void testGetInterfaces() throws Exception
    {
        assertFalse( host.getHostInterfaces().getAll().isEmpty() );
    }


    @Test
    public void testGetHostArchitecture() throws Exception
    {
        assertEquals( ARCH, host.getArch() );
    }


    @Test
    public void testToString() throws Exception
    {
        assertThat( host.toString(), containsString( PEER_ID ) );
    }


    @Test
    public void testEquals() throws Exception
    {
        HostImpl host1 = new HostImpl( PEER_ID, hostInfo );

        assertEquals( host1, host );

        host1 = null;

        assertFalse( host.equals( host1 ) );
    }


    @Test
    public void testHashcode() throws Exception
    {
        HostImpl host1 = new HostImpl( PEER_ID, hostInfo );

        assertEquals( host1.hashCode(), host.hashCode() );
    }
}
