package io.subutai.common.util;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * Provides utility functions for working with collections
 */
public class CollectionUtil


{
    private CollectionUtil()
    {
        throw new IllegalAccessError( "Utility class" );
    }


    public static boolean isCollectionEmpty( Collection col )
    {
        return col == null || col.isEmpty();
    }


    public static <T extends Comparable<? super T>> List<T> asSortedList( Collection<T> c )
    {
        List<T> list = new ArrayList<>( c );
        java.util.Collections.sort( list );
        return list;
    }


    public static boolean isMapEmpty( Map map )
    {
        return map == null || map.isEmpty();
    }
}
