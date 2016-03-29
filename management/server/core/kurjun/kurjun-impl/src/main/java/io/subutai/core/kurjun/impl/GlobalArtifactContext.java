package io.subutai.core.kurjun.impl;


import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ai.subut.kurjun.common.service.KurjunContext;
import ai.subut.kurjun.model.repository.RemoteRepository;
import io.subutai.core.kurjun.api.RepositoryContext;



public class GlobalArtifactContext implements RepositoryContext
{

    private Map<String, KurjunContext> map;


    private Set<RemoteRepository> remoteTemplate;
    private Set<RemoteRepository> remoteApt;
    private Set<RemoteRepository> remoteRaw;


    public GlobalArtifactContext()
    {
        this.remoteTemplate = new HashSet<>();
        this.remoteApt = new HashSet<>();
        this.remoteRaw = new HashSet<>();
    }


    @Override
    public void addRemoteTemplateRepository( RemoteRepository remoteRepository )
    {
        this.remoteTemplate.add( remoteRepository );
    }


    @Override
    public Set<RemoteRepository> getRemoteRawRepositories()
    {
        return this.remoteRaw;
    }


    @Override
    public void addRemoteRawRepositories( final RemoteRepository remoteRepository )
    {
        this.remoteRaw.add( remoteRepository );
    }


    @Override
    public Set<RemoteRepository> getRemoteAptRepositories()
    {
        return this.remoteApt;
    }


    @Override
    public void addRemoteAptRepositories( final RemoteRepository remoteRepository )
    {
        this.remoteApt.add( remoteRepository );
    }


    @Override
    public Set<RemoteRepository> getRemoteTemplateRepositories()
    {
        return this.remoteTemplate;
    }

}
