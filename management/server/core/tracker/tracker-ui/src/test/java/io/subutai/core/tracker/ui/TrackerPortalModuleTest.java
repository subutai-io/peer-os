package io.subutai.core.tracker.ui;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.core.tracker.api.Tracker;
import io.subutai.core.tracker.ui.TrackerPortalModule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * Test for TrackerPortalModule
 */
@RunWith( MockitoJUnitRunner.class )
public class TrackerPortalModuleTest
{

    @Mock
    Tracker tracker;
    private TrackerPortalModule trackerPortalModule;


    @Before
    public void setUp() throws Exception
    {
        trackerPortalModule = new TrackerPortalModule( tracker );
        trackerPortalModule.init();
    }


    @After
    public void tearDown() throws Exception
    {
        trackerPortalModule.destroy();
    }


    @Test( expected = NullPointerException.class )
    public void testConstructorShouldFailOnNullTracker() throws Exception
    {
        new TrackerPortalModule( null );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertEquals( TrackerPortalModule.MODULE_NAME, trackerPortalModule.getId() );
        assertEquals( TrackerPortalModule.MODULE_NAME, trackerPortalModule.getName() );
        assertEquals( TrackerPortalModule.MODULE_IMAGE, trackerPortalModule.getImage().getName() );
    }


    @Test
    public void shouldCreateComponent()
    {

        assertNotNull( trackerPortalModule.createComponent() );
    }


    @Test
    public void shouldBeCore()
    {

        assertTrue( trackerPortalModule.isCorePlugin() );
    }
}
