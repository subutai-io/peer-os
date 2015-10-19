package io.subutai.core.registration.impl.entity.entity;


import org.junit.Before;
import org.junit.Test;

import io.subutai.common.host.Interface;

import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Created by talas on 8/26/15.
 */
public class HostInterfaceModelTest
{

    private io.subutai.core.registration.impl.entity.HostInterface hostInterface;

    String iname = "iname", ip = "ip", mac = "mac";


    @Before
    public void setUp() throws Exception
    {
        Interface iface = mock( Interface.class );
        when( iface.getName() ).thenReturn( iname );
        when( iface.getIp() ).thenReturn( ip );
        when( iface.getMac() ).thenReturn( mac );
        hostInterface = new io.subutai.core.registration.impl.entity.HostInterface( iface );
    }


    @Test
    public void testSetInterfaceName() throws Exception
    {
        String newName = "iname2";
        hostInterface.setInterfaceName( newName );
        assertNotEquals( iname, hostInterface.getInterfaceName() );
    }


    @Test
    public void testSetIp() throws Exception
    {
        String newIp = "ip2";
        hostInterface.setIp( newIp );
        assertNotEquals( ip, hostInterface.getIp() );
    }


    @Test
    public void testSetMac() throws Exception
    {
        String newMac = "mac2";
        hostInterface.setMac( newMac );
        assertNotEquals( mac, hostInterface.getMac() );
    }


    @Test
    public void testEquals() throws Exception
    {

    }


    @Test
    public void testHashCode() throws Exception
    {

    }


    @Test
    public void testToString() throws Exception
    {

    }
}