package io.subutai.common.settings;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.settings.ChannelSettings;
import io.subutai.common.settings.Common;
import io.subutai.common.settings.SecuritySettings;
import io.subutai.common.tracker.OperationState;

import static org.junit.Assert.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class ChannelSettingsTest
{
    private ChannelSettings channelSettings;
    private Common common = new Common();
    private SecuritySettings settings = new SecuritySettings();

    @Before
    public void setUp() throws Exception
    {
        channelSettings = new ChannelSettings();
    }


    @Test
    public void testCheckURLArray() throws Exception
    {
        String[] strings = new String[2];
        strings[0] = "http://www.goeschool.com/erp.pdf";
        strings[1] = "http://angularjs4u.com/forms/angular-js-bootstrap-3-login-form-demo/";

        assertNotNull( channelSettings.checkURLArray( "http://www.goeschool.com/erp.pdf", strings ) );
    }


    @Test
    public void testCheckURL() throws Exception
    {
        OperationState running = OperationState.RUNNING;
        assertNotNull(
                channelSettings.checkURL( "http://www.goeschool.com/erp.pdf", "http://www.goeschool.com/erp.pdf" ) );
    }
}