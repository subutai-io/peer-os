package io.subutai.core.identity.rest.ui;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;
import io.subutai.common.security.objects.UserType;
import io.subutai.core.identity.api.*;
import io.subutai.core.identity.api.model.Permission;
import io.subutai.core.identity.api.model.Role;
import io.subutai.core.identity.api.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.subutai.common.util.JsonUtil;

import io.subutai.core.identity.rest.ui.PermissionJson;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


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
            return Response.ok( jsonUtil.to(identityManager.getAllPermissions()) ).build();
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
            return Response.ok(jsonUtil.to(identityManager.getAllRoles())).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting roles #getRoles", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }

    @Override
    public Response setUser( final String username, final String fullName,
                             final String password, final String email,
                             final String rolesJson, final Long userId )
    {
        try
        {
//            User testUser = JsonUtil.<User>fromJson(username, new TypeToken<User>() {}.getType());
//            testUser.setFullName(fullName);

            User newUser;

            if(userId == null || userId <= 0){
                Preconditions.checkArgument(!Strings.isNullOrEmpty(username));
                Preconditions.checkArgument(!Strings.isNullOrEmpty(fullName));
                Preconditions.checkArgument(!Strings.isNullOrEmpty(password));
                Preconditions.checkArgument(!Strings.isNullOrEmpty(email));
                newUser = identityManager.createUser(username, password, fullName, email, UserType.Regular.getId() );
            } else {
                newUser = identityManager.getUser(userId );

                if(!Strings.isNullOrEmpty(fullName)) {
                    newUser.setFullName(fullName);
                }

                if(!Strings.isNullOrEmpty(email)) {
                    newUser.setEmail(email);
                }

                if(!Strings.isNullOrEmpty(password)) {
                    newUser.setPassword(password);
                }
            }

            if(!Strings.isNullOrEmpty(rolesJson)) {
                List<Long> roleIds = jsonUtil.fromJson(
                    rolesJson, new TypeToken<ArrayList<Long>>() {}.getType()
                );


                newUser.setRoles( roleIds.stream().map( r -> identityManager.getRole(r) ).collect( Collectors.toList() ) );
            }
            identityManager.updateUser(newUser);

            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error setting new user #setUser", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }

    @Override
    public Response deleteUser( final Long userId )
    {
        try
        {
            identityManager.removeUser(userId);

            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error deleting user #deleteUser", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }

    @Override
    public Response saveRole( final String rolename, final String permissionJson, final Long roleId )
    {
        try
        {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(rolename));

            Role role;

            if(roleId == null || roleId <= 0){
                role = identityManager.createRole( rolename, UserType.Regular.getId() );
            } else {
                role = identityManager.getRole(roleId);
            }

            if(!Strings.isNullOrEmpty(permissionJson)) {
                ArrayList<PermissionJson> permissions = JsonUtil.fromJson(
                    permissionJson, new TypeToken<ArrayList<PermissionJson>>() {}.getType()
                );


                if(!Strings.isNullOrEmpty(rolename)) {
                    role.setName(rolename);
                }

                role.setPermissions( permissions.stream().map( p -> identityManager.createPermission(
                        p.getObject(),
                        p.getScope(),
                        p.getRead(),
                        p.getWrite(),
                        p.getUpdate(),
                        p.getDelete()
                        ) ).collect( Collectors.toList() ) );
            }

            identityManager.updateRole(role);

            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error setting new role #createRole", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }

    @Override
    public Response deleteRole( final Long roleId )
    {
        try
        {
            identityManager.removeRole(roleId);

            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error deleting role #deleteRole", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }
}