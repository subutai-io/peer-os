/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.communication.api;


import java.util.logging.Level;
import java.util.logging.Logger;

import org.safehaus.subutai.common.protocol.Request;
import org.safehaus.subutai.common.protocol.Response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * This is simple utility class for serializing/deserializing object to/from json.
 */
public class CommandJson
{

    private static final Logger LOG = Logger.getLogger( CommandJson.class.getName() );
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().addDeserializationExclusionStrategy(
            new SkipNullsExclusionStrategy() ).disableHtmlEscaping().create();


    /**
     * Returns deserialized request from json string
     *
     * @param json - request in json format
     *
     * @return request
     */
    public static Request getRequest( String json )
    {
        try
        {
            Command cmd = gson.fromJson( escape( json ), CommandImpl.class );
            if ( cmd.getRequest() != null )
            {
                return cmd.getRequest();
            }
        }
        catch ( Exception ex )
        {
            LOG.log( Level.SEVERE, "Error in getRequest", ex );
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
    private static String escape( String s )
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
                    if ( ( ch >= '\u0000' && ch <= '\u001F' ) || ( ch >= '\u007F' && ch <= '\u009F' ) || (
                            ch >= '\u2000' && ch <= '\u20FF' ) )
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
            }
        }
        return sb.toString();
    }


    /**
     * Returns deserialized response from json string
     *
     * @param json - response in json format
     *
     * @return response
     */
    public static Response getResponse( String json )
    {
        try
        {
            Command cmd = gson.fromJson( escape( json ), CommandImpl.class );
            if ( cmd.getResponse() != null )
            {
                return cmd.getResponse();
            }
        }
        catch ( Exception ex )
        {
            LOG.log( Level.SEVERE, "Error in getResponse", ex );
        }

        return null;
    }


    /**
     * Returns deserialized command from json string
     *
     * @param json - command in json format
     *
     * @return command
     */
    public static Command getCommand( String json )
    {
        try
        {
            return gson.fromJson( escape( json ), CommandImpl.class );
        }
        catch ( Exception ex )
        {
            LOG.log( Level.SEVERE, "Error in getCommand", ex );
        }

        return null;
    }


    /**
     * Returns serialized request from Request POJO
     *
     * @param cmd - request in pojo format
     *
     * @return request in json format
     */
    public static String getJson( Request cmd )
    {
        try
        {
            return gson.toJson( new CommandImpl( cmd ) );
        }
        catch ( Exception ex )
        {
            LOG.log( Level.SEVERE, "Error in getJson", ex );
        }
        return null;
    }


    /**
     * Returns serialized response from Response POJO
     *
     * @param cmd - response in pojo format
     *
     * @return response in json format
     */
    public static String getResponse( Response cmd )
    {
        try
        {
            return gson.toJson( new CommandImpl( cmd ) );
        }
        catch ( Exception ex )
        {
            LOG.log( Level.SEVERE, "Error in getResponse", ex );
        }
        return null;
    }


    /**
     * Returns serialized command from Command POJO
     *
     * @param cmd - command in pojo format
     *
     * @return request in command format
     */
    public static String getJson( Command cmd )
    {
        try
        {
            return gson.toJson( cmd );
        }
        catch ( Exception ex )
        {
            LOG.log( Level.SEVERE, "Error in getJson", ex );
        }
        return null;
    }


    /**
     * Returns serialized agent from Agent POJO
     *
     * @param agent - agent in pojo format
     *
     * @return agent in json format
     */
    /*public static String getAgentJson( Object agent ) {
        try {
            return gson.toJson( agent );
        }
        catch ( Exception ex ) {
            LOG.log( Level.SEVERE, "Error in getAgentJson", ex );
        }
        return null;
    }*/


    /**
     * Returns deserialized agent from Agent json
     *
     * \@param json - agent in json format
     *
     * \@return agent in pojo format
     */
    /*static Agent getAgent( String json ) {
        try {
            Agent agent = gson.fromJson( escape( json ), Agent.class );
            if ( agent != null ) {
                return agent;
            }
        }
        catch ( Exception ex ) {
            LOG.log( Level.SEVERE, "Error in getAgent", ex );
        }

        return null;
    }*/


    private static class CommandImpl implements Command
    {

        Request command;
        Response response;


        public CommandImpl( Object message )
        {
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
    }
}
