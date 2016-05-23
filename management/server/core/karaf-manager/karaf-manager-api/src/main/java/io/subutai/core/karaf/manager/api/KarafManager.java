package io.subutai.core.karaf.manager.api;


import javax.annotation.security.RolesAllowed;


/**
 * Karaf Manager
 */
public interface KarafManager
{
    String executeShellCommand( String commandStr );

    /* *************
        */
    @RolesAllowed( {"Karaf-Server-Administration|Write","Karaf-Server-Administration|Read",
                    "System-Management|Write", "System-Management|Update" } )
    String executeJMXCommand( String commandStr );
}
