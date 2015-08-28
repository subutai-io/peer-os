package io.subutai.core.registration.api.service;


import java.sql.Timestamp;


/**
 * Created by talas on 8/28/15.
 */
public interface ContainerToken
{
    public String getSecret();

    public Timestamp getDateCreated();

    public Long getTtl();

    public String getHostId();
}
