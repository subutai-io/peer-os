package io.subutai.core.strategy.impl;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.environment.Blueprint;
import io.subutai.common.environment.NodeSchema;

import static org.junit.Assert.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class BlueprintTest
{
    private Blueprint blueprint;
    private List<NodeSchema> mySet;

    @Mock
    NodeSchema node;


    @Before
    public void setUp() throws Exception
    {
        mySet = new ArrayList<>();
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