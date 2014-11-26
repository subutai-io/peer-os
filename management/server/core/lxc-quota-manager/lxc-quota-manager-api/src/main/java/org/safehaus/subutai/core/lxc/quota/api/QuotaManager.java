package org.safehaus.subutai.core.lxc.quota.api;


/**
 * Created by talas on 10/7/14.
 */
public interface QuotaManager
{
    /**
     * Set Quota for container specified with parameters passed parameter as value to change, it is enum and specific
     * value specified in enum key and newValue can be in any format for setting new value. host is a host with
     * collection of container we intend to modify
     */
    public void setQuota( String containerName, QuotaEnum parameter, String newValue ) throws QuotaException;

    /**
     * Set quota for a container in a host with parameter specified in QuotaEnum.getKey()
     */
    public String getQuota( String containerName, QuotaEnum parameter ) throws QuotaException;
}
