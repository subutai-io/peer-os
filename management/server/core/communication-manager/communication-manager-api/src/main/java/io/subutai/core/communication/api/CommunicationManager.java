package io.subutai.core.communication.api;


import java.net.URI;
import java.util.List;


/**
 * Created by tzhamakeev on 8/5/15.
 */
public interface CommunicationManager
{
    String getSender();

    List<String> getRecipients();

    String post( URI uri, String recipientKeyId, String data ) throws CommunicationException;
}
