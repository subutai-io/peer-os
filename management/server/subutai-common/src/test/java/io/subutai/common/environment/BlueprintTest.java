package io.subutai.common.environment;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class BlueprintTest
{
    private Blueprint blueprint;
    private Set<Node> mySet;

    @Mock
    Node node;

    @Before
    public void setUp() throws Exception
    {
        mySet = new HashSet<>(  );
        mySet.add( node );

        blueprint = new Blueprint( "test", mySet );
        blueprint.setId( UUID.randomUUID() );
    }


   @Test
    public void testProperties() throws Exception
    {
        assertNotNull( blueprint.getId() );
        assertNotNull( blueprint.getName() );
        assertNotNull( blueprint.getNodes() );
    }
}