package io.subutai.core.communication.api;


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
        final StringBuffer sb = new StringBuffer( "Response{" );
        sb.append( "STATUS_CODE=" ).append( STATUS_CODE );
        sb.append( ", CONTENT='" ).append( CONTENT ).append( '\'' );
        sb.append( '}' );
        return sb.toString();
    }
}
