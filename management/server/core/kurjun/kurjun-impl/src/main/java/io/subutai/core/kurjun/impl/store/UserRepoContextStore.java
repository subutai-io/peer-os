package io.subutai.core.kurjun.impl.store;


import java.io.IOException;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

import ai.subut.kurjun.db.file.FileDb;
import io.subutai.core.kurjun.impl.model.UserRepoContext;


public class UserRepoContextStore
{
    private static final String MAP_NAME_USER_REPO = "user_repo_contexts";

    private final String repoFile;


    public UserRepoContextStore( String appDataBaseUrl )
    {
        String path = appDataBaseUrl == null ? "" : appDataBaseUrl + "/";
        repoFile = path + "kurjun/misc/user_repositories";
    }


    ///////////////// Common private methods /////////////////////
    public void addUserRepoContext( UserRepoContext userRepoContext ) throws IOException
    {
        try ( FileDb fileDb = new FileDb( repoFile ) )
        {
            fileDb.put( MAP_NAME_USER_REPO, makeKey( userRepoContext ), userRepoContext );
        }
    }


    public UserRepoContext removeUserRepoContext( UserRepoContext userRepoContext ) throws IOException
    {
        UserRepoContext removed;
        try ( FileDb fileDb = new FileDb( repoFile ) )
        {
            removed = fileDb.remove( MAP_NAME_USER_REPO, makeKey( userRepoContext ) );
        }
        return removed;
    }


    public Set<UserRepoContext> getUserRepoContexts() throws IOException
    {
        try ( FileDb fileDb = new FileDb( repoFile ) )
        {
            Map<String, UserRepoContext> map = fileDb.get( MAP_NAME_USER_REPO );

            return Sets.newConcurrentHashSet( map.values() );
        }
    }


    private String makeKey( UserRepoContext userRepoContext )
    {
        return userRepoContext.getOwnerFprint() + "_" + userRepoContext.getName();
    }
}
