package org.safehaus.subutai.core.environment.api;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentContainer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class EnvironmentContainerNodeTest
{
    private static final String NODE_GROUP_NAME = "name";
    EnvironmentContainer environmentContainer;
    @Mock
    Agent agent;
    @Mock
    Template template;


    @Before
    public void setUp() throws Exception
    {
        this.environmentContainer = new EnvironmentContainer( agent, template, NODE_GROUP_NAME );
    }


    @Test
    public void testGetAgent() throws Exception
    {
        Agent agent = environmentContainer.getAgent();
        assertNotNull( agent );
    }


    @Test
    public void testGetNodeGroupName() throws Exception
    {
        String ngn = environmentContainer.getNodeGroupName();
        assertEquals( NODE_GROUP_NAME, ngn );
    }


    @Test
    public void testGetTemplate() throws Exception
    {
        Template t = environmentContainer.getTemplate();
        assertNotNull( t );
    }
}


