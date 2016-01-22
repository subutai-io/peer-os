package io.subutai.common.util;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.protocol.ControlNetworkConfig;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class ControlNetworkUtilTest
{
    private static final String SECRET_KEY = "secret";
    private static final long SECRET_TTL = 100;
    private static final String PEER_ID1 = UUID.randomUUID().toString();
    private static final String PEER_ID2 = UUID.randomUUID().toString();
    private static final String PEER_ID3 = UUID.randomUUID().toString();
    private static final String[] PEER_1_USED_NETWORKS = { "10.200.0.0" };
    private static final String[] PEER_2_USED_NETWORKS = { "10.200.0.0", "10.200.1.0" };
    private static final String[] PEER_3_USED_NETWORKS = { "10.200.0.0", "10.200.1.0" , "10.200.2.0" };
    private static String FINGERPRINT = UUID.randomUUID().toString();
    private static String NETWORK = "10.200.0.0";
    private static String IP1 = "10.200.0.1";
    private static String IP2 = "10.200.0.2";
    private static String IP3 = "10.200.0.3";
    private static String IP4 = "10.200.4.3";

    @Mock
    ControlNetworkConfig config1;
    @Mock
    ControlNetworkConfig config2;
    @Mock
    ControlNetworkConfig config3;

    private List<ControlNetworkConfig> existingConfig;
    private List<String> usedNets1;
    private List<String> usedNets2;
    private List<String> usedNets3;


    @Before
    public void setup()
    {

        usedNets1 = new ArrayList<>();
        for ( String s : PEER_1_USED_NETWORKS )
        {
            usedNets1.add( s );
        }

        usedNets2 = new ArrayList<>();
        for ( String s : PEER_2_USED_NETWORKS )
        {
            usedNets2.add( s );
        }        usedNets3 = new ArrayList<>();
        for ( String s : PEER_3_USED_NETWORKS )
        {
            usedNets3.add( s );
        }

        config1 = new ControlNetworkConfig( PEER_ID1, IP1, FINGERPRINT, SECRET_KEY, SECRET_TTL, usedNets1 );
        existingConfig = new ArrayList<>();
        existingConfig.add( config1 );
        config2 = new ControlNetworkConfig( PEER_ID2, IP2, FINGERPRINT, SECRET_KEY, SECRET_TTL, usedNets2 );
        existingConfig.add( config2 );

        config3 = new ControlNetworkConfig( PEER_ID3, null, null, null, 0, usedNets3 );
        existingConfig.add( config2 );
    }


    @Test
    public void testFindNextAddress() throws ControlNetworkException
    {
        ControlNetworkUtil util =
                new ControlNetworkUtil( FINGERPRINT, NETWORK, SECRET_KEY, SECRET_TTL, existingConfig );

        assertEquals( "10.200.0.3", util.findNextAddress() );
    }


    @Test( expected = ControlNetworkException.class )
    public void testClashes() throws ControlNetworkException
    {
        ControlNetworkUtil util =
                new ControlNetworkUtil( FINGERPRINT, NETWORK, SECRET_KEY, SECRET_TTL, existingConfig );

        ControlNetworkConfig config =
                new ControlNetworkConfig( PEER_ID3, null, FINGERPRINT, SECRET_KEY, SECRET_TTL, usedNets3 );
        util.add( config );
    }


    @Test
    public void testAdd() throws ControlNetworkException
    {
        ControlNetworkUtil util =
                new ControlNetworkUtil( FINGERPRINT, NETWORK, SECRET_KEY, SECRET_TTL, existingConfig );

        ControlNetworkConfig config =
                new ControlNetworkConfig( PEER_ID3, null, FINGERPRINT, SECRET_KEY, SECRET_TTL, usedNets2 );
        util.add( config );

        assertEquals( 3, util.getConfigs().size() );
        assertEquals( "10.200.0.3", util.getConfigs().get( 2 ).getAddress() );
    }


    @Test
    public void testFindFreeNetwork() throws ControlNetworkException
    {
        final List<ControlNetworkConfig> l = new ArrayList<>();
        l.add( config1 );
        l.add( config2 );

        String s = ControlNetworkUtil.findFreeNetwork( l );
        assertEquals( "10.200.2.0", s );
    }

    @Test
    public void testRebuild() throws ControlNetworkException
    {
        final List<ControlNetworkConfig> l = new ArrayList<>();
        l.add( config1 );
        l.add( config2 );
        l.add( config3 );

        String network = ControlNetworkUtil.findFreeNetwork( l );
        assertEquals( "10.200.3.0", network );

        List<ControlNetworkConfig> result = ControlNetworkUtil.rebuild( FINGERPRINT, network,SECRET_KEY, SECRET_TTL, l );
        assertEquals( 3, result.size() );
        assertEquals( "10.200.3.1", result.get(0).getAddress());
        assertEquals( "10.200.3.2", result.get(1).getAddress());
        assertEquals( "10.200.3.3", result.get(2).getAddress());
    }
}
