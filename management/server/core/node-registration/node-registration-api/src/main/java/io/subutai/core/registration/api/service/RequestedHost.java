package io.subutai.core.registration.api.service;


import java.util.Set;

import io.subutai.common.host.HostArchitecture;
import io.subutai.common.host.Interface;
import io.subutai.core.registration.api.RegistrationStatus;


/**
 * Created by talas on 8/24/15.
 */
public interface RequestedHost
{
    public String getId();

    public String getHostname();

    public Set<Interface> getNetInterfaces();

    public Set<ContainerInfo> getHostInfoSet();

    public HostArchitecture getArch();

    public RegistrationStatus getStatus();

    public String getPublicKey();

    public String getRestHook();

    public void setRestHook( String restHook );

    public String getSecret();
}
