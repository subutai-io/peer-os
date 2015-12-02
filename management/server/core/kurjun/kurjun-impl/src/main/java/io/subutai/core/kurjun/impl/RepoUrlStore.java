package io.subutai.core.kurjun.impl;


import ai.subut.kurjun.db.file.FileDb;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Set;


/**
 * Handles persisting and accessing remote repository urls. Leverages FileDb.
 *
 */
public class RepoUrlStore
{
    private static final String MAP_NAME_TEMPLATE = "template_repo_urls";
    private static final String MAP_NAME_APT = "apt_repo_urls";

    private final String remoteRepoFile;


    public RepoUrlStore( String appDataBaseUrl )
    {
        String path = appDataBaseUrl == null ? "kurjun" : appDataBaseUrl;
        remoteRepoFile = path + "/misc/remote_repo_url";
    }


    public void addRemoteTemplateUrl( RepoUrl repoUrl ) throws IOException
    {
        try ( FileDb fileDb = new FileDb( remoteRepoFile ) )
        {
            fileDb.put( MAP_NAME_TEMPLATE, repoUrl.getUrl().toString(), repoUrl );
        }
    }


    public void removeRemoteTemplateUrl( URL url ) throws IOException
    {
        try ( FileDb fileDb = new FileDb( remoteRepoFile ) )
        {
            fileDb.remove( MAP_NAME_TEMPLATE, url.toString() );
        }
    }


    public Set<RepoUrl> getRemoteTemplateUrls() throws IOException
    {
        try ( FileDb fileDb = new FileDb( remoteRepoFile ) )
        {
            Map<String, RepoUrl> map = fileDb.get( MAP_NAME_TEMPLATE );

            return Sets.newConcurrentHashSet( map.values() );
        }
    }

}
