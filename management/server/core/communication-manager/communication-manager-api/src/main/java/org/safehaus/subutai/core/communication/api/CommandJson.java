/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.communication.api;


import org.safehaus.subutai.common.protocol.Request;
import org.safehaus.subutai.common.protocol.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;


/**
 * This is simple utility class for serializing/deserializing object to/from json.
 */
public class CommandJson
{

    private static final Logger LOG = LoggerFactory.getLogger( CommandJson.class.getName() );
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().addDeserializationExclusionStrategy(
            new SkipNullsExclusionStrategy() ).disableHtmlEscaping().create();


    private CommandJson()
    {

    }


    /**
     * Returns deserialized request from command json string
     *
     * @param json - command in json format
     *
     * @return request
     */
    public static Request getRequestFromCommandJson( String json )
    {

        Command cmd = getCommandFromJson( json );
        if ( cmd != null && cmd.getRequest() != null )
        {
            return cmd.getRequest();
        }

        return null;
    }


    /**
     * Returns deserialized command from command json string
     *
     * @param json - command in json format
     *
     * @return command
     */
    public static Command getCommandFromJson( String json )
    {
        try
        {
            return GSON.fromJson( escape( json ), CommandImpl.class );
        }
        catch ( JsonSyntaxException ex )
        {
            LOG.error( "Error in getCommandFromJson", ex );
        }

        return null;
    }


    /**
     * Escapes symbols in json string
     *
     * @param s - string to escape
     *
     * @return escaped json string
     */
    public static String escape( String s )
    {
        StringBuilder sb = new StringBuilder();
        for ( int i = 0; i < s.length(); i++ )
        {
            char ch = s.charAt( i );
            switch ( ch )
            {
                case '"':
                    sb.append( "\"" );
                    break;
                case '\\':
                    sb.append( "\\" );
                    break;
                case '\b':
                    sb.append( "\b" );
                    break;
                case '\f':
                    sb.append( "\f" );
                    break;
                case '\n':
                    sb.append( "\n" );
                    break;
                case '\r':
                    sb.append( "\r" );
                    break;
                case '\t':
                    sb.append( "\t" );
                    break;
                case '/':
                    sb.append( "\\/" );
                    break;
                default:
                    sb.append( processDefaultCase( ch ) );
            }
        }
        return sb.toString();
    }


    private static String processDefaultCase( char ch )
    {
        StringBuilder sb = new StringBuilder();
        if ( ( ch >= '\u0000' && ch <= '\u001F' ) || ( ch >= '\u007F' && ch <= '\u009F' ) || ( ch >= '\u2000'
                && ch <= '\u20FF' ) )
        {
            String ss = Integer.toHexString( ch );
            sb.append( "\\u" );
            for ( int k = 0; k < 4 - ss.length(); k++ )
            {
                sb.append( '0' );
            }
            sb.append( ss.toUpperCase() );
        }
        else
        {
            sb.append( ch );
        }
        return sb.toString();
    }


    /**
     * Returns deserialized response from command json string
     *
     * @param json - command in json format
     *
     * @return response
     */
    public static Response getResponseFromCommandJson( String json )
    {

        Command cmd = getCommandFromJson( json );
        if ( cmd != null && cmd.getResponse() != null )
        {
            return cmd.getResponse();
        }

        return null;
    }


    /**
     * Returns serialized command from {@code Request}
     *
     * @param request - request
     *
     * @return command in json format
     */
    public static String getRequestCommandJson( Request request )
    {

        return GSON.toJson( new CommandImpl( request ) );
    }


    /**
     * Returns serialized command from {@code Response}
     *
     * @param response - response
     *
     * @return command in json format
     */
    public static String getResponseCommandJson( Response response )
    {

        return GSON.toJson( new CommandImpl( response ) );
    }


    /**
     * Returns serialized command from {@code Command}
     *
     * @param cmd - command
     *
     * @return command in json format
     */
    public static String getCommandJson( Command cmd )
    {

        return GSON.toJson( cmd );
    }


    public static class CommandImpl implements Command
    {

        Request command;
        Response response;


        public CommandImpl( Object message )
        {
            Preconditions.checkArgument( message instanceof Request || message instanceof Response,
                    "Message is of wrong type" );

            if ( message instanceof Request )
            {
                this.command = ( Request ) message;
            }
            else if ( message instanceof Response )
            {
                this.response = ( Response ) message;
            }
        }


        @Override
        public Request getRequest()
        {
            return command;
        }


        @Override
        public Response getResponse()
        {
            return response;
        }


        @Override
        public boolean equals( final Object o )
        {
            if ( this == o )
            {
                return true;
            }
            if ( !( o instanceof CommandImpl ) )
            {
                return false;
            }

            final CommandImpl cmd = ( CommandImpl ) o;

            if ( this.command != null ? !this.command.equals( cmd.command ) : cmd.command != null )
            {
                return false;
            }
            if ( response != null ? !response.equals( cmd.response ) : cmd.response != null )
            {
                return false;
            }

            return true;
        }


        @Override
        public int hashCode()
        {
            int result = command != null ? command.hashCode() : 0;
            result = 31 * result + ( response != null ? response.hashCode() : 0 );
            return result;
        }
    }
}
