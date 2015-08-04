package io.subutai.core.communication.api;


import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;


/**
 * Wrapper class for response
 */
public class Response
{
    public static final int OK = 200;
    public static final int METHOD_NOT_ALLOWED = 405;

    private final int STATUS_CODE;
    private final String CONTENT;


    public Response( int statusCode, String content )
    {
        STATUS_CODE = statusCode;
        CONTENT = content;
    }


    public int getStatusCode()
    {
        return STATUS_CODE;
    }


    public String getContent()
    {
        return CONTENT;
    }


    @Override
    public String toString()
    {
        return new ToStringBuilder( this, ToStringStyle.SHORT_PREFIX_STYLE )
                .append( "statusCode", getStatusCode() )
                .append( "content", getContent() )
                .toString();
    }
}
