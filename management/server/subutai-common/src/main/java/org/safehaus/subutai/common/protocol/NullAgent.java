package org.safehaus.subutai.common.protocol;


import java.util.ArrayList;
import java.util.UUID;


/**
 * Used to define a null Agent.
 */
public class NullAgent extends Agent
{

    public static final String NULL_UUID = "44e128a5-ac7a-4c9a-be4c-224b6bf81b14";
    private static NullAgent instance = new NullAgent();


    private NullAgent()
    {
        super( UUID.fromString( NULL_UUID ), "NULL_AGENT", "NULL_PARENT", "NULL_MAC", new ArrayList<String>(), false,
                "NULL_TRANSPORT_ID" );
    }


    public static NullAgent getInstance()
    {
        return instance;
    }
}
