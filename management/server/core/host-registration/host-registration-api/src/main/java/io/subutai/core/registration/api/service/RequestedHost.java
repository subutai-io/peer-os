package io.subutai.core.registration.api.service;


import java.util.Set;

import io.subutai.common.host.HostArchitecture;
import io.subutai.core.registration.api.ResourceHostRegistrationStatus;


public interface RequestedHost
{
    String getId();

    String getHostname();

    Set<ContainerInfo> getHostInfos();

    HostArchitecture getArch();

    ResourceHostRegistrationStatus getStatus();

    String getPublicKey();

    String getSecret();

    String getCert();

    String getAddress();
}
