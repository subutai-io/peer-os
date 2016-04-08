package io.subutai.common.util;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Provides utility functions for working with collections
 */
public class CollectionUtil
{
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
}
