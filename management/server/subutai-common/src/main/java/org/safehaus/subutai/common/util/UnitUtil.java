package org.safehaus.subutai.common.util;


import com.google.common.base.Preconditions;


public class UnitUtil
{
    public static enum Unit
    {
        B, KB, MB, GB
    }


    public double convert( Double value, Unit from, Unit to )
    {
        Preconditions.checkArgument( value > 0, "Value must be greater than 0" );
        Preconditions.checkNotNull( from );
        Preconditions.checkNotNull( to );

        switch ( from )
        {
            case B:
                return convertFromBytes( value, to );
            case KB:
                return convertFromKB( value, to );
            case MB:
                return convertFromMB( value, to );
            default:
                return convertFromGB( value, to );
        }
    }


    private double convertFromGB( final Double value, final Unit to )
    {
        switch ( to )
        {
            case B:
                return value * 1024 * 1024 * 1024;
            case KB:
                return value * 1024 * 1024;
            case MB:
                return value * 1024;
            default:
                return value;
        }
    }


    private double convertFromMB( final Double value, final Unit to )
    {
        switch ( to )
        {
            case B:
                return value * 1024 * 1024;
            case KB:
                return value * 1024;
            case MB:
                return value;
            default:
                return value / 1024;
        }
    }


    private double convertFromKB( final Double value, final Unit to )
    {
        switch ( to )
        {
            case B:
                return value * 1024;
            case KB:
                return value;
            case MB:
                return value / 1024;
            default:
                return value / ( 1024 * 1024 );
        }
    }


    private double convertFromBytes( final Double value, final Unit to )
    {
        switch ( to )
        {
            case B:
                return value;
            case KB:
                return value / 1024;
            case MB:
                return value / ( 1024 * 1024 );
            default:
                return value / ( 1024 * 1024 * 1024 );
        }
    }
}
