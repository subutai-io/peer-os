package org.safehaus.subutai.core.peer.impl.command;


import org.safehaus.subutai.common.command.ResponseType;

import com.google.common.collect.Sets;


/**
 * TODO remove after migration to new agent architecture
 */
public class TempResponseConverter
{

    public static ResponseImpl convertResponse( org.safehaus.subutai.common.protocol.Response response )
    {

        ResponseType responseType = null;
        switch ( response.getType() )
        {
            case EXECUTE_RESPONSE:
            case EXECUTE_RESPONSE_DONE:
                responseType = ResponseType.EXECUTE_RESPONSE;
                break;
            case EXECUTE_TIMEOUT:
                responseType = ResponseType.EXECUTE_TIMEOUT;
                break;
            case IN_QUEUE:
                responseType = ResponseType.IN_QUEUE;
                break;
            case TERMINATE_RESPONSE_DONE:
            case TERMINATE_RESPONSE_FAILED:
                responseType = ResponseType.TERMINATE_RESPONSE;
                break;
            case INOTIFY_ACTION_RESPONSE:
                responseType = ResponseType.INOTIFY_EVENT;
                break;
            case INOTIFY_LIST_RESPONSE:
                responseType = ResponseType.LIST_INOTIFY_RESPONSE;
                break;
        }

        if ( responseType != null )
        {
            return new ResponseImpl( responseType, response.getUuid(), response.getTaskUuid(), response.getPid(),
                    response.getResponseSequenceNumber(), response.getStdOut(), response.getStdErr(),
                    response.getExitCode(), response.getConfPoints() == null ? Sets.<String>newHashSet() :
                                            Sets.newHashSet( response.getConfPoints() ) );
        }
        return null;
    }
}
