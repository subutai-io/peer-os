package org.safehaus.agent.command.model;


/**
 * Created with IntelliJ IDEA.
 * User: akarasulu
 * Date: 8/25/13
 * Time: 2:24 PM
 * To change this template use File | Settings | File Templates.
 */
public enum Type
{
    EXECUTE_REQUEST( "execute-request" ),
    EXECUTE_RESPONSE( "execute-response" ),
    EXECUTION_DONE ( "execute-response-done" );

    private final String name;


    private Type( String name )
    {
        this.name = name;
    }


    private String getName()
    {
        return name;
    }
}
