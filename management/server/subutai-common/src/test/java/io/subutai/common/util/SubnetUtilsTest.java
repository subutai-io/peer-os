package io.subutai.common.util;


import org.junit.Test;

import org.apache.commons.net.util.SubnetUtils;

import static junit.framework.TestCase.assertEquals;


public class SubnetUtilsTest
{
    @Test
    public void testIp()
    {
        SubnetUtils utils = new SubnetUtils( "10.12.0.1/24" );
        final SubnetUtils.SubnetInfo info = utils.getInfo();
        assertEquals( "10.12.0.1", info.getAddress() );
        assertEquals( "10.12.0.0", info.getNetworkAddress() );
        assertEquals( "10.12.0.255", info.getBroadcastAddress() );
        assertEquals( 254, info.getAddressCount() );
        assertEquals( "10.12.0.1/24", info.getCidrSignature() );
        assertEquals( "10.12.0.1", info.getLowAddress() );
        assertEquals( "10.12.0.254", info.getHighAddress() );
        assertEquals( "255.255.255.0", info.getNetmask() );
        assertEquals( true, info.isInRange( "10.12.0.100" ) );
        assertEquals( false, info.isInRange( "10.11.0.1" ) );
    }
}
