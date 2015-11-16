package io.subutai.common.util;


import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * Provides utility functions for working with JSON <-> POJO conversions
 */
public class JsonUtil
{
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();


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


    public static <T> String toJson( T value )
    {

        return GSON.toJson( value );
    }


    public <T> String to( T value )
    {

        return GSON.toJson( value );
    }


    public <T> String to( T value, Type type )
    {

        return GSON.toJson( value, type );
    }
}
