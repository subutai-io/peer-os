package org.safehaus.subutai.core.identity.api;


import java.io.Serializable;


/**
 * Created by talas on 3/13/15.
 */
public interface CliCommand extends Serializable
{
    public String getScope();

    public String getName();
}
