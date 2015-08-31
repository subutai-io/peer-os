package io.subutai.core.registration.api.service;


import java.util.Set;

import io.subutai.common.host.HostArchitecture;
import io.subutai.common.host.Interface;


/**
 * Created by talas on 8/24/15.
 */
public interface VirtualHost
{
    public String getId();

    public String getHostname();

    public Set<Interface> getInterfaces();

    public HostArchitecture getArch();
}
