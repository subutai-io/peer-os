package org.safehaus.subutai.common.environment;


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
import org.safehaus.subutai.common.protocol.Criteria;
import org.safehaus.subutai.common.protocol.Template;

import static org.junit.Assert.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class CreateContainerGroupRequestTest
{
    private CreateContainerGroupRequest containerGroupRequest;
    private Set<String> mySet;
    private List<Template> myList;
    private List<Criteria> myListCriteria;

    @Mock
    Template template;
    @Mock
    Criteria criteria;


    @Before
    public void setUp() throws Exception
    {
        mySet = new HashSet<>();
        mySet.add( "test" );

        myList = new ArrayList<>();
        myList.add( template );

        myListCriteria = new ArrayList<>();
        myListCriteria.add( criteria );

        containerGroupRequest =
                new CreateContainerGroupRequest( mySet, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "555",
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