package io.subutai.common.util;


import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Provides utility functions for working with collections
 */
public class CollectionUtil
{
    public static boolean isCollectionEmpty( Collection col )
    {
        return col == null || col.isEmpty();
    }


    public static void retainValues( Set col1, Set col2 )
    {
        if ( col1 != null && col2 != null )
        {
            col1.retainAll( col2 );
        }
    }


    public static void removeValues( Set col1, Set col2 )
    {
        if ( col1 != null && col2 != null )
        {
            col1.removeAll( col2 );
        }
    }


    public static <K, V extends Comparable<? super V>> Map<K, V> sortMapByValueAsc( Map<K, V> map )
    {
        List<Map.Entry<K, V>> list = new LinkedList<>( map.entrySet() );
        Collections.sort( list, new Comparator<Map.Entry<K, V>>()
        {
            public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
            {
                return ( o1.getValue() ).compareTo( o2.getValue() );
            }
        } );

        Map<K, V> result = new LinkedHashMap<>();
        for ( Map.Entry<K, V> entry : list )
        {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    }


    public static <K, V extends Comparable<? super V>> Map<K, V> sortMapByValueDesc( Map<K, V> map )
    {
        List<Map.Entry<K, V>> list = new LinkedList<>( map.entrySet() );
        Collections.sort( list, new Comparator<Map.Entry<K, V>>()
        {
            public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
            {
                return -( o1.getValue() ).compareTo( o2.getValue() );
            }
        } );

        Map<K, V> result = new LinkedHashMap<>();
        for ( Map.Entry<K, V> entry : list )
        {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    }
}
