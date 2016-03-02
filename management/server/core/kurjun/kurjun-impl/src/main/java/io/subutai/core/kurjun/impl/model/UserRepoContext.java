package io.subutai.core.kurjun.impl.model;


import java.io.Serializable;

import ai.subut.kurjun.common.service.KurjunContext;


public class UserRepoContext extends KurjunContext implements Serializable
{

    private final String ownerFprint;


    public UserRepoContext( String ownerFprint, String repoName )
    {
        super( repoName );
        this.ownerFprint = ownerFprint;
    }


    public String getOwnerFprint()
    {
        return ownerFprint;
    }

}
