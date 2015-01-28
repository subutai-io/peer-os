package org.safehaus.subutai.common.quota;


import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by talas on 12/2/14.
 */
public class Memory
{
    MemoryUnit unit;
    Long value;


    public Memory( String memory )
    {
        memory = memory.replace( "\n", "" );
        Pattern p = Pattern.compile( "-?\\d+" );

        Matcher m = p.matcher( memory );

        if ( m.find() )
        {
            if ( m.end() < memory.length() && ( !memory.contains( "e" ) && !memory.contains( "E" ) ) )
            {
                unit = MemoryUnit.getMemoryUnit( memory.substring( m.end() ) );
                value = Long.valueOf( m.group() );
            }
            else
            {
                unit = MemoryUnit.BYTES;
                value = Double.valueOf( memory ).longValue();
            }
        }
        else
        {
            unit = MemoryUnit.NONE;
            value = 0L;
        }
    }


    public Memory( final MemoryUnit unit, final Long value )
    {
        this.unit = unit;
        this.value = value;
    }


    public MemoryUnit getUnit()
    {
        return unit;
    }


    public Long getValue()
    {
        return value;
    }


    @Override
    public String toString()
    {
        if ( unit == MemoryUnit.NONE || value == null )
        {
            return unit.getAcronym();
        }
        else if ( unit == MemoryUnit.BYTES )
        {
            return String.valueOf( value );
        }
        else
        {
            return String.valueOf( value ) + unit.getAcronym();
        }
    }
}
