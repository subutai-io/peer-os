/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.communication.impl;


import java.util.UUID;

import org.safehaus.subutai.common.enums.OutputRedirection;
import org.safehaus.subutai.common.enums.RequestType;
import org.safehaus.subutai.common.protocol.Request;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.protocol.ResponseListener;


/**
 * Test utilities
 */
public class TestUtils
{

    public static Request getRequestTemplate( UUID uuid )
    {
        return new Request( "SOURCE", RequestType.EXECUTE_REQUEST, // type
                uuid, //                        !! agent uuid
                UUID.randomUUID(), //                        !! task uuid
                1, //                           !! request sequence number
                "/", //                         cwd
                "pwd", //                        program
                OutputRedirection.RETURN, //    std output redirection
                OutputRedirection.RETURN, //    std error redirection
                null, //                        stdout capture file path
                null, //                        stderr capture file path
                "root", //                      runas
                null, //                        arg
                null, //                        env vars
                null, 30 ); //
    }


    public static ResponseListener getResponseListener()
    {

        return new ResponseListener()
        {

            public void onResponse( Response response )
            {

            }
        };
    }
}
