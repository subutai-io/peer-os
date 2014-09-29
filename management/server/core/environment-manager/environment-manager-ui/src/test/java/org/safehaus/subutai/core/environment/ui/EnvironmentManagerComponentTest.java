package org.safehaus.subutai.core.environment.ui;


import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;


/**
 * Created by bahadyr on 9/25/14.
 */
public class EnvironmentManagerComponentTest
{

    EnvironmentManagerComponent component;
    EnvironmentManagerPortalModule module;


    @Before
    public void init()
    {
        module = mock( EnvironmentManagerPortalModule.class );
        component = new EnvironmentManagerComponent( module );
    }


    @Test
    public void shouldDispose()
    {

        component.dispose();
    }
}
