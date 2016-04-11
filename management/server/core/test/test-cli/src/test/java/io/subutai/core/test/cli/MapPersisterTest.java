package io.subutai.core.test.cli;


import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Maps;


public class MapPersisterTest
{
    MapPersister mapPersister;


    @Before
    public void setUp() throws Exception
    {
        mapPersister = new MapPersister();
    }


    @Test
    public void testSerializeMap() throws Exception
    {
        Map<String, Object> map = Maps.newHashMap();

        map.put( "test", "test" );
        map.put( "key", null );
        map.put( null, "value" );

        mapPersister.serialize( map, "testMap" );

        Map<String, Object> map2 = mapPersister.deserialize( "testMap" );

        for ( Map.Entry<String, Object> mapEntry : map2.entrySet() )
        {
            System.out.format( "%s->%s%n", mapEntry.getKey(), mapEntry.getValue() );
        }
    }


    @Test
    public void testSerializeMapOfMaps() throws Exception
    {
        Map<String, String> map = Maps.newHashMap();
        Map<String, Object> map2 = Maps.newHashMap();

        map.put( "test", "test" );
        map.put( "key", null );
        map.put( null, "value" );

        map2.put( "map", map );

        mapPersister.serialize( map2, "testMap2" );

        map2 = mapPersister.deserialize( "testMap2" );

        map = ( Map<String, String> ) map2.get( "map" );

        for ( Map.Entry<String, String> mapEntry : map.entrySet() )
        {
            System.out.format( "%s->%s%n", mapEntry.getKey(), mapEntry.getValue() );
        }
    }
}
