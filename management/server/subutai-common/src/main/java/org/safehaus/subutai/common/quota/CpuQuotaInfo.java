package org.safehaus.subutai.common.quota;


import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * Created by talas on 12/2/14.
 */
public class CpuQuotaInfo extends QuotaInfo
{
    private Set<Integer> indexSet = new HashSet<>();


    public CpuQuotaInfo( String cpuIndexes )
    {
        if ( cpuIndexes.equals( "none" ) )
        {
            return;
        }
        cpuIndexes = cpuIndexes.replace( "\n", "" );
        String[] ranges = cpuIndexes.split( "," );
        for ( String range : ranges )
        {
            range = range.replace( " ", "" );
            String[] neighbour = range.split( "-" );
            Integer start = Integer.parseInt( neighbour[0] );
            Integer end = Integer.parseInt( neighbour[neighbour.length - 1] );
            for (; start <= end; start++ )
            {
                indexSet.add( start );
            }
        }
    }


    public CpuQuotaInfo( final Set<Integer> indexSet )
    {
        this.indexSet = indexSet;
    }


    public Set<Integer> getIndexSet()
    {
        return indexSet;
    }


    public void setIndexSet( final Set<Integer> indexSet )
    {
        this.indexSet = indexSet;
    }


    public void addCpuIndex( Integer index )
    {
        indexSet.add( index );
    }


    @Override
    public String getQuotaValue()
    {
        StringBuilder formattedString = new StringBuilder();
        Iterator<Integer> it = indexSet.iterator();
        formattedString.append( String.valueOf( it.next() ) );
        while ( it.hasNext() )
        {
            formattedString.append( ", " ).append( String.valueOf( it.next() ) );
        }
        if ( formattedString.length() == 0 )
        {
            formattedString.append( "none" );
        }
        return formattedString.toString();
    }


    @Override
    public String getQuotaKey()
    {
        return "cpu.cpus";
    }
}
