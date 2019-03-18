package io.subutai.health;


import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import org.apache.karaf.bundle.core.BundleState;
import org.apache.karaf.bundle.core.BundleStateService;

import com.google.common.collect.Lists;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;


@RunWith( MockitoJUnitRunner.class )
public class HealthRestServiceImplTest
{

    @Mock
    BundleContext bundleContext;
    @Mock
    BundleStateService bundleStateService;
    @Mock
    Bundle bundle;

    HealthRestServiceImpl healthRestService;


    @Before
    public void setUp() throws Exception
    {
        healthRestService = Mockito.spy( new HealthRestServiceImpl() );
        doReturn( bundleContext ).when( healthRestService ).getBundleContext();
        doReturn( bundleStateService ).when( healthRestService ).getBundleStateService();
        doReturn( Bundle.ACTIVE ).when( bundle ).getState();
        doReturn( BundleState.Active ).when( bundleStateService ).getState( bundle );
    }


    @Test
    public void testGetState()
    {
        List<Bundle> bundles = Lists.newArrayList( bundle );

        doReturn( bundles.toArray( new Bundle[0] ) ).when( bundleContext ).getBundles();

        assertEquals( HealthService.State.LOADING, healthRestService.getState() );

        doReturn( BundleState.Failure ).when( bundleStateService ).getState( bundle );

        for ( int i = 0; i <= HealthService.BUNDLE_COUNT; i++ )
        {
            bundles.add( bundle );
        }

        doReturn( bundles.toArray( new Bundle[0] ) ).when( bundleContext ).getBundles();

        assertEquals( HealthService.State.FAILED, healthRestService.getState() );

        doReturn( BundleState.Active ).when( bundleStateService ).getState( bundle );

        assertEquals( HealthService.State.READY, healthRestService.getState() );
    }
}
