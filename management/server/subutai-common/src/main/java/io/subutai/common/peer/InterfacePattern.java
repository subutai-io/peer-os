package io.subutai.common.peer;


import javax.xml.bind.annotation.XmlRootElement;


/**
 * JSON request parameter
 */
@XmlRootElement
public class InterfacePattern
{
    private String field;
    private String pattern;


    public InterfacePattern()
    {
    }


    public InterfacePattern( final String field, final String pattern )
    {
        this.field = field;
        this.pattern = pattern;
    }


    public String getPattern()
    {
        return pattern;
    }


    public void setPattern( final String pattern )
    {
        this.pattern = pattern;
    }


    public String getField()
    {
        return field;
    }


    public void setField( final String field )
    {
        this.field = field;
    }


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "InterfaceRequest{" );
        sb.append( "field='" ).append( field ).append( '\'' );
        sb.append( ", pattern='" ).append( pattern ).append( '\'' );
        sb.append( '}' );
        return sb.toString();
    }
}
