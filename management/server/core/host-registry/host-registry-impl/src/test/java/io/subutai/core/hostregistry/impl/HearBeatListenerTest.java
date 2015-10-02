package io.subutai.core.hostregistry.impl;


import java.io.PrintStream;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.util.JsonUtil;
import io.subutai.common.host.ResourceHostInfo;

import io.subutai.core.broker.api.Topic;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class HearBeatListenerTest
{
    private static final String HOST_HOSTNAME = "host";
    private static final String HOST_ID = UUID.randomUUID().toString();
    private static final String HOST_IP = "127.0.0.2";
    private static final String HOST_MAC_ADDRESS = "0c:8b:fd:c0:ea:fe";
    private static final String CONTAINER_HOSTNAME = "container";
    private static final String CONTAINER_ID = UUID.randomUUID().toString();
    private static final String CONTAINER_IP = "127.0.0.1";
    private static final ContainerHostState CONTAINER_STATUS = ContainerHostState.FROZEN;
    private static final String INFO_JSON = String.format(
            "{ \"response\" : {\"type\":\"HEARTBEAT\", \"hostname\":\"%s\", \"id\":\"%s\", \"ips\" : [\"%s\"]," +
                    "\"macAddress\": \"%s\", "
                    + "\"containers\": [{ \"hostname\":\"%s\", \"id\":\"%s\", \"ips\" : [\"%s\"], " +
                    "\"status\":\"%s\" }]}}", HOST_HOSTNAME, HOST_ID, HOST_IP, HOST_MAC_ADDRESS, CONTAINER_HOSTNAME,
            CONTAINER_ID, CONTAINER_IP, CONTAINER_STATUS );
    private byte[] message;

    @Mock
    HostRegistryImpl containerRegistry;
    @Mock
    JsonUtil jsonUtil;

    HeartBeatListener heartBeatListener;


    @Before
    public void setUp() throws Exception
    {
        heartBeatListener = new HeartBeatListener( containerRegistry );
        message = INFO_JSON.getBytes( "UTF-8" );
    }


    @Test
    public void testGetTopic() throws Exception
    {
        assertEquals( Topic.HEARTBEAT_TOPIC, heartBeatListener.getTopic() );
    }


    @Test
    public void testOnMessage() throws Exception
    {
        heartBeatListener.onMessage( message );

        verify( containerRegistry ).registerHost( isA( ResourceHostInfo.class ) );


        heartBeatListener.jsonUtil = jsonUtil;
        RuntimeException exception = mock( RuntimeException.class );
        doThrow( exception ).when( jsonUtil ).from( anyString(), eq( HeartBeat.class ) );

        heartBeatListener.onMessage( message );

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }
}
