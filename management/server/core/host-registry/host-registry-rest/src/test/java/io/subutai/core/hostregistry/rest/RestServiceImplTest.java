package io.subutai.core.hostregistry.rest;


import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostArchitecture;
import io.subutai.common.settings.Common;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.core.hostregistry.api.ResourceHostInfo;
import io.subutai.core.hostregistry.impl.ResourceHostInfoImpl;

import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class RestServiceImplTest
{

    private static final String HOST_HOSTNAME = "host";
    private static final UUID HOST_ID = UUID.randomUUID();
    private static final String HOST_IP = "127.0.0.2";
    private static final String HOST_MAC_ADDRESS = "0c:8b:fd:c0:ea:fe";
    private static final String CONTAINER_HOSTNAME = "container";
    private static final UUID CONTAINER_ID = UUID.randomUUID();
    private static final String CONTAINER_IP = "127.0.0.1";
    private static final String CONTAINER_INTERFACE = "eth0";
    private static final String CONTAINER_MAC_ADDRESS = "0c:8b:fd:c0:ea:fe";
    private static final ContainerHostState CONTAINER_STATUS = ContainerHostState.FROZEN;
    private static final HostArchitecture ARCH = HostArchitecture.AMD64;

    private static final String INFO_JSON =
            String.format( "{\"type\":\"HEARTBEAT\", \"hostname\":\"%s\", \"id\":\"%s\", \"arch\":\"%s\", " +
                            "\"interfaces\" : [{ \"interfaceName\":\"%s\", \"ip\":\"%s\",\"mac\":\"%s\"}], "
                            + "\"containers\": [{ \"hostname\":\"%s\", \"id\":\"%s\", " +
                            "\"interfaces\" : [{ \"interfaceName\":\"%s\", \"ip\":\"%s\",\"mac\":\"%s\"}], " +
                            "\"status\":\"%s\" , \"arch\":\"%s\"}]}", HOST_HOSTNAME, HOST_ID, ARCH,
                    Common.DEFAULT_CONTAINER_INTERFACE, HOST_IP, HOST_MAC_ADDRESS, CONTAINER_HOSTNAME, CONTAINER_ID,
                    CONTAINER_INTERFACE, CONTAINER_IP, CONTAINER_MAC_ADDRESS, CONTAINER_STATUS, ARCH );

    @Mock
    HostRegistry hostRegistry;

    RestServiceImpl restService;


    @Before
    public void setUp() throws Exception
    {
        Set<ResourceHostInfo> hosts = Sets.newHashSet();
        hosts.add( JsonUtil.fromJson( INFO_JSON, ResourceHostInfoImpl.class ) );
        restService = new RestServiceImpl( hostRegistry );
        when( hostRegistry.getResourceHostsInfo() ).thenReturn( hosts );
    }


    @Test
    public void testGetHosts() throws Exception
    {
        Response response = restService.getHosts();

        Set<ResourceHostInfo> hosts =
                JsonUtil.fromJson( response.getEntity().toString(), new TypeToken<Set<ResourceHostInfoImpl>>()
                {}.getType() );

        assertTrue( hosts.contains( JsonUtil.fromJson( INFO_JSON, ResourceHostInfoImpl.class ) ) );
    }
}
