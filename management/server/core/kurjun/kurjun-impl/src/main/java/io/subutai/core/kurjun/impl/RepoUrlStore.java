package io.subutai.core.kurjun.impl;


import ai.subut.kurjun.db.file.FileDb;
import com.google.common.collect.Sets;
import io.subutai.common.settings.Common;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Set;


public class RepoUrlStore
{
    private static final String MAP_NAME_TEMPLATE = "template_repo_urls";
    private static final String MAP_NAME_APT = "apt_repo_urls";

    private final String remoteRepoFile;


    public RepoUrlStore()
    {
        remoteRepoFile = Common.SUBUTAI_APP_DATA_PATH + "/kurjun/misc/remote_repo_url";
    }


    public void addRemoteTemplateUrl( URL url ) throws IOException
    {
        try ( FileDb fileDb = new FileDb( remoteRepoFile ) )
        {
            fileDb.put( MAP_NAME_TEMPLATE, url.toString(), url );
        }
    }


    public void removeRemoteTemplateUrl( URL url ) throws IOException
    {
        try ( FileDb fileDb = new FileDb( remoteRepoFile ) )
        {
            fileDb.remove( MAP_NAME_TEMPLATE, url.toString() );
        }
    }


    public Set<URL> getRemoteTemplateUrls() throws IOException
    {
        try ( FileDb fileDb = new FileDb( remoteRepoFile ) )
        {
            Map<String, URL> map = fileDb.get( MAP_NAME_TEMPLATE );

            return Sets.newConcurrentHashSet( map.values() );
        }
    }

}
