package io.subutai.common.environment;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.environment.Blueprint;
import io.subutai.common.environment.NodeGroup;

import static org.junit.Assert.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class BlueprintTest
{
    private Blueprint blueprint;
    private Set<NodeGroup> mySet;

    @Mock
    NodeGroup nodeGroup;

    @Before
    public void setUp() throws Exception
    {
        mySet = new HashSet<>(  );
        mySet.add( nodeGroup );

        blueprint = new Blueprint( "test", mySet );
        blueprint.setId( UUID.randomUUID() );
    }


   @Test
    public void testProperties() throws Exception
    {
        assertNotNull( blueprint.getId() );
        assertNotNull( blueprint.getName() );
        assertNotNull( blueprint.getNodeGroups() );
    }
}