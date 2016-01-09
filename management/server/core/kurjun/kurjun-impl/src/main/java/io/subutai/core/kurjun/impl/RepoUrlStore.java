package io.subutai.core.kurjun.impl;


import java.io.IOException;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

import ai.subut.kurjun.db.file.FileDb;


/**
 * Handles persisting and accessing remote repository urls. Leverages FileDb.
 *
 */
public class RepoUrlStore
{
    private static final String MAP_NAME_GLOBAL = "global_repo_urls";
    private static final String MAP_NAME_TEMPLATE = "template_repo_urls";
    private static final String MAP_NAME_APT = "apt_repo_urls";

    private final String repoFile;


    public RepoUrlStore( String appDataBaseUrl )
    {
        String path = appDataBaseUrl == null ? "" : appDataBaseUrl + "/";
        repoFile = path + "kurjun/misc/remote_repo_url";
    }


    ///////////////// Remote template methods /////////////////////
    public void addRemoteTemplateUrl( RepoUrl repoUrl ) throws IOException
    {
        addUrl( repoUrl, MAP_NAME_TEMPLATE );
    }


    public RepoUrl removeRemoteTemplateUrl( RepoUrl repoUrl ) throws IOException
    {
        return removeUrl( repoUrl, MAP_NAME_TEMPLATE );
    }


    public Set<RepoUrl> getRemoteTemplateUrls() throws IOException
    {
        return getUrls( MAP_NAME_TEMPLATE );
    }


    ///////////////// Remote apt methods /////////////////////
    public void addRemoteAptUrl( RepoUrl repoUrl ) throws IOException
    {
        addUrl( repoUrl, MAP_NAME_APT );
    }


    public RepoUrl removeRemoteAptUrl( RepoUrl repoUrl ) throws IOException
    {
        return removeUrl( repoUrl, MAP_NAME_APT );
    }


    public Set<RepoUrl> getRemoteAptUrls() throws IOException
    {
        return getUrls( MAP_NAME_APT );
    }


    ///////////////// Global template methods /////////////////////
    public void addGlobalTemplateUrl( RepoUrl repoUrl ) throws IOException
    {
        addUrl( repoUrl, MAP_NAME_GLOBAL );
    }


    public void removeAllGlobalTemplateUrl() throws IOException
    {
        removeAllUrls( MAP_NAME_GLOBAL );
    }


    public Set<RepoUrl> getGlobalTemplateUrls() throws IOException
    {
        return getUrls( MAP_NAME_GLOBAL );
    }


    ///////////////// Common private methods /////////////////////
    private void addUrl( RepoUrl repoUrl, String mapName ) throws IOException
    {
        try ( FileDb fileDb = new FileDb( repoFile ) )
        {
            fileDb.put( mapName, makeKey( repoUrl ), repoUrl );
        }
    }


    private RepoUrl removeUrl( RepoUrl repoUrl, String mapName ) throws IOException
    {
        RepoUrl removed = null;
        try ( FileDb fileDb = new FileDb( repoFile ) )
        {
            Map<String, RepoUrl> map = fileDb.get( mapName );
            Object[] keys = map.keySet().stream().filter( u -> u.startsWith( repoUrl.getUrl().toString() ) ).toArray();
            for ( Object key : keys )
            {
                removed = fileDb.remove( mapName, key );
            }
        }
        return removed;
    }


    private Set<RepoUrl> getUrls( String mapName ) throws IOException
    {
        try ( FileDb fileDb = new FileDb( repoFile ) )
        {
            Map<String, RepoUrl> map = fileDb.get( mapName );

            return Sets.newConcurrentHashSet( map.values() );
        }
    }


    private void removeAllUrls( String mapName ) throws IOException
    {
        try ( FileDb fileDb = new FileDb( repoFile ) )
        {
            Map<String, RepoUrl> map = fileDb.get( mapName );

            for ( RepoUrl r : map.values() )
            {
                fileDb.remove( mapName, makeKey( r ) );
            }
        }
    }


    private String makeKey( RepoUrl repoUrl )
    {
        return repoUrl.getUrl() + "_" + repoUrl.getToken();
    }
}
