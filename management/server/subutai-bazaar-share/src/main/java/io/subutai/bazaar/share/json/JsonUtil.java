package io.subutai.bazaar.share.json;


import java.io.IOException;
import java.util.Collection;

import org.apache.commons.lang3.ArrayUtils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;


/**
 * https://github.com/FasterXML/jackson
 */
public class JsonUtil
{
    private static final JsonNodeFactory FACTORY = new JsonNodeFactory( false );

    private static ObjectMapper CBOR_MAPPER = createMapper( new CBORFactory() );

    public static final ObjectMapper MAPPER = createMapper( null );


    private JsonUtil()
    {
        throw new IllegalAccessError( "Utility class" );
    }


    public static ObjectNode objectNode()
    {
        return FACTORY.objectNode();
    }


    public static ObjectNode createNode( String key, Object value )
    {
        return objectNode().put( key, value.toString() );
    }


    private static ObjectMapper createMapper( JsonFactory factory )
    {
        ObjectMapper mapper = new ObjectMapper( factory );

        mapper.setVisibility( PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY );

        return mapper;
    }


    public static String toJson( Object value ) throws JsonProcessingException
    {
        return MAPPER.writeValueAsString( value );
    }


    public static <T> T fromJson( String json, Class<T> type ) throws IOException
    {
        return MAPPER.readValue( json, type );
    }


    public static byte[] toCbor( Object value ) throws JsonProcessingException
    {
        return CBOR_MAPPER.writeValueAsBytes( value );
    }


    public static <T> T fromCbor( byte data[], Class<T> type ) throws IOException
    {
        return ArrayUtils.isNotEmpty( data ) ? CBOR_MAPPER.readValue( data, type ) : null;
    }


    public static void addAll( ArrayNode arr, Collection<String> list )
    {
        if ( arr == null || list == null || list.isEmpty() )
        {
            return;
        }

        for ( String s : list )
        {
            arr.add( s );
        }
    }
}
