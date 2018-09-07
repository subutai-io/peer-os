package io.subutai.bazaar.share.json;


import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.subutai.bazaar.share.dto.RegistrationDto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * https://github.com/FasterXML/jackson
 */
public class JsonUtilTest
{
    @Test
    public void testCbor() throws IOException
    {
        RegistrationDto reg = new RegistrationDto( "6831eaf43361e3aa116abc1541c24197044fd723" );

        byte[] data = JsonUtil.toCbor( reg );

        RegistrationDto reg2 = JsonUtil.fromCbor( data, RegistrationDto.class );

        assertEquals( reg.getOwnerFingerprint(), reg2.getOwnerFingerprint() );
    }


    @Test
    public void testGetObjectNode()
    {
        ObjectNode person = JsonUtil.objectNode();

        assertNotNull( person );

        person.put( "name", "Subutai" );
        person.put( "age", 50 );
        person.put( "legend", true );

        assertEquals( "Subutai", person.get( "name" ).asText() );

        assertEquals( 50, person.get( "age" ).asInt() );

        assertEquals( true, person.get( "legend" ).asBoolean() );
    }


    @Test
    public void testMapper() throws IOException
    {
        JsonNode json = JsonUtil.MAPPER.readTree( "{}" );

        assertNotNull( json );

        assertEquals( 0, json.size() );
    }


    @Test
    public void testCreate() throws IOException
    {
        ObjectNode person = JsonUtil.createNode( "name", "Subutai" );

        assertNotNull( person );

        assertEquals( "Subutai", person.get( "name" ).asText() );
    }


    @Test
    public void testRead() throws IOException
    {
        JsonNode json = JsonUtil.MAPPER.readTree( "{}" );

        assertNotNull( json );

        assertEquals( 0, json.size() );
    }


    @Test
    public void testAddAll()
    {
        ObjectNode json = JsonUtil.objectNode();
        ArrayNode arr = json.putArray( "arr" );

        Set<String> set = new HashSet<>();
        set.add( "one" );
        set.add( "two" );

        JsonUtil.addAll( arr, set );

        assertTrue( json.toString().contains( "arr" ) );
        assertTrue( json.toString().contains( "one" ) );
        assertTrue( json.toString().contains( "two" ) );
    }
}
