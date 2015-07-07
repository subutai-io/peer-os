package io.subutai.core.identity.impl.entity;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.core.identity.api.CliCommand;
import io.subutai.core.identity.api.Permission;
import io.subutai.core.identity.api.PortalModuleScope;
import io.subutai.core.identity.api.RestEndpointScope;
import io.subutai.core.identity.impl.entity.CliCommandEntity;
import io.subutai.core.identity.impl.entity.PermissionEntity;
import io.subutai.core.identity.impl.entity.PortalModuleScopeEntity;
import io.subutai.core.identity.impl.entity.RestEndpointScopeEntity;
import io.subutai.core.identity.impl.entity.RoleEntity;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class RoleEntityTest
{
    private RoleEntity roleEntity;

    @Mock
    PermissionEntity permissionEntity;
    @Mock
    Permission permission;
    @Mock
    CliCommandEntity cliCommandEntity;
    @Mock
    CliCommand cliCommand;
    @Mock
    PortalModuleScope portalModuleScope;
    @Mock
    PortalModuleScopeEntity portalModuleScopeEntity;
    @Mock
    RestEndpointScopeEntity restEndpointScopeEntity;
    @Mock
    RestEndpointScope restEndpointScope;


    @Before
    public void setUp() throws Exception
    {
        Set<PermissionEntity> mySet = new HashSet<>();
        mySet.add( permissionEntity );

        roleEntity = new RoleEntity();
        roleEntity.setName( "name" );
        roleEntity = new RoleEntity( "name" );
        roleEntity = new RoleEntity( "name", mySet );
    }


    @Test
    public void testGetName() throws Exception
    {
        assertNotNull( roleEntity.getName() );
    }


    @Test
    public void testGetPermissions() throws Exception
    {
        assertNotNull( roleEntity.getPermissions() );
    }


    @Test
    public void testAddPermission() throws Exception
    {
        roleEntity.addPermission( permissionEntity );
        roleEntity.addPermission( permission );
    }


    @Test
    public void testRemovePermission() throws Exception
    {
        roleEntity.removePermission( permissionEntity );
        roleEntity.removePermission( permission );
    }


    @Test
    public void testGetCliCommands() throws Exception
    {
        assertNotNull( roleEntity.getCliCommands() );
    }


    @Test
    public void testAddCliCommand() throws Exception
    {
        roleEntity.addCliCommand( cliCommandEntity );
    }


    @Test
    public void testSetCliCommands() throws Exception
    {
        List<CliCommand> myList = new ArrayList<>();
        myList.add( cliCommandEntity );

        roleEntity.setCliCommands( myList );
    }


    @Test
    public void testGetAccessibleModules() throws Exception
    {
        assertNotNull( roleEntity.getAccessibleModules() );
    }


    @Test( expected = IllegalArgumentException.class )
    public void testAddPortalModuleException() throws Exception
    {
        roleEntity.addPortalModule( null );
    }


    @Test
    public void testAddPortalModule() throws Exception
    {
        roleEntity.addPortalModule( portalModuleScopeEntity );
    }


    @Test( expected = IllegalArgumentException.class )
    public void testAddPortalModuleException2() throws Exception
    {
        roleEntity.addPortalModule( portalModuleScope );
    }


    @Test
    public void testClearPortalModules() throws Exception
    {
        roleEntity.clearPortalModules();
    }


    @Test( expected = IllegalArgumentException.class )
    public void testAddRestEndpointScopeException() throws Exception
    {
        roleEntity.addRestEndpointScope( null );
    }


    @Test
    public void testAddRestEndpointScope() throws Exception
    {
        roleEntity.addRestEndpointScope( restEndpointScopeEntity );
    }


    @Test( expected = ClassCastException.class )
    public void testAddRestEndpointScopeException2() throws Exception
    {
        roleEntity.addRestEndpointScope( restEndpointScope );
    }


    @Test
    public void testGetAccessibleRestEndpoints() throws Exception
    {
        assertNotNull( roleEntity.getAccessibleRestEndpoints() );
    }


    @Test
    public void testClearRestEndpointScopes() throws Exception
    {
        roleEntity.clearRestEndpointScopes();
    }


    @Test
    public void testCanAccessModule() throws Exception
    {
        assertFalse( roleEntity.canAccessModule( "module" ) );
    }
}