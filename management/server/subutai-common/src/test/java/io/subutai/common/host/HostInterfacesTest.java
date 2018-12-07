package io.subutai.common.host;


import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import io.subutai.common.settings.Common;
import io.subutai.common.util.JsonUtil;

import static org.junit.Assert.assertFalse;


public class HostInterfacesTest
{

    private static String ID = "id-123";
    private static String IP = "172.16.1.2";
    private static final String IFACE_JSON =
            String.format( "{ \"hostId\":\"%s\", \"interfaces\" : [{ \"interfaceName\":\"%s\", " + "\"ip\":\"%s\"}] }",
                    ID, Common.DEFAULT_CONTAINER_INTERFACE, IP );


    HostInterfaces hostInterfaces;


    @Before
    public void setUp() throws Exception
    {
        hostInterfaces = JsonUtil.fromJson( IFACE_JSON, HostInterfaces.class );
    }


    @Test
    public void testFilterByIp()
    {
        Set<HostInterfaceModel> ifaces = hostInterfaces.filterByIp( "172.*" );

        assertFalse( ifaces.isEmpty() );
    }


    @Test
    public void testFilterByName()
    {
        Set<HostInterfaceModel> ifaces = hostInterfaces.filterByName( Common.DEFAULT_CONTAINER_INTERFACE );

        assertFalse( ifaces.isEmpty() );
    }
}
