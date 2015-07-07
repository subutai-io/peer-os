package io.subutai.pluginmanager.impl;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.pluginmanager.impl.PluginInfoImpl;

import static org.junit.Assert.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class PluginInfoImplTest
{
    private PluginInfoImpl pluginInfo;


    @Before
    public void setUp() throws Exception
    {
        pluginInfo = new PluginInfoImpl();
        pluginInfo.setVersion( "555" );
        pluginInfo.setPluginName( "testPluginName" );
        pluginInfo.setRating( "555" );
        pluginInfo.setType( "test" );
    }


    @Test
    public void testGetVersion() throws Exception
    {
        assertNotNull( pluginInfo.getVersion() );
    }


    @Test
    public void testGetPluginName() throws Exception
    {
        assertNotNull( pluginInfo.getPluginName() );
    }


    @Test
    public void testGetType() throws Exception
    {
        assertNotNull( pluginInfo.getType() );
    }


    @Test
    public void testGetRating() throws Exception
    {
        assertNotNull( pluginInfo.getRating() );
    }
}