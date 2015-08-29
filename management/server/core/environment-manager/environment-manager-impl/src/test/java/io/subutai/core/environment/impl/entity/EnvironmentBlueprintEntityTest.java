package io.subutai.core.environment.impl.entity;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import io.subutai.core.environment.impl.entity.EnvironmentBlueprintEntity;

import static junit.framework.TestCase.assertEquals;


public class EnvironmentBlueprintEntityTest
{
    EnvironmentBlueprintEntity entity;


    @Before
    public void setUp() throws Exception
    {
        entity = new EnvironmentBlueprintEntity();
    }


    @Test
    public void testProperties() throws Exception
    {
        UUID id = UUID.randomUUID();
        String info = "info";
        entity.setId( id.toString() );
        entity.setInfo( info );

        assertEquals( info, entity.getInfo() );
        assertEquals( id.toString(), entity.getId() );
    }
}
