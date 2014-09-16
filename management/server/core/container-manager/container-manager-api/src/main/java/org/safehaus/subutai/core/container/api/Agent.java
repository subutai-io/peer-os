package org.safehaus.subutai.core.container.api;

/**
 * Created by timur on 9/13/14.
 */
public interface Agent {
    public String getHostname();

    public byte[] getHardwareAddress();

    public ContainerType getType();
}
