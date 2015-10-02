package io.subutai.core.registration.api.service;


import java.sql.Timestamp;


public interface ContainerToken
{
    public String getToken();

    public Timestamp getDateCreated();

    public Long getTtl();

    public String getHostId();
}
