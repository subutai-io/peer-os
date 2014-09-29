package org.safehaus.subutai.core.environment.ui;


import java.util.concurrent.ExecutorService;

import org.junit.After;
import org.junit.Before;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;

import static org.mockito.Mockito.mock;


/**
 * Created by bahadyr on 9/25/14.
 */
public class EnvironmentManagerPortalModuleTest
{
    EnvironmentManagerPortalModule environmentManagerPortalModule;
    ExecutorService executorService;
    EnvironmentManager environmentManager;


    @Before
    public void setup()
    {
        executorService = mock( ExecutorService.class );
        environmentManager = mock( EnvironmentManager.class );
        environmentManagerPortalModule = new EnvironmentManagerPortalModule();
        environmentManagerPortalModule.setExecutor( executorService );
        environmentManagerPortalModule.setEnvironmentManager( environmentManager );
        environmentManagerPortalModule.init();
    }


    @After
    public void tearDown()
    {
        environmentManagerPortalModule.destroy();
    }
}
