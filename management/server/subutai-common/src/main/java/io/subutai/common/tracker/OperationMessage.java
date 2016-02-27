package io.subutai.common.tracker;


public class OperationMessage
{
    public enum Type
    {
        SUCCEEDED, FAILED;
    }


    private final String value;
    private final String description;
    private final Type type;


    public OperationMessage( final String value, final Type type, final String description )
    {
        this.value = value;
        this.type = type;
        this.description = description;
    }


    public String getValue()
    {
        return value;
    }


    public Type getType()
    {
        return type;
    }


    public String getDescription()
    {
        return description;
    }


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "OperationMessage{" );
        sb.append( "value='" ).append( value ).append( '\'' );
        sb.append( ", description='" ).append( description ).append( '\'' );
        sb.append( ", type=" ).append( type );
        sb.append( '}' );
        return sb.toString();
    }
}
