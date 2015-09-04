package io.subutai.common.environment;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.protocol.Criteria;
import io.subutai.common.protocol.Template;

import static org.junit.Assert.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class CreateContainerGroupRequestTest
{
    private CreateContainerGroupRequest containerGroupRequest;
    private Map<String, String> myMap;
    private List<Template> myList;
    private List<Criteria> myListCriteria;

    @Mock
    Template template;
    @Mock
    Criteria criteria;


    @Before
    public void setUp() throws Exception
    {
        myMap = new HashMap<>();
        myMap.put( "test", "test" );

        myList = new ArrayList<>();
        myList.add( template );

        myListCriteria = new ArrayList<>();
        myListCriteria.add( criteria );

        containerGroupRequest =
                new CreateContainerGroupRequest( myMap, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "555",
                        myList, 5, "Round Robin", myListCriteria, 555 );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertNotNull( containerGroupRequest.getCriteria() );
        assertNotNull( containerGroupRequest.getEnvironmentId() );
        assertNotNull( containerGroupRequest.getInitiatorPeerId() );
        assertNotNull( containerGroupRequest.getIpAddressOffset() );
        assertNotNull( containerGroupRequest.getNumberOfContainers() );
        assertNotNull( containerGroupRequest.getOwnerId() );
        assertNotNull( containerGroupRequest.getPeerIps() );
        assertNotNull( containerGroupRequest.getSubnetCidr() );
        assertNotNull( containerGroupRequest.getTemplates() );
        assertNotNull( containerGroupRequest.getStrategyId() );
        EnvironmentStatus healthy = EnvironmentStatus.HEALTHY;
    }
}