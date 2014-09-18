package org.safehaus.subutai.common.util;


import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * Provides utility functions for working with JSON <-> POJO conversions
 */
public class JsonUtil {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();


    public static String toJson( Object key, Object value )
    {

        Map map = new HashMap();
        map.put( key, value );

        return GSON.toJson( map );
    }


    public static String toJson( Object value )
    {

        return GSON.toJson( value );
    }


    public static <T> T fromJson( String value, Class<T> clazz )
    {

        return GSON.fromJson( value, clazz );
    }


    public static <T> T fromJson( String value, Type type )
    {

        return GSON.fromJson( value, type );
    }
}
