package io.subutai.core.peer.rest;


import javax.xml.bind.annotation.XmlRootElement;


/**
 * JSON request parameter
 */
@XmlRootElement
public class InterfaceRequest
{
    private String field;
    private String pattern;


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
