package io.subutai.core.identity.rest.ui;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.subutai.common.util.JsonUtil;

import javax.ws.rs.core.Response;


public class RestServiceImpl implements RestService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(RestServiceImpl.class);
    protected JsonUtil jsonUtil = new JsonUtil();
    private IdentityManager identityManager;


    public RestServiceImpl( final IdentityManager identityManager )
    {
        this.identityManager = identityManager;
    }

    @Override
    public Response getUsers()
    {

        try
        {
            return Response.ok(jsonUtil.to( identityManager.getAllUsers() )).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting users #getUsers", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }

    @Override
    public Response getPermissions()
    {

        try
        {
            String response = String.format("{\"permissions\": %s, \"portalModules\": %s, \"endpoints\": %s, \"commands\": %s",
                    jsonUtil.toJson(identityManager.getAllPermissions()),
                    jsonUtil.toJson(identityManager.getAllPortalModules()),
                    jsonUtil.toJson(identityManager.getAllRestEndpoints()),
                    jsonUtil.toJson(identityManager.getAllCliCommands()));
            return Response.ok( response ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting permissions #getPermissions", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }

    @Override
    public Response getRoles()
    {

        try
        {
            return Response.ok(jsonUtil.to( identityManager.getAllRoles() )).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting roles #getRoles", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }

    @Override
    public Response setUser( final String username, final String fullName, final String password, final String email )
    {
        try
        {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(username));
            Preconditions.checkArgument(!Strings.isNullOrEmpty(fullName));
            Preconditions.checkArgument(!Strings.isNullOrEmpty(password));
            Preconditions.checkArgument(!Strings.isNullOrEmpty(email));

            //User newUser = identityManager.createMockUser( username, fullName, password, email );

            identityManager.addUser(username, fullName, password, email);

            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error setting new user #setUser", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }
}