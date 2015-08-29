package io.subutai.core.environment.impl.dao;


import java.io.PrintStream;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.common.dao.DaoManager;
import io.subutai.common.environment.Blueprint;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;
import io.subutai.core.environment.impl.entity.EnvironmentBlueprintEntity;

import com.google.common.collect.Lists;

import static junit.framework.TestCase.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class BlueprintDataServiceTest
{
    private final static UUID BLUEPRINT_ID = UUID.randomUUID();
    private final static String BLUEPRINT = String.format(
            "{ \"id\":\"%s\", " + "  \"name\": \"Sample blueprint\"," + "  \"nodeGroups\": [" + "    {"
                    + "      \"name\": \"Sample node group\"," + "      \"templateName\": \"master\","
                    + "      \"numberOfContainers\": 2," + "      \"sshGroupId\": 0," + "      \"hostsGroupId\": 0,"
                    + "      \"containerPlacementStrategy\": {" + "        \"strategyId\": \"ROUND_ROBIN\","
                    + "        \"criteria\": []" + "      }" + "    }" + "  ]" + "}", BLUEPRINT_ID );
    @Mock
    DaoManager daoManager;
    @Mock
    EntityManager entityManager;
    @Mock
    RuntimeException exception;
    @Mock
    TypedQuery<EnvironmentBlueprintEntity> typedQuery;
    @Mock
    EnvironmentBlueprintEntity environmentBlueprintEntity;
    @Mock
    Query query;


    BlueprintDataService service;
    Blueprint blueprint;


    @Before
    public void setUp() throws Exception
    {
        service = new BlueprintDataService( daoManager );
        when( daoManager.getEntityManagerFromFactory() ).thenReturn( entityManager );
        blueprint = JsonUtil.fromJson( BLUEPRINT, Blueprint.class );
    }


    @Test( expected = EnvironmentManagerException.class )
    public void testPersist() throws Exception
    {
        service.persist( blueprint );

        verify( entityManager ).merge( isA( EnvironmentBlueprintEntity.class ) );
        verify( daoManager ).commitTransaction( entityManager );
        verify( daoManager ).closeEntityManager( entityManager );

        doThrow( exception ).when( daoManager ).commitTransaction( any( EntityManager.class ) );

        service.persist( blueprint );
    }


    @Test
    public void testGetAll() throws Exception
    {
        when( entityManager.createQuery( anyString(), eq( EnvironmentBlueprintEntity.class ) ) )
                .thenReturn( typedQuery );
        when( typedQuery.getResultList() ).thenReturn( Lists.newArrayList( environmentBlueprintEntity ) );
        when( environmentBlueprintEntity.getInfo() ).thenReturn( BLUEPRINT );

        Set<Blueprint> entityList = service.getAll();

        assertFalse( entityList.isEmpty() );
        verify( daoManager ).closeEntityManager( entityManager );

        doThrow( exception ).when( typedQuery ).getResultList();

        service.getAll();

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }


    @Test (expected = EnvironmentManagerException.class)
    public void testRemove() throws Exception
    {
        when( entityManager.createQuery( anyString() ) ).thenReturn( query );

        service.remove( BLUEPRINT_ID );

        verify( daoManager ).commitTransaction( entityManager );
        verify( daoManager ).closeEntityManager( entityManager );

        doThrow( exception ).when( query ).executeUpdate();

        service.remove( BLUEPRINT_ID );
    }
}
