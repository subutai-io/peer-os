package org.safehaus.subutai.common;


import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class JsonUtil {

    public static final Gson GSON =
            new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();


    public static String toJson( Object key, Object value ) {

        HashMap<Object, Object> map = new HashMap();
        map.put( key, value );

        return GSON.toJson( map );
    }
}
