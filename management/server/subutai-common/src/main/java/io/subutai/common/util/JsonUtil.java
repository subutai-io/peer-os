package io.subutai.common.util;


import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * Provides utility functions for working with JSON <-> POJO conversions
 */
public class JsonUtil
{
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Gson GSON_MINIFIED = new GsonBuilder().create();
    public static final ObjectMapper mapper = new ObjectMapper();


    public static <T, V> String toJson( T key, V value )
    {

        Map<T, V> map = new HashMap<>();
        map.put( key, value );

        return GSON.toJson( map );
    }


    public static <T> T fromJson( String value, Class<T> clazz )
    {
        return GSON.fromJson( value, clazz );
    }


    public static <T> T fromJsonString( String value, Class<T> clazz )
    {
        try
        {
            return mapper.readValue( value, clazz );
        }
        catch ( IOException e )
        {
            throw new IllegalArgumentException(
                    String.format( "Could not convert string %s to %s object: %s", value, clazz, e.getMessage() ) );
        }
    }


    public static <T> T fromJsonString( String value, JavaType type )
    {
        try
        {
            return mapper.readValue( value, type );
        }
        catch ( IOException e )
        {
            throw new IllegalArgumentException(
                    String.format( "Could not convert string %s to %s object: %s", value, type, e.getMessage() ) );
        }
    }


    public static <T> T fromJson( String value, Type type )
    {

        return GSON.fromJson( value, type );
    }


    public <T> T from( String value, Class<T> clazz )
    {

        return GSON.fromJson( value, clazz );
    }


    public <T> T from( String value, Type type )
    {

        return GSON.fromJson( value, type );
    }


    //to


    public static <T> String toJson( T value, Type type )
    {
        return GSON.toJson( value, type );
    }


    public static <T> String toJsonString( T value )
    {
        try
        {
            return mapper.writeValueAsString( value );
        }
        catch ( JsonProcessingException e )
        {
            throw new IllegalArgumentException(
                    String.format( "Illegal argument. Could not convert object %s to JSON string: (%s)", value,
                            e.getMessage() ) );
        }
    }


    public static <T> String toJsonMinified( T value, Type type )
    {

        return GSON_MINIFIED.toJson( value, type );
    }


    public static <T> String toJson( T value )
    {

        return GSON.toJson( value );
    }


    public static <T> String toJsonMinified( T value )
    {

        return GSON_MINIFIED.toJson( value );
    }


    public <T> String to( T value )
    {

        return GSON.toJson( value );
    }


    public <T> String toMinified( T value )
    {

        return GSON_MINIFIED.toJson( value );
    }


    public <T> String to( T value, Type type )
    {

        return GSON.toJson( value, type );
    }


    public <T> String toMinified( T value, Type type )
    {

        return GSON_MINIFIED.toJson( value, type );
    }
}
