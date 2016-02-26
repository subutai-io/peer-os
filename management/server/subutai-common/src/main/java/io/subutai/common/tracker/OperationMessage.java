package io.subutai.common.tracker;


public class OperationMessage
{
    public enum Type
    {
        SUCCEEDED, FAILED;
    }


    private final String value;
    private final Type type;


    public OperationMessage( final String value, final Type type )
    {
        this.value = value;
        this.type = type;
    }


    public String getValue()
    {
        return value;
    }


    public Type getType()
    {
        return type;
    }
}
