package io.subutai.common.metric;


/**
 * String alert value
 */
public class StringAlertValue implements AlertValue
{
    String value;


    public StringAlertValue( final String value )
    {
        this.value = value;
    }


    public String getValue()
    {
        return value;
    }


    @Override
    public boolean validate()
    {
        return value != null && value.trim().length() > 0;
    }


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "StringAlertValue{" );
        sb.append( "value='" ).append( value ).append( '\'' );
        sb.append( '}' );
        return sb.toString();
    }
}
