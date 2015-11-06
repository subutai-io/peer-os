package io.subutai.core.identity.rest.ui;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.reflect.TypeToken;
import io.subutai.core.identity.api.*;
import io.subutai.core.identity.api.model.Role;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.rest.ui.model.CliCommandJson;
import io.subutai.core.identity.rest.ui.model.PortalModuleScopeJson;
import io.subutai.core.identity.rest.ui.model.RestEndpointScopeJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.subutai.common.util.JsonUtil;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;


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
            return Response.ok( jsonUtil.toJson(identityManager.getAllPermissions()) ).build();
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
    public Response setUser( final String username, final String fullName,
                             final String password, final String email,
                             final String rolesJson, final Long userId )
    {
        try
        {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(username));
            Preconditions.checkArgument(!Strings.isNullOrEmpty(fullName));
            Preconditions.checkArgument(!Strings.isNullOrEmpty(password));
            Preconditions.checkArgument(!Strings.isNullOrEmpty(email));

            //JsonUtil.<DiskPartition>fromJson(diskPartition, new TypeToken<DiskPartition>() {}.getType());
//            User newUser;
//
//            if(userId == null || userId <= 0){
//                newUser = identityManager.createUser(username, password, fullName, email, UserType.Regular.getId() );
//            } else {
//                //newUser = jsonUtil.fromJson(username, new TypeToken<User>(){}.getType());
//            }
//
//            List<Role> roles = jsonUtil.fromJson(rolesJson, new TypeToken<ArrayList<Role>>(){}.getType());
//
//            newUser.removeUserAllRoles();
//            newUser.setRoles(roles);
////            for( String roleName : roles ) {
////                Role role = identityManager.getRole(roleName);
////                newUser.addRole(role);
////            }
//            identityManager.updateUser(newUser);

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
            //User userToDelete = identityManager.getUser(userId);

            identityManager.removeUser(userId);

            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error setting new user #setUser", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }

    @Override
    public Response saveRole( final String rolename, final String modulesJson,
                             final String endpointJson, final String cliCommandsJson )
    {
        try
        {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(rolename));

            //JsonUtil.<DiskPartition>fromJson(diskPartition, new TypeToken<DiskPartition>() {}.getType());
//            Role role = identityManager.getRole(rolename);
//
//            if(role == null) {
//                role = identityManager.createRole(rolename);
//            }
//
//            if(!Strings.isNullOrEmpty(cliCommandsJson)) {
//                List<CliCommand> cliCommands = JsonUtil.fromJson(
//                    cliCommandsJson, new TypeToken<ArrayList<CliCommandJson>>() {}.getType()
//                );
//                role.clearCliCommands();
//                for(CliCommand cliCommand: cliCommands) {
//                    role.addCliCommand(cliCommand);
//                }
//            }
//
//            if(!Strings.isNullOrEmpty(modulesJson)) {
//                List<PortalModuleScope> modules = JsonUtil.fromJson(
//                    modulesJson, new TypeToken<ArrayList<PortalModuleScopeJson>>() {}.getType()
//                );
//                role.clearPortalModules();
//                for(PortalModuleScope module: modules) {
//                    role.addPortalModule(module);
//                }
//            }
//
//            if(!Strings.isNullOrEmpty(endpointJson)) {
//                List<RestEndpointScope> endpoints = JsonUtil.fromJson(
//                        endpointJson, new TypeToken<ArrayList<RestEndpointScopeJson>>() {}.getType()
//                );
//                role.clearRestEndpointScopes();
//                for(RestEndpointScope endpoint: endpoints) {
//                    role.addRestEndpointScope(endpoint);
//                }
//            }
//
//            identityManager.updateRole(role);

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
            //Role role = identityManager.getRole( roleName );

            identityManager.removeRole(roleId);

            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error setting new user #setUser", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }
}